package com.example.nyumbakumitool.ui.Reports;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.nyumbakumitool.R;
import com.example.nyumbakumitool.ui.Users.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewReportActivity extends AppCompatActivity {

    private EditText reportMessage;
    private LinearLayout mediaContainer;
    private Uri selectedMediaUri;

    private DatabaseReference reportsRef;
    private StorageReference storageRef;

    private static final int PICK_MEDIA_REQUEST = 1;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_report);

        // View padding to account for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.new_report_form_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        reportsRef = FirebaseDatabase.getInstance().getReference("reports");
        storageRef = FirebaseStorage.getInstance().getReference("media");

        // Bind views
        reportMessage = findViewById(R.id.reportMessage);
        Button uploadMediaButton = findViewById(R.id.uploadMediaButton);
        Button submitReportButton = findViewById(R.id.submitReportButton);
        mediaContainer = findViewById(R.id.mediaContainer);

        // Upload media button listener
        uploadMediaButton.setOnClickListener(v -> {
            Intent pickMediaIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickMediaIntent.setType("image/* video/*");
            startActivityForResult(pickMediaIntent, PICK_MEDIA_REQUEST);
        });

        // Submit report button listener
        submitReportButton.setOnClickListener(v -> submitReport());
    }

    // Handle result from media picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_MEDIA_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedMediaUri = data.getData();
            displaySelectedMedia(selectedMediaUri); // Display media thumbnail dynamically
        }
    }

    // Dynamically display selected media in mediaContainer
    private void displaySelectedMedia(Uri mediaUri) {
        ImageView mediaPreview = new ImageView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 8, 8, 8);
        mediaPreview.setLayoutParams(layoutParams);

        // Use Glide to load image/video thumbnail into the ImageView
        Glide.with(this).load(mediaUri).into(mediaPreview);

        // Add the ImageView to the mediaContainer
        mediaContainer.addView(mediaPreview);
    }

    private void submitReport() {
        String message = reportMessage.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please enter a message for the report.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create unique report ID
        String reportId = reportsRef.push().getKey();

        // Upload media if selected
        if (selectedMediaUri != null) {
            uploadMediaToFirebase(reportId, message);
        } else {
            saveReportToFirebase(reportId, message, null);
        }
    }

    private void uploadMediaToFirebase(String reportId, String message) {
        StorageReference mediaRef = storageRef.child(reportId + "/" + selectedMediaUri.getLastPathSegment());

        mediaRef.putFile(selectedMediaUri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mediaRef.getDownloadUrl().addOnCompleteListener(urlTask -> {
                            if (urlTask.isSuccessful()) {
                                String mediaUrl = urlTask.getResult().toString();
                                List<String> mediaUrls = new ArrayList<>();
                                mediaUrls.add(mediaUrl);

                                saveReportToFirebase(reportId, message, mediaUrls);
                            } else {
                                Log.e("NewReportActivity", "Failed to get download URL.");
                                Toast.makeText(this, "Failed to upload media.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.e("NewReportActivity", "Media upload failed.");
                        Toast.makeText(this, "Failed to upload media.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveReportToFirebase(String reportId, String message, List<String> mediaUrls) {
        // Create report data
        Map<String, Object> reportData = new HashMap<>();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Fetch additional user details from the database
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserModel user = dataSnapshot.getValue(UserModel.class);
                    if (user != null) {
                        List<String> readBy = new ArrayList<>();
                        readBy.add(user.getUserId());
                        // Prepare data to save with the report
                        long currentTime = System.currentTimeMillis();
                        reportData.put("userId", user.getUserId());
                        reportData.put("readBy", readBy);
                        reportData.put("firstName", user.getFirstName());
                        reportData.put("lastName", user.getLastName());
                        reportData.put("content", message);
                        reportData.put("createdAt", currentTime);
                        reportData.put("updatedAt", currentTime);
                        reportData.put("media", mediaUrls != null ? mediaUrls : new ArrayList<>());

                        // Save the report to Firebase
                        reportsRef.child(reportId).setValue(reportData)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(NewReportActivity.this, "Report submitted successfully.", Toast.LENGTH_SHORT).show();
                                        finish(); // Close the activity after successful submission
                                    } else {
                                        Toast.makeText(NewReportActivity.this, "Failed to submit the report.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("NewReportActivity", "Failed to fetch user data.", databaseError.toException());
                }
            });
        }
    }
}

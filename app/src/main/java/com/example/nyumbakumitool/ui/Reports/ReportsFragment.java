package com.example.nyumbakumitool.ui.Reports;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.nyumbakumitool.MainActivity;
import com.example.nyumbakumitool.NotificationHelper;
import com.example.nyumbakumitool.authentication.LoginActivity;
import com.example.nyumbakumitool.authentication.RegisterActivity;
import com.example.nyumbakumitool.databinding.FragmentReportsBinding;
import com.example.nyumbakumitool.ui.Reports.ReportAdapter;
import com.example.nyumbakumitool.ui.Reports.ReportModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private FragmentReportsBinding binding;
    private ReportAdapter reportAdapter;
    private List<ReportModel> reportList;
    private String currentUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Use binding to inflate the layout
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Get current user ID (assuming you're using FirebaseAuth)
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Button newReport = binding.newReportForm;

        newReport.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewReportForm();
            }
        });

        // Initialize the RecyclerView using view binding
        binding.reportsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the report list
        reportList = new ArrayList<>();

        // Set up the adapter
        reportAdapter = new ReportAdapter(reportList, getContext());
        binding.reportsRecyclerView.setAdapter(reportAdapter);

        // Fetch report data (from Firebase or any other source)
        fetchReportsData();

        return root;
    }

    private void fetchReportsData() {
        // Initialize Firebase Database reference
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference("reports");

        reportsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reportList.clear(); // Clear the old data before adding new data
                for (DataSnapshot reportSnapshot : dataSnapshot.getChildren()) {
                    String reportId = reportSnapshot.getKey();
                    String firstName = reportSnapshot.child("firstName").getValue(String.class);
                    String lastName = reportSnapshot.child("lastName").getValue(String.class);
                    String userId = reportSnapshot.child("userId").getValue(String.class);
                    String content = reportSnapshot.child("content").getValue(String.class);
                    long createdAt = reportSnapshot.child("createdAt").getValue(Long.class);
                    long updatedAt = reportSnapshot.child("updatedAt").getValue(Long.class);

                    // Fetch the readBy list
                    List<String> readByList = new ArrayList<>();
                    if (reportSnapshot.child("readBy").exists()) {
                        for (DataSnapshot readBySnapshot : reportSnapshot.child("readBy").getChildren()) {
                            readByList.add(readBySnapshot.getValue(String.class));
                        }
                    }

                    // If the current user hasn't read this report, trigger a notification
                    if (!readByList.contains(currentUserId)) {
                        sendReportNotification(firstName + " " + lastName, content, createdAt);
                    }

                    // Fetch media URLs
                    List<String> mediaUrls = new ArrayList<>();
                    for (DataSnapshot mediaSnapshot : reportSnapshot.child("media").getChildren()) {
                        mediaUrls.add(mediaSnapshot.getValue(String.class));
                    }

                    // Fetch comments
                    List<CommentModel> comments = new ArrayList<>();
                    for (DataSnapshot commentSnapshot : reportSnapshot.child("comments").getChildren()) {
                        String commentId = commentSnapshot.child("commentId").getValue(String.class);
                        String commentUserId = commentSnapshot.child("userId").getValue(String.class);
                        String commentText = commentSnapshot.child("comment").getValue(String.class);
                        long commentTimestamp = commentSnapshot.child("timestamp").getValue(Long.class);

                        comments.add(new CommentModel(commentId, commentUserId, "", "https://firebasestorage.googleapis.com/v0/b/database-89d71.appspot.com/o/User_Placeholder.png?alt=media&token=8da0e626-2d4d-4b24-ad0e-4b7fea422595", commentText, commentTimestamp));
                    }

                    // Create the report model and add it to the list
                    ReportModel report = new ReportModel(reportId, userId, firstName + " " + lastName, "https://firebasestorage.googleapis.com/v0/b/database-89d71.appspot.com/o/User_Placeholder.png?alt=media&token=8da0e626-2d4d-4b24-ad0e-4b7fea422595", content, mediaUrls, createdAt, comments);
                    reportList.add(report);

                    // Mark the report as read when opened
                    markReportAsRead(reportId);
                }

                // Notify adapter after data has been loaded
                reportAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void sendReportNotification(String sender, String message, long timestamp) {
        // Use the NotificationHelper to send a notification with the report details
        NotificationHelper.showNotification(getContext(), sender, message, timestamp);
    }

    private void openNewReportForm() {
        Intent intent = new Intent(getActivity(), NewReportActivity.class);
        startActivity(intent);
    }

    // Helper method to convert date string to timestamp
    private long parseDateToTimestamp(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        try {
            Date date = sdf.parse(dateString);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void markReportAsRead(String reportId) {
        DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference("reports").child(reportId);

        // Fetch the readBy list for the report
        reportRef.child("readBy").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> readByList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        readByList.add(snapshot.getValue(String.class));
                    }
                }

                // If current user hasn't read this report, mark it as read
                if (!readByList.contains(currentUserId)) {
                    readByList.add(currentUserId);

                    // Update the readBy field in Firebase
                    reportRef.child("readBy").setValue(readByList)
                            .addOnSuccessListener(aVoid -> {
                                // Success logging or actions
                            })
                            .addOnFailureListener(e -> {
                                // Handle the failure
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
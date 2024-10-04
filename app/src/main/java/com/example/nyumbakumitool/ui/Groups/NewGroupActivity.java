package com.example.nyumbakumitool.ui.Groups;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nyumbakumitool.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewGroupActivity extends AppCompatActivity {

    private EditText groupNameEditText;
    private Button createGroupButton;

    private DatabaseReference groupsRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        // Initialize Firebase components
        groupsRef = FirebaseDatabase.getInstance().getReference("groups");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize views
        groupNameEditText = findViewById(R.id.groupNameEditText);
        createGroupButton = findViewById(R.id.createGroupButton);

        // Set click listener for create group button
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewGroup();
            }
        });
    }

    private void createNewGroup() {
        String groupName = groupNameEditText.getText().toString().trim();

        // Check if the group name is provided
        if (TextUtils.isEmpty(groupName)) {
            groupNameEditText.setError("Group name is required");
            return;
        }

        // Get the current user ID (the admin of the group)
        String adminId = currentUser.getUid();

        // Generate a new group ID
        String groupId = groupsRef.push().getKey();

        // Prepare the group data to store in Firebase
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("adminId", adminId);
        groupData.put("groupName", groupName);
        groupData.put("createdAt", System.currentTimeMillis());
        groupData.put("updatedAt", System.currentTimeMillis());

        // For simplicity, adding only the admin as the initial member
        List<String> members = new ArrayList<>();
        members.add(adminId);
        groupData.put("members", members);

        // Save the group data in Firebase under the generated group ID
        groupsRef.child(groupId).setValue(groupData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(NewGroupActivity.this, "Group created successfully!", Toast.LENGTH_SHORT).show();
                finish();  // Close the activity and return to the previous screen
            } else {
                Toast.makeText(NewGroupActivity.this, "Failed to create group", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

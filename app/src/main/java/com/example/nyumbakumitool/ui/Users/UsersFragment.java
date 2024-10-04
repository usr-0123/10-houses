package com.example.nyumbakumitool.ui.Users;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nyumbakumitool.databinding.FragmentUsersBinding;
import com.example.nyumbakumitool.ui.Chats.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersFragment extends Fragment {

    private FragmentUsersBinding binding;
    private UsersAdapter usersAdapter;
    private List<UserModel> usersList;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        // Initialize RecyclerView
        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize users list
        usersList = new ArrayList<>();

        // Set up the adapter
        usersAdapter = new UsersAdapter(usersList, currentUserId, new UsersAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(String currentUserId, String selectedUserId) {
                searchChat(selectedUserId);
            }
        });
        binding.recyclerViewUsers.setAdapter(usersAdapter);

        // Fetch users from Firebase
        fetchUsersFromFirebase();

        return root;
    }

    private void fetchUsersFromFirebase() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear(); // Clear the old data before adding new data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    if (user != null) {
                        usersList.add(user);
                    } else {
                        Toast.makeText(getContext(), "No users found", Toast.LENGTH_SHORT).show();
                    }
                }
                // Notify adapter after data has been loaded
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error fetching data
                Toast.makeText(requireContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Error fetching data", databaseError.toException());
            }
        });
    }

    private void searchChat(String selectedUserId) {
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean chatFound = false;
                String chatId = null;

                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                    // Safely get the list as List<Object>
                    List<Object> membersObjectList = (List<Object>) chatSnapshot.child("members").getValue();

                    if (membersObjectList == null || membersObjectList.size() < 2) continue;

                    // Convert List<Object> to List<String> safely
                    List<String> members = new ArrayList<>();
                    for (Object memberObject : membersObjectList) {
                        if (memberObject instanceof String) {
                            members.add((String) memberObject);
                        }
                    }

                    // Now you can safely check if the list contains the IDs
                    if (members.contains(currentUserId) && members.contains(selectedUserId)) {
                        chatFound = true;
                        chatId = chatSnapshot.getKey();
                        break;
                    }
                }

                if (chatFound && chatId != null) {
                    // Chat exists, navigate to ChatActivity
                    navigateToChatActivity(chatId);
                } else {
                    // Chat does not exist, create a new one
                    createNewChat(selectedUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error searching for chat", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Error searching for chat", databaseError.toException());
            }
        });
    }

    private void createNewChat(String selectedUserId) {
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        // Create a new chat
        String chatId = chatsRef.push().getKey();
        if (chatId != null) {
            Map<String, Object> chatData = new HashMap<>();
            List<String> members = new ArrayList<>();
            members.add(currentUserId);
            members.add(selectedUserId);
            chatData.put("members", members);
            chatData.put("createdAt", System.currentTimeMillis());
            chatData.put("updatedAt", System.currentTimeMillis());

            chatsRef.child(chatId).setValue(chatData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Navigate to the newly created chat
                    navigateToChatActivity(chatId);
                } else {
                    Toast.makeText(getContext(), "Failed to create chat", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void navigateToChatActivity(String chatId) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("chatId", chatId);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.example.nyumbakumitool.ui.Users;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.nyumbakumitool.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<UserModel> usersList;
    private Context context;
    private String currentUserId;

    private OnUserClickListener onUserClickListener;

    // Define an interface for click handling
    public interface OnUserClickListener {
        void onUserClick(String currentUserId, String selectedUserId);
    }

    // Constructor
    public UsersAdapter(List<UserModel> usersList, String currentUserId, OnUserClickListener onUserClickListener) {
        this.usersList = usersList;
        this.currentUserId = currentUserId;
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = usersList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        String formattedDate = sdf.format(new Date(user.getCreatedAt()));

        holder.textViewFullName.setText(user.getFirstName() + " " + user.getLastName());
        holder.textViewEmail.setText(user.getEmail());
        holder.textViewPhoneNumber.setText(user.getPhone());

        // Load profile image using Glide
        Glide.with(holder.itemView.getContext())
                .load(user.getProfileImage())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.imageViewProfile);

        // Set the click listener
        holder.itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(currentUserId, user.getUserId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProfile;
        TextView textViewFullName, textViewEmail, textViewPhoneNumber;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            textViewFullName = itemView.findViewById(R.id.textViewFullName);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewPhoneNumber = itemView.findViewById(R.id.textViewPhoneNumber);
        }
    }
}

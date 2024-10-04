package com.example.nyumbakumitool.ui.Reports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nyumbakumitool.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
    private List<ReportModel> reportList;
    private Context context;

    public ReportAdapter(List<ReportModel> reportList, Context context) {
        this.reportList = reportList;
        this.context = context;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportModel report = reportList.get(position);

        // Set the username, message, and createdAt timestamp
        holder.usernameTextView.setText(report.getUsername());
        holder.messageTextView.setText(report.getMessage());

        // Format the timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        String formattedDate = sdf.format(new Date(report.getCreatedAt()));
        holder.createdAtTextView.setText(formattedDate);

        // Load profile image using Glide
        Glide.with(context)
                .load(report.getProfileImage())
                .into(holder.profileImageView);

        // Set up the media RecyclerView for this report
        List<String> mediaUrls = report.getMediaUrls();
//        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            MediaAdapter mediaAdapter = new MediaAdapter(mediaUrls, context);
            holder.mediaRecyclerView.setAdapter(mediaAdapter);
            holder.mediaRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
//        }
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, messageTextView, createdAtTextView;
        ImageView profileImageView;
        RecyclerView mediaRecyclerView;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            createdAtTextView = itemView.findViewById(R.id.createdAtTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            mediaRecyclerView = itemView.findViewById(R.id.mediaRecyclerView);
        }
    }
}

package com.example.nyumbakumitool.ui.Reports;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nyumbakumitool.R;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    private List<String> mediaUrls; // URLs of media files (images/videos)
    private Context context;

    public MediaAdapter(List<String> mediaUrls, Context context) {
        this.mediaUrls = mediaUrls;
        this.context = context;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.media_item, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        String mediaUrl = mediaUrls.get(position);

        // Check if it's an image or video (based on file extension or mime type)
        if (mediaUrl != null) {
            // Load the image using Glide
            Glide.with(context)
                    .load(mediaUrl)
                    .into(holder.mediaImageView);
            holder.mediaImageView.setVisibility(View.VISIBLE);
            holder.mediaVideoView.setVisibility(View.GONE);

            holder.mediaImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ImageDisplayActivity.class);
                    // Pass the media URL to the ImageDisplayActivity
                    intent.putExtra("image_url", mediaUrl);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mediaUrls.size();
    }

    public static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView mediaImageView;
        VideoView mediaVideoView;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            mediaImageView = itemView.findViewById(R.id.mediaImageView);
            mediaVideoView = itemView.findViewById(R.id.mediaVideoView);
        }
    }
}

package com.example.mp08_firebase;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.recyclerview.widget.RecyclerView;

public class PostViewHolder extends RecyclerView.ViewHolder {
    ImageView avatarImageView;
    TextView displayNameTextView, usernameTextView, contentTextView;
    ImageView mediaImageView;
    VideoView mediaVideoView;

    public PostViewHolder(@NonNull View itemView, int viewType) {
        super(itemView);
        avatarImageView = itemView.findViewById(R.id.avatarImageView);
        displayNameTextView = itemView.findViewById(R.id.displayNameTextView);
        usernameTextView = itemView.findViewById(R.id.usernameTextView);
        contentTextView = itemView.findViewById(R.id.contentTextView);
        mediaImageView = itemView.findViewById(R.id.mediaImageView);
        mediaVideoView = itemView.findViewById(R.id.mediaVideoView);
    }
}
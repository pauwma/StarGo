package com.example.mp08_firebase;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.recyclerview.widget.RecyclerView;

public class PostViewHolder extends RecyclerView.ViewHolder {

    TextView displayNameTextView, usernameTextView, timestampTextView, contentTextView;
    ImageView avatarImageView;
    ImageView mediaImageView;
    VideoView mediaVideoView;

    // ? Likes & Comments
    ImageButton likeButton, commentButton;
    TextView likesNumTextView, commentsNumTextView;

    public PostViewHolder(@NonNull View itemView, int viewType) {
        super(itemView);

        displayNameTextView = itemView.findViewById(R.id.displayNameTextView);
        usernameTextView = itemView.findViewById(R.id.usernameTextView);
        avatarImageView = itemView.findViewById(R.id.avatarImageView);
        contentTextView = itemView.findViewById(R.id.contentTextView);
        timestampTextView = itemView.findViewById(R.id.timestampTextView);
        likeButton = itemView.findViewById(R.id.likeButton);
        commentButton = itemView.findViewById(R.id.commentButton);
        likesNumTextView = itemView.findViewById(R.id.likesNumTextView);
        commentsNumTextView = itemView.findViewById(R.id.commentsNumTextView);

        if (viewType == 1) {
            mediaImageView = itemView.findViewById(R.id.mediaImageView);
        } else if (viewType == 2) {
            mediaVideoView = itemView.findViewById(R.id.mediaVideoView);
        }
    }
}

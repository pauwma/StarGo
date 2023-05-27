package com.example.mp08_firebase.items;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp08_firebase.R;

public class PostViewHolder extends RecyclerView.ViewHolder {

    TextView displayNameTextView, usernameTextView, timestampTextView, contentTextView;
    ImageView avatarImageView;
    ImageView mediaImageView;
    VideoView mediaVideoView;

    ConstraintLayout likeLayout, commentLayout, shareLayout;

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
        likeLayout = itemView.findViewById(R.id.likeLayout);
        commentLayout = itemView.findViewById(R.id.commentLayout);
        shareLayout = itemView.findViewById(R.id.shareLayout);

        if (viewType == 1) {
            mediaImageView = itemView.findViewById(R.id.mediaImageView);
        } else if (viewType == 2) {
            mediaVideoView = itemView.findViewById(R.id.mediaVideoView);
        }
    }
}

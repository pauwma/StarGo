package com.example.mp08_firebase;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentViewHolder extends RecyclerView.ViewHolder {
    CircleImageView avatarImageView;
    TextView displayNameTextView, usernameTextView, timestampTextView, contentTextView;

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);

        avatarImageView = itemView.findViewById(R.id.avatarImageView);
        displayNameTextView = itemView.findViewById(R.id.displayNameTextView);
        usernameTextView = itemView.findViewById(R.id.usernameTextView);
        timestampTextView = itemView.findViewById(R.id.timestampTextView);
        contentTextView = itemView.findViewById(R.id.contentTextView);
    }
}

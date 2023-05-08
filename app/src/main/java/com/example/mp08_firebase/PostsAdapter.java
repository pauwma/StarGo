package com.example.mp08_firebase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostViewHolder> {

    private Context context;

    public PostsAdapter(Context context, FirestoreRecyclerOptions<Post> options) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        switch (viewType) {
            case 1: // Tipo de media: imagen
                view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
                break;
            case 2: // Tipo de media: video
                view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
                break;
            default: // Sin media
                view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        }
        return new PostViewHolder(view, viewType);
    }

    @Override
    protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Post post) {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users").document(post.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String displayName = documentSnapshot.getString("displayName");
                            String avatar = documentSnapshot.getString("avatar");

                            // Set user info in Post object
                            post.setUsername(username);
                            post.setDisplayName(displayName);
                            post.setAvatar(avatar);

                            // Set user info in ViewHolder
                            holder.displayNameTextView.setText(displayName);
                            holder.usernameTextView.setText(username);
                            Glide.with(context).load(avatar).into(holder.avatarImageView);
                        }
                    }
                });

        holder.contentTextView.setText(post.getContent());

        if (post.getMediaType() != null) {
            switch (post.getMediaType()) {
                case "image":
                    Glide.with(context).load(post.getMedia()).into(holder.mediaImageView);
                    break;
                case "video":
                    // Carga el video en el reproductor de video, aquí se asume que se utiliza un VideoView
                    VideoView videoView = holder.itemView.findViewById(R.id.mediaVideoView);
                    videoView.setVideoPath(post.getMedia());
                    videoView.requestFocus();
                    videoView.start();
                    break;
            }
        }
    }
}
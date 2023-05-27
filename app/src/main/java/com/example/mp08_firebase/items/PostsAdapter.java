package com.example.mp08_firebase.items;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.mp08_firebase.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostViewHolder> {

    private Context context;
    private ProgressBar progressBar;
    private String userUID;
    private int currentLikes;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    private OnPostClickListener listener;

    public PostsAdapter(Context context, FirestoreRecyclerOptions<Post> options, ProgressBar progressBar, OnPostClickListener listener) {
        super(options);
        this.context = context;
        this.progressBar = progressBar;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        View view;

        switch (viewType) {
            case 1: // Tipo de media: imagen
                view = LayoutInflater.from(context).inflate(R.layout.item_post_image, parent, false);
                break;
            case 2: // Tipo de media: video
                view = LayoutInflater.from(context).inflate(R.layout.item_post_video, parent, false);
                break;
            default: // Sin media
                view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        }
        return new PostViewHolder(view, viewType);
    }

    @Override
    protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Post post) {

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;

        holder.displayNameTextView.setMaxWidth(screenWidth / 3);
        holder.usernameTextView.setMaxWidth(screenWidth / 3);
        holder.usernameTextView.setEllipsize(TextUtils.TruncateAt.END);
        holder.contentTextView.setOnClickListener(v -> listener.onPostClick(post));

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users").document(post.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String displayName = documentSnapshot.getString("displayName");
                            String avatar = documentSnapshot.getString("avatar");

                            // Set user info in ViewHolder
                            holder.displayNameTextView.setText(displayName);
                            holder.usernameTextView.setText("@" + username);
                            Glide.with(context).load(avatar).into(holder.avatarImageView);

                            // Agregar OnClickListener a avatarImageView
                            holder.avatarImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToProfile(v, post);
                                }
                            });

                            holder.displayNameTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToProfile(v, post);
                                }
                            });

                            holder.usernameTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToProfile(v, post);
                                }
                            });
                        }
                    }
                });

        if (post.getContent() == null || post.getContent().isEmpty()){
            holder.contentTextView.setVisibility(View.GONE);
        } else {
            holder.contentTextView.setText(post.getContent());
        }
        holder.timestampTextView.setText(" · " + getTimeAgo(post.getTimestamp()));

        // ? Comprobación de media
        if (post.getMediaType() != null) {
            switch (post.getMediaType()) {
                case "image":
                    holder.mediaImageView.setOnClickListener(v -> listener.onPostClick(post));

                    holder.mediaImageView.setOnLongClickListener(v -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("imageUrl", post.getMedia());
                        Navigation.findNavController(v).navigate(R.id.detailedImageFragment, bundle);
                        return true;
                    });

                    if (post.getMedia() != null) {
                        int dpValue = 20;
                        float density = context.getResources().getDisplayMetrics().density;
                        int radius = (int) (dpValue * density);
                        Glide.with(context).load(post.getMedia())
                                .transform(new RoundedCorners(radius))
                                .into(holder.mediaImageView);
                    }
                    break;
                case "video":
                    holder.mediaVideoView.setOnClickListener(v -> listener.onPostClick(post));
                    // Carga el video en el reproductor de video, aquí se asume que se utiliza un VideoView
                    VideoView videoView = holder.itemView.findViewById(R.id.mediaVideoView);
                    videoView.setVideoPath(post.getMedia());
                    videoView.requestFocus();
                    videoView.start();
                    break;
            }
        }

        // ? Verificar si el usuario actual ha dado "like" a la publicación
        FirebaseFirestore.getInstance().collection("posts").document(post.getPostId()).collection("likes").document(userUID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Si el usuario ha dado "like", cambiar el ícono a "like_solid_icon"
                    holder.likeButton.setImageResource(R.drawable.heart_solid);
                    holder.likeButton.setAlpha(1f);
                    holder.likesNumTextView.setTextColor(ContextCompat.getColor(context,R.color.morado));
                } else {
                    // Si el usuario no ha dado "like", cambiar el ícono a "like_icon"
                    holder.likeButton.setImageResource(R.drawable.heart_regular);
                    holder.likesNumTextView.setTextColor(ContextCompat.getColor(context,R.color.white));
                    holder.likesNumTextView.setAlpha(0.7f);
                }
            }
        });

        // ? Establecer el número de "likes"
        FirebaseFirestore.getInstance().collection("posts").document(post.getPostId()).collection("likes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int likesCount = task.getResult().size();
                    holder.likesNumTextView.setText(String.valueOf(likesCount));
                } else {
                    holder.likesNumTextView.setText("0");
                }
            }
        });

        // ? Establecer el número de "comments"
        FirebaseFirestore.getInstance().collection("posts").document(post.getPostId()).collection("comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int likesCount = task.getResult().size();
                    if (likesCount <= 0){
                        holder.commentsNumTextView.setText("0");
                    } else {
                        holder.commentsNumTextView.setText(String.valueOf(likesCount));
                    }
                } else {
                    holder.commentsNumTextView.setText("0");
                }
            }
        });

        holder.likeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Agregar o eliminar "like" en Firestore
                FirebaseFirestore.getInstance().collection("posts").document(post.getPostId()).collection("likes").document(userUID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // Verificar si el texto actual es un número válido
                        try {
                            currentLikes = Integer.parseInt(holder.likesNumTextView.getText().toString());
                        } catch (NumberFormatException e) {
                            // Si no es un número válido, establecer el contador de "likes" en 0
                            currentLikes = 0;
                        }

                        if (documentSnapshot.exists()) {
                            // Si el usuario ya ha dado "like", eliminar el "like"
                            FirebaseFirestore.getInstance().collection("posts").document(post.getPostId()).collection("likes").document(userUID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Actualizar el botón y el contador de "likes"
                                    holder.likeButton.setImageResource(R.drawable.heart_regular);
                                    holder.likeButton.setAlpha(0.7f);
                                    holder.likesNumTextView.setTextColor(ContextCompat.getColor(context,R.color.white));
                                    holder.likesNumTextView.setAlpha(0.7f);
                                    holder.likesNumTextView.setText(String.valueOf(getCurrentLikes(holder) - 1));
                                }
                            });


                        } else {
                            // Si el usuario no ha dado "like", agregar un nuevo "like"
                            FirebaseFirestore.getInstance().collection("posts").document(post.getPostId()).collection("likes").document(userUID).set(Collections.singletonMap("timestamp", FieldValue.serverTimestamp())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Actualizar el botón y el contador de "likes"
                                    holder.likeButton.setImageResource(R.drawable.heart_solid);
                                    holder.likeButton.setAlpha(1f);
                                    holder.likesNumTextView.setText(String.valueOf(getCurrentLikes(holder) + 1));
                                    holder.likesNumTextView.setAlpha(1f);
                                    holder.likesNumTextView.setTextColor(ContextCompat.getColor(context,R.color.morado));
                                }
                            });
                        }
                    }
                });
            }
        });
        holder.commentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("commentButton", true);
                bundle.putString("postId", post.getPostId());
                bundle.putString("uid", post.getUid());
                bundle.putString("content", post.getContent());
                bundle.putString("media", post.getMedia());
                bundle.putString("mediaType", post.getMediaType());
                bundle.putString("timestamp", String.valueOf(post.getTimestamp()));
                Navigation.findNavController(view).navigate(R.id.mediaFragment, bundle);
            }
        });

        progressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemViewType(int position) {
        Post post = getItem(position);

        if (post.getMediaType() == null) {
            return 0;
        } else if (post.getMediaType().equals("image")) {
            return 1;
        } else if (post.getMediaType().equals("video")) {
            return 2;
        } else {
            return 0;
        }
    }

    public String getTimeAgo(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - timestamp;

        if (timeDiff < TimeUnit.SECONDS.toMillis(60)) {
            long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff);
            return seconds + "s";
        } else if (timeDiff < TimeUnit.MINUTES.toMillis(60)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);
            return minutes + "m";
        } else if (timeDiff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(timeDiff);
            return hours + "h";
        } else if (timeDiff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(timeDiff);
            return days + "d";
        } else {
            Calendar postCalendar = Calendar.getInstance();
            postCalendar.setTimeInMillis(timestamp);
            Calendar currentCalendar = Calendar.getInstance();

            if (postCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
                return dateFormat.format(new Date(timestamp));
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy", Locale.getDefault());
                return dateFormat.format(new Date(timestamp));
            }
        }
    }

    public int getCurrentLikes(PostViewHolder holder) {
        String currentLikesText = holder.likesNumTextView.getText().toString();
        int currentLikes = 0;

        try {
            currentLikes = Integer.parseInt(currentLikesText);
        } catch (NumberFormatException e) {
            // Manejar excepción si el texto no se puede convertir en un número entero
            Log.e("ERROR", "Error al convertir el número de likes a entero", e);
        }

        return currentLikes;
    }

    public void goToProfile(View v, Post post){
        if (post.getUid().equals(FirebaseAuth.getInstance().getUid())){
            Navigation.findNavController(v).navigate(R.id.profileFragment);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("userUID", post.getUid()); // Pasar el UID del usuario al fragmento de perfil del usuario
            Navigation.findNavController(v).navigate(R.id.usersProfileFragment, bundle);
        }
    }
}
package com.example.mp08_firebase;

import static android.content.ContentValues.TAG;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MediaFragment extends Fragment {

    private NavController navController;
    private ImageButton crossImageButton;

    // ? Post & User info
    private String postID, postUID, content, media, mediaType, timestamp, userUID;
    private int currentLikes;
    private TextView displayNameTextView, usernameTextView, contentTextView, timestampTextView, likesNumTextView, commentsNumTextView;
    private CircleImageView profile_image;
    private ImageView mediaImageView;
    private ImageButton likeButton, commentButton;
    private ConstraintLayout userLayout, likeLayout, commentLayout;
    private RecyclerView commentsRecyclerView;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        displayNameTextView = view.findViewById(R.id.displayNameTextView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        contentTextView = view.findViewById(R.id.contentTextView);
        timestampTextView = view.findViewById(R.id.timestampTextView);
        profile_image = view.findViewById(R.id.profile_image);
        mediaImageView = view.findViewById(R.id.mediaImageView);
        userLayout = view.findViewById(R.id.userLayout);
        likesNumTextView = view.findViewById(R.id.likesNumTextView);
        commentsNumTextView = view.findViewById(R.id.commentsNumTextView);
        likeLayout = view.findViewById(R.id.likeLayout);
        commentLayout = view.findViewById(R.id.commentLayout);
        likeButton = view.findViewById(R.id.likeButton);
        commentButton = view.findViewById(R.id.commentButton);
        commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView);
        db = FirebaseFirestore.getInstance();

        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (getArguments() != null) {
            postID = getArguments().getString("postId");
            postUID = getArguments().getString("uid");
            content = getArguments().getString("content");
            media = getArguments().getString("media");
            mediaType = getArguments().getString("mediaType");
            timestamp = getArguments().getString("timestamp");
        }

        // ? Content
        if (content != null && !content.trim().equals("")){
            contentTextView.setText(content);
            contentTextView.setVisibility(View.VISIBLE);
        } else {
            contentTextView.setVisibility(View.GONE);
        }

        // ? Media
        if (mediaType != null && !mediaType.isEmpty()){
            if (mediaType.equals("image") && media != null){
                int dpValue = 20;
                float density = getContext().getResources().getDisplayMetrics().density;
                int radius = (int) (dpValue * density);
                Glide.with(getContext()).load(media)
                        .transform(new RoundedCorners(radius))
                        .into(mediaImageView);
                mediaImageView.setVisibility(View.VISIBLE);
            } else {
                mediaImageView.setVisibility(View.GONE);
            }
        } else {
            mediaImageView.setVisibility(View.GONE);
        }

        // ? Timestamp
        timestampTextView.setText(formatDate(Long.parseLong(timestamp)));

        userInfo();

        // ? Perfil del usuario
        userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfile(v, postUID);
            }
        });

        // ? Flecha Back
        view.findViewById(R.id.crossImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });

        // ? Imagen
        mediaImageView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("imageUrl", media);
            navController.navigate(R.id.detailedImageFragment, bundle);
        });

        // ? Verificar si el usuario actual ha dado "like" a la publicación
        FirebaseFirestore.getInstance().collection("posts").document(postID).collection("likes").document(userUID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Si el usuario ha dado "like", cambiar el ícono a "like_solid_icon"
                    likeButton.setImageResource(R.drawable.heart_solid);
                    likeButton.setAlpha(1f);
                    likesNumTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.morado));
                } else {
                    // Si el usuario no ha dado "like", cambiar el ícono a "like_icon"
                    likeButton.setImageResource(R.drawable.heart_regular);
                    likesNumTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                    likesNumTextView.setAlpha(0.7f);
                }
            }
        });

        // ? Establecer el número de "likes"
        FirebaseFirestore.getInstance().collection("posts").document(postID).collection("likes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@android.support.annotation.NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int likesCount = task.getResult().size();
                    likesNumTextView.setText(String.valueOf(likesCount));
                } else {
                    likesNumTextView.setText("0");
                }
            }
        });

        // ? Establecer el número de "comments"
        FirebaseFirestore.getInstance().collection("posts").document(postID).collection("comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@android.support.annotation.NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int likesCount = task.getResult().size();
                    if (likesCount <= 0){
                        commentsNumTextView.setText("0");
                    } else {
                        commentsNumTextView.setText(String.valueOf(likesCount));
                    }
                } else {
                    commentsNumTextView.setText("0");
                }
            }
        });
        commentsRecyclerView.setNestedScrollingEnabled(false);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (commentsRecyclerView != null) {
            getCommentsFromFirebase(postID, new OnCommentsReceivedListener() {
                @Override
                public void onCommentsReceived(List<Comment> comments) {
                    Log.d("getCommentsFromFirebase", "Received " + comments.size() + " comments");
                    CommentAdapter adapter = new CommentAdapter(getContext(), comments);
                    commentsRecyclerView.setAdapter(adapter);
                }
            });
        } else {
            Log.d("getCommentsFromFirebase", "commentsRecyclerView is null");
        }



        likeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Agregar o eliminar "like" en Firestore
                FirebaseFirestore.getInstance().collection("posts").document(postID).collection("likes").document(userUID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // Verificar si el texto actual es un número válido
                        try {
                            currentLikes = Integer.parseInt(likesNumTextView.getText().toString());
                        } catch (NumberFormatException e) {
                            // Si no es un número válido, establecer el contador de "likes" en 0
                            currentLikes = 0;
                        }

                        if (documentSnapshot.exists()) {
                            // Si el usuario ya ha dado "like", eliminar el "like"
                            FirebaseFirestore.getInstance().collection("posts").document(postID).collection("likes").document(userUID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Actualizar el botón y el contador de "likes"
                                    likeButton.setImageResource(R.drawable.heart_regular);
                                    likeButton.setAlpha(0.7f);
                                    likesNumTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                                    likesNumTextView.setAlpha(0.7f);
                                    likesNumTextView.setText(String.valueOf(getCurrentLikes() - 1));
                                }
                            });


                        } else {
                            // Si el usuario no ha dado "like", agregar un nuevo "like"
                            FirebaseFirestore.getInstance().collection("posts").document(postID).collection("likes").document(userUID).set(Collections.singletonMap("timestamp", FieldValue.serverTimestamp())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Actualizar el botón y el contador de "likes"
                                    likeButton.setImageResource(R.drawable.heart_solid);
                                    likeButton.setAlpha(1f);
                                    likesNumTextView.setText(String.valueOf(getCurrentLikes() + 1));
                                    likesNumTextView.setAlpha(1f);
                                    likesNumTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.morado));
                                }
                            });
                        }
                    }
                });
            }
        });

    }

    public void userInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(postUID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String username = document.getString("username");
                        String displayName = document.getString("displayName");
                        String avatarURL = document.getString("avatar");

                        usernameTextView.setText("@" + username);
                        displayNameTextView.setText(displayName);
                        Glide.with(requireView()).load(avatarURL).into(profile_image);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
    public String formatDate(long timestamp) {
        // Convertir timestamp a Date
        Date date = new Date(timestamp);

        // Crear el formato de fecha
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a '·' d MMM. yyyy", new Locale("es", "ES"));

        // Formatear la fecha
        return sdf.format(date);
    }
    public void goToProfile(View v, String uid){
        if (uid.equals(FirebaseAuth.getInstance().getUid())){
            Navigation.findNavController(v).navigate(R.id.profileFragment);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("userUID", uid); // Pasar el UID del usuario al fragmento de perfil del usuario
            Navigation.findNavController(v).navigate(R.id.usersProfileFragment, bundle);
        }
    }
    public int getCurrentLikes() {
        String currentLikesText = likesNumTextView.getText().toString();
        int currentLikes = 0;

        try {
            currentLikes = Integer.parseInt(currentLikesText);
        } catch (NumberFormatException e) {
            // Manejar excepción si el texto no se puede convertir en un número entero
            Log.e("ERROR", "Error al convertir el número de likes a entero", e);
        }

        return currentLikes;
    }

    // ? Obtención de comentarios
    public interface OnCommentsReceivedListener {
        void onCommentsReceived(List<Comment> comments);
    }
    public void getCommentsFromFirebase(String postId, OnCommentsReceivedListener listener) {
        db.collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Comment> comments = new ArrayList<>();
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            String content = document.getString("content");
                            String uid = document.getString("uid");
                            long timestamp = Long.parseLong(document.getString("timestamp"));
                            String commentID = document.getId();

                            comments.add(new Comment(content, postId, commentID, uid, timestamp));
                        }
                        listener.onCommentsReceived(comments);
                    } else {
                        Log.d("GetCommentsError", "Error getting documents: ", task.getException());
                    }
                });
    }


}
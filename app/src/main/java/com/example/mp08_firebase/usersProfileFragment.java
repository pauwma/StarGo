package com.example.mp08_firebase;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class usersProfileFragment extends Fragment {

    private NavController navController;
    private ProgressBar progressBar;
    private ImageView photoImageView;
    // ? User Stats
    private TextView postsNumber, followersNumber, followingNumber, usernameTitleTextView;
    private String userUID, profileUID;
    private Query queryPosts;
    RecyclerView profilePostRecyclerView;
    public AppViewModel appViewModel;
    private FirebaseDatabase fDatabase;
    private FirebaseFirestore fStore;
    private FirebaseUser user;
    private DatabaseReference usersRef;
    private DatabaseReference userRef;

    // ? Botones de interacciones
    private Button followButton, followingButton, sendMessageButton;
    private LinearLayout followLayout, followingLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_users_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        navController = Navigation.findNavController(view);
        progressBar = view.findViewById(R.id.progressBar);

        fStore = FirebaseFirestore.getInstance();
        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        photoImageView = view.findViewById(R.id.photoImageView);
        usernameTitleTextView = view.findViewById(R.id.usernameTitleTextView);
        followersNumber = view.findViewById(R.id.followersNumber);
        followingNumber = view.findViewById(R.id.followingNumber);

        followButton = view.findViewById(R.id.followButton);
        view.findViewById(R.id.followButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followUser();
            }
        });

        followingButton = view.findViewById(R.id.followingButton);
        view.findViewById(R.id.followingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unfollowUser();
            }
        });

        sendMessageButton = view.findViewById(R.id.sendMessageButton);
        view.findViewById(R.id.sendMessageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendMessageToUser();
            }
        });

        followLayout = view.findViewById(R.id.followLayout);
        followingLayout = view.findViewById(R.id.followingLayout);

        appViewModel.postSeleccionado.observe(getViewLifecycleOwner(), post ->
        {
            profileUID = post.getUid();

            // ? Stats

            if (userUID != null && profileUID != null) {
                getFollowers().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int followersCount = task.getResult();
                        followersNumber.setText(String.valueOf(followersCount));
                    } else {
                        // Maneja el error
                    }
                });

                getFollowing().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int followingCount = task.getResult();
                        followingNumber.setText(String.valueOf(followingCount));
                    } else {
                        // Maneja el error
                    }
                });
            } else {
                // Maneja el caso en que userUID o profileUID sea nulo
            }


            // ? Comprobación de seguimiento
            isUserFollowing()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean isFollowing = task.getResult();
                            if (isFollowing) {
                                followingLayout.setVisibility(View.VISIBLE);
                                followLayout.setVisibility(View.GONE);
                            } else {
                                followingLayout.setVisibility(View.GONE);
                                followLayout.setVisibility(View.VISIBLE);
                            }
                        } else {
                            // Manejar el error
                            Log.e(TAG, "Error al verificar si el usuario sigue a otro: " + task.getException().getMessage());
                        }
                    });

            queryPosts = FirebaseFirestore.getInstance().collection("posts")
                    .whereEqualTo("uid", profileUID).limit(50).orderBy("date", Query.Direction.DESCENDING);

            // ? Lista de posts
            profilePostRecyclerView = view.findViewById(R.id.profilePostsRecyclerView);

            // Obtener referencia a la base de datos de Firebase
            fDatabase = FirebaseDatabase.getInstance();
            // Obtener referencia a la colección "users"
            usersRef = fDatabase.getReference("users");
            // Obtener referencia a los datos del usuario
            userRef = usersRef.child(profileUID);
            DocumentReference userRef = fStore.collection("users").document(profileUID);
            // ? Imagen del perfil del usuario
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String avatarUrl = document.getString("avatar");
                            if (avatarUrl != null) {
                                Glide.with(requireView()).load(avatarUrl).into(photoImageView);
                            }
                        }
                    }
                }
            });
        });

        // ? User Stats
        postsNumber = view.findViewById(R.id.postNumber);
        followersNumber = view.findViewById(R.id.followersNumber);
        followingNumber = view.findViewById(R.id.followingNumber);


        CollectionReference postsRef = fStore.collection("posts");
        Query queryPostsNumber = postsRef.whereEqualTo("uid", profileUID);
        queryPostsNumber.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int postCount = task.getResult().size();
                postsNumber.setText(String.valueOf(postCount));
            } else {
                // Error al obtener el número de posts del usuario.
            }
        });

        // ? Flecha Back
        view.findViewById(R.id.flechaBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.homeFragment);
            }
        });
    }

    private void followUser(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Agregar userToFollowUID a la lista de usuarios seguidos del usuario actual
        db.collection("users").document(userUID).collection("following").document(profileUID)
                .set(Collections.singletonMap("uid", profileUID));

        // Agregar currentUID a la lista de seguidores del usuario userToFollowUID
        db.collection("users").document(profileUID).collection("followers").document(userUID)
                .set(Collections.singletonMap("uid", userUID));

        followingLayout.setVisibility(View.VISIBLE);
        followLayout.setVisibility(View.GONE);

        // Incrementar el número de seguidores en 1
        updateFollowersCount(1);
    }

    private void unfollowUser(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

// Eliminar userToUnfollowUID de la lista de usuarios seguidos del usuario actual
        db.collection("users").document(userUID).collection("following").document(profileUID)
                .delete();

// Eliminar currentUID de la lista de seguidores del usuario userToUnfollowUID
        db.collection("users").document(profileUID).collection("followers").document(userUID)
                .delete();

        followingLayout.setVisibility(View.GONE);
        followLayout.setVisibility(View.VISIBLE);

        // Disminuir el número de seguidores en 1
        updateFollowersCount(-1);
    }

    private void updateFollowersCount(int change) {
        int currentCount = Integer.parseInt(followersNumber.getText().toString());
        int newCount = currentCount + change;
        followersNumber.setText(String.valueOf(newCount));
    }


    private Task<Boolean> isUserFollowing() {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Verificar si los UIDs no son nulos
        if (userUID == null || profileUID == null) {
            taskCompletionSource.setException(new IllegalArgumentException("UIDs must not be null."));
            return taskCompletionSource.getTask();
        }

        db.collection("users").document(userUID).collection("following").document(profileUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isFollowing = task.getResult().exists(); // Si el documento existe, el usuario actual sigue al usuario objetivo
                        taskCompletionSource.setResult(isFollowing);
                    } else {
                        // Manejar el error, si es necesario
                        taskCompletionSource.setException(task.getException());
                    }
                });

        return taskCompletionSource.getTask();
    }

    private Task<Integer> getFollowers() {
        TaskCompletionSource<Integer> taskCompletionSource = new TaskCompletionSource<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(profileUID).collection("followers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int followersCount = task.getResult().size();
                        taskCompletionSource.setResult(followersCount);
                    } else {
                        taskCompletionSource.setException(task.getException());
                    }
                });

        return taskCompletionSource.getTask();
    }

    private Task<Integer> getFollowing() {
        TaskCompletionSource<Integer> taskCompletionSource = new TaskCompletionSource<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(profileUID).collection("following")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int followingCount = task.getResult().size();
                        taskCompletionSource.setResult(followingCount);
                    } else {
                        taskCompletionSource.setException(task.getException());
                    }
                });

        return taskCompletionSource.getTask();
    }


    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

}
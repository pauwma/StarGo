package com.example.mp08_firebase;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.List;

public class ProfileFragment extends Fragment {

    private NavController navController;
    private String uid;
    private PostsAdapter adapter;
    private RecyclerView postsRecyclerView;
    private ConstraintLayout profileLayout;
    private ImageView photoImageView;
    private TextView displayNameTextView, descTextView;
    private TextView postsNumber, followersNumber, followingNumber, usernameTitleTextView;
    private ProgressBar progressBar;
    private FirebaseDatabase fDatabase;
    private FirebaseFirestore fStore;
    private FirebaseUser user;
    private DatabaseReference usersRef;
    private DatabaseReference userRef;


    public ProfileFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        user = FirebaseAuth.getInstance().getCurrentUser();
        fStore = FirebaseFirestore.getInstance();

        profileLayout = view.findViewById(R.id.profileLayout);

        photoImageView = view.findViewById(R.id.photoImageView);
        usernameTitleTextView = view.findViewById(R.id.usernameTitleTextView);
        displayNameTextView = view.findViewById(R.id.displayNameTextView);
        descTextView = view.findViewById(R.id.descTextView);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // ? User Stats
        postsNumber = view.findViewById(R.id.postNumber);
        followersNumber = view.findViewById(R.id.followersNumber);
        followingNumber = view.findViewById(R.id.followingNumber);

        uid = user.getUid();

        // ? Número de posts
        CollectionReference postsRef = fStore.collection("posts");
        Query queryPostsNumber = postsRef.whereEqualTo("uid", uid);
        queryPostsNumber.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int postCount = task.getResult().size();
                postsNumber.setText(String.valueOf(postCount));
            } else {
                // Error al obtener el número de posts del usuario.
                Log.e("ProfileFragment", "Error al obtener el número de posts del usuario", task.getException());
            }
        }).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d("ProfileFragment", "Posts recuperados correctamente: " + queryDocumentSnapshots.size());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ProfileFragment", "Error al recuperar los posts", e);
            }
        });


        getFollowers().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int followersCount = task.getResult();
                followersNumber.setText(String.valueOf(followersCount));
            } else {}
        });

        getFollowing().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int followingCount = task.getResult();
                followingNumber.setText(String.valueOf(followingCount));
            } else {}
        });

        fDatabase = FirebaseDatabase.getInstance();
        usersRef = fDatabase.getReference("users");
        userRef = usersRef.child(uid);
        DocumentReference userRef = fStore.collection("users").document(uid);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String username = document.getString("username");
                        usernameTitleTextView.setText("@" + username);

                        String avatarUrl = document.getString("avatar");
                        if (avatarUrl != null) {
                            Glide.with(requireView()).load(avatarUrl).into(photoImageView);
                        }

                        String displayName = document.getString("displayName");
                        if (displayName != null) {
                            displayNameTextView.setText(displayName);
                        } else {
                            displayNameTextView.setVisibility(View.GONE);
                        }

                        String description = document.getString("description");
                        if (description != null) {
                            descTextView.setText(description);
                        } else {
                            descTextView.setVisibility(View.GONE);
                        }

                        progressBar.setVisibility(View.GONE);
                        showViews();
                    }
                }
            }
        });

        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        setupRecyclerView(view);

        view.findViewById(R.id.settingsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.settingsFragment);
            }
        });

        view.findViewById(R.id.uploadButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.newPostFragment);
            }
        });

        view.findViewById(R.id.editProfileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.editarPerfilFragment);
            }
        });
    }

    private void showViews() {
        profileLayout.setVisibility(View.VISIBLE);
    }

    private Task<Integer> getFollowers() {
        TaskCompletionSource<Integer> taskCompletionSource = new TaskCompletionSource<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).collection("followers")
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

        db.collection("users").document(uid).collection("following")
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

    private void setupRecyclerView(View view) {
        // Asegúrate de tener una referencia al RecyclerView en el layout de tu fragmento
        RecyclerView recyclerView = view.findViewById(R.id.postsRecyclerView);

        // Establecer el LinearLayoutManager para el RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Crear una referencia a la colección de Firestore donde se almacenan los posts
        CollectionReference postsRef = FirebaseFirestore.getInstance().collection("posts");

        // Crear una consulta para obtener los posts que deseas mostrar
        // Crear una consulta para obtener los posts que deseas mostrar
        Query query = postsRef.whereEqualTo("uid", uid).orderBy("timestamp", Query.Direction.DESCENDING);

        // Crear opciones para el FirestoreRecyclerAdapter usando la consulta
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();

        // Asegúrate de tener una referencia al ProgressBar en el layout de tu fragmento
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        // Crear y configurar el adaptador
        PostsAdapter adapter = new PostsAdapter(getContext(), options, progressBar);

        // Establecer el adaptador para el RecyclerView
        recyclerView.setAdapter(adapter);

        // Iniciar la escucha de cambios en los datos
        adapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
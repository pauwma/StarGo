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
            profileUID = post.uid;

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

    class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostsAdapter.PostViewHolder> {
        public PostsAdapter(@NonNull FirestoreRecyclerOptions<Post> options) {super(options);}

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_post, parent, false));
        }

        @Override
        protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull final Post post) {
            Glide.with(getContext()).load(post.authorPhotoUrl).circleCrop().into(holder.authorPhotoImageView);
            holder.authorTextView.setText(post.author);
            holder.contentTextView.setText(post.content);
            Calendar now = Calendar.getInstance();
            Calendar postDate = Calendar.getInstance();
            postDate.setTime(post.date);
            long diff = now.getTimeInMillis() - postDate.getTimeInMillis();
            long diffHours = diff / (60 * 60 * 1000);
            if (diffHours < 24) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                String formattedDate = dateFormat.format(post.date);
                holder.timeTextView.setText(formattedDate + " h");
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
                String formattedDate = dateFormat.format(post.date);
                holder.timeTextView.setText(formattedDate);
            }

            // ? Gestion de likes
            final String postKey = getSnapshots().getSnapshot(position).getId();
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if(post.likes.containsKey(uid))
                holder.likeImageButton.setImageResource(R.drawable.like_solid_icon);
            else
                holder.likeImageButton.setImageResource(R.drawable.like_icon);
            holder.numLikesTextView.setText(String.valueOf(post.likes.size()));
            holder.likeImageButton.setOnClickListener(view -> {
                FirebaseFirestore.getInstance().collection("posts")
                        .document(postKey)
                        .update("likes."+uid, post.likes.containsKey(uid) ?
                                FieldValue.delete() : true);
            });

            // ? Miniatura de media
            if (post.mediaUrl != null) {
                holder.fadeUp.setVisibility(View.VISIBLE);
                holder.fadeDown.setVisibility(View.VISIBLE);
                holder.contentTextView.setText(post.content);
                holder.contentTextView.setAlpha(0f);
                holder.mediaImageView.setVisibility(View.VISIBLE);
                if ("audio".equals(post.mediaType)) {
                    Glide.with(requireView()).load(R.drawable.ic_baseline_audio_file_24).centerCrop().into(holder.mediaImageView);
                } else {
                    Glide.with(requireView()).load(post.mediaUrl).centerCrop().into(holder.mediaImageView);
                }
                holder.mediaImageView.setOnClickListener(view -> {
                    appViewModel.postSeleccionado.setValue(post);
                    navController.navigate(R.id.mediaFragment);
                });
                //holder.contentTextView.setPadding(0,0,0,6);
            } else {
                holder.mediaImageView.setVisibility(View.GONE);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(holder.constraintLayout);
                constraintSet.connect(holder.contentTextView.getId(), ConstraintSet.RIGHT, holder.actionsLayout.getId(), ConstraintSet.RIGHT);
                constraintSet.connect(holder.contentTextView.getId(), ConstraintSet.BOTTOM, holder.actionsLayout.getId(), ConstraintSet.TOP);
                constraintSet.connect(holder.contentTextView.getId(), ConstraintSet.TOP, holder.userInfo.getId(), ConstraintSet.BOTTOM);
                constraintSet.applyTo(holder.constraintLayout);

                holder.fadeUp.setVisibility(View.GONE);
                holder.fadeDown.setVisibility(View.GONE);
            }

        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout constraintLayout;
            ImageView authorPhotoImageView, mediaImageView;
            ImageButton likeImageButton, commentImageButton, shareImageButton;
            TextView authorTextView, contentTextView, numLikesTextView, timeTextView;
            View fadeUp, fadeDown;
            LinearLayout userInfo, actionsLayout;
            PostViewHolder(@NonNull View itemView) {
                super(itemView);
                constraintLayout = itemView.findViewById(R.id.constraintPost);
                authorPhotoImageView = itemView.findViewById(R.id.photoImageView);
                likeImageButton = itemView.findViewById(R.id.likeImageButton);
                commentImageButton = itemView.findViewById(R.id.commentImageButton);
                shareImageButton = itemView.findViewById(R.id.shareImageButton);
                mediaImageView = itemView.findViewById(R.id.mediaImage);
                authorTextView = itemView.findViewById(R.id.authorTextView);
                contentTextView = itemView.findViewById(R.id.contentTextView);
                numLikesTextView = itemView.findViewById(R.id.numLikesTextView);
                timeTextView = itemView.findViewById(R.id.timeTextView);
                userInfo = itemView.findViewById(R.id.userInfoLayout);
                actionsLayout = itemView.findViewById(R.id.actionsLayout);
                fadeUp = itemView.findViewById(R.id.fade_up);
                fadeDown = itemView.findViewById(R.id.fade_down);
            }
        }
    }
}
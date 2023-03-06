package com.example.mp08_firebase;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.transition.Hold;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ProfileFragment extends Fragment {

    NavController navController;
    ImageView photoImageView, deleteImageView;
    TextView displayNameTextView;
    String uid;
    public AppViewModel appViewModel;

    private FirebaseDatabase fDatabase;
    private FirebaseFirestore fStore;
    private FirebaseUser user;
    private DatabaseReference usersRef;
    private DatabaseReference userRef;

    // ? User Stats
    TextView postsNumber, followersNumber, followingNumber;

    public ProfileFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();
        fStore = FirebaseFirestore.getInstance();

        photoImageView = view.findViewById(R.id.photoImageView);
        displayNameTextView = view.findViewById(R.id.displayNameTextView);
        navController = Navigation.findNavController(view);

        // ? User Stats
        postsNumber = view.findViewById(R.id.postNumber);
        followersNumber = view.findViewById(R.id.followersNumber);
        followingNumber = view.findViewById(R.id.followingNumber);
        uid = user.getUid();

        if(user != null){
            displayNameTextView.setText(user.getDisplayName());
            // ! Glide.with(requireView()).load(user.getPhotoUrl()).into(photoImageView);
        }


        CollectionReference postsRef = fStore.collection("posts");
        Query queryPostsNumber = postsRef.whereEqualTo("uid", uid);
        queryPostsNumber.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int postCount = task.getResult().size();
                postsNumber.setText(String.valueOf(postCount));
            } else {
                // Error al obtener el número de posts del usuario.
            }
        });

        // Obtener referencia a la base de datos de Firebase
        fDatabase = FirebaseDatabase.getInstance();
        // Obtener referencia a la colección "users"
        usersRef = fDatabase.getReference("users");
        // Obtener referencia a los datos del usuario
        userRef = usersRef.child(uid);
        DocumentReference userRef = fStore.collection("users").document(uid);
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



        if(user.getPhotoUrl() == null){
            String[] userMailSplit = user.getEmail().split("@");
            displayNameTextView.setText(userMailSplit[0]);
            //Glide.with(requireView()).load(R.drawable.user_default_image).into(photoImageView);
        }


        // ? Lista de posts
        RecyclerView profilePostRecyclerView = view.findViewById(R.id.profilePostsRecyclerView);

        Query query = FirebaseFirestore.getInstance().collection("posts").whereEqualTo("uid", uid).limit(50).orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .setLifecycleOwner(this)
                .build();

        profilePostRecyclerView.setAdapter(new ProfileFragment.PostsAdapter(options));

        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);


        // ? Botón settings
        view.findViewById(R.id.settingsRueda).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.settingsFragment);
            }
        });

        // ? Botón editar
        view.findViewById(R.id.editTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.editarPerfilFragment);
            }
        });
    }

    class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostsAdapter.PostViewHolder> {
        public PostsAdapter(@NonNull FirestoreRecyclerOptions<Post> options) {super(options);}

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_post_profile, parent, false));
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
                holder.mediaImageView.setVisibility(View.VISIBLE);
                holder.contentTextView.setVisibility(View.GONE);
                if ("audio".equals(post.mediaType)) {
                    Glide.with(requireView()).load(R.drawable.ic_baseline_audio_file_24).centerCrop().into(holder.mediaImageView);
                } else {
                    Glide.with(requireView()).load(post.mediaUrl).centerCrop().into(holder.mediaImageView);
                }
                holder.mediaImageView.setOnClickListener(view -> {
                    appViewModel.postSeleccionado.setValue(post);
                    navController.navigate(R.id.mediaFragment);
                });
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

            deleteImageView = holder.deleteImageView;
            deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseFirestore.getInstance().collection("posts").document(postKey).delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(), "Post eliminado exitosamente", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Error al eliminar el post", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });

        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout constraintLayout;
            ImageView authorPhotoImageView, mediaImageView, deleteImageView;
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
                deleteImageView = itemView.findViewById(R.id.deleteImageView);
            }
        }
    }
}
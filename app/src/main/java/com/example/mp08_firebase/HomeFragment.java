package com.example.mp08_firebase;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    NavController navController;   // <-----------------
    public AppViewModel appViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);  // <-----------------

        view.findViewById(R.id.gotoNewPostFragmentButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.newPostFragment);
            }
        });

        RecyclerView postsRecyclerView = view.findViewById(R.id.postsRecyclerView);

        Query query = FirebaseFirestore.getInstance().collection("posts").limit(50).orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .setLifecycleOwner(this)
                .build();

        postsRecyclerView.setAdapter(new PostsAdapter(options));

        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
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
                holder.timeTextView.setText(formattedDate + " Hrs");
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
                String formattedDate = dateFormat.format(post.date);
                holder.timeTextView.setText(formattedDate);
            }

            // ? Gestion de likes
            final String postKey = getSnapshots().getSnapshot(position).getId();
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if(post.likes.containsKey(uid))
                holder.likeImageView.setImageResource(R.drawable.favorite_full);
            else
                holder.likeImageView.setImageResource(R.drawable.like_borde);
            holder.numLikesTextView.setText(String.valueOf(post.likes.size()));
            holder.likeImageView.setOnClickListener(view -> {
                FirebaseFirestore.getInstance().collection("posts")
                        .document(postKey)
                        .update("likes."+uid, post.likes.containsKey(uid) ?
                                FieldValue.delete() : true);
            });

            // ? Miniatura de media
            if (post.mediaUrl != null) {
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
            } else {
                holder.mediaImageView.setVisibility(View.GONE);
            }
        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            ImageView authorPhotoImageView, likeImageView, mediaImageView;
            TextView authorTextView, contentTextView, numLikesTextView, timeTextView;
            PostViewHolder(@NonNull View itemView) {
                super(itemView);
                authorPhotoImageView =
                        itemView.findViewById(R.id.photoImageView);
                likeImageView = itemView.findViewById(R.id.likeImageView);
                mediaImageView = itemView.findViewById(R.id.mediaImage);
                authorTextView = itemView.findViewById(R.id.authorTextView);
                contentTextView = itemView.findViewById(R.id.contentTextView);
                numLikesTextView = itemView.findViewById(R.id.numLikesTextView);
                timeTextView = itemView.findViewById(R.id.timeTextView);
            }
        }
    }

}
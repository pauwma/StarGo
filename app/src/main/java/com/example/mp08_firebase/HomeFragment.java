package com.example.mp08_firebase;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class HomeFragment extends Fragment implements PostsAdapter.OnPostClickListener {

    private NavController navController;
    private ProgressBar progressBar;
    private FloatingActionButton addButtonA, addButtonB;
    private FloatingActionsMenu fabMenu;
    private PostsAdapter adapter;

    public HomeFragment() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        RecyclerView postsRecyclerView = view.findViewById(R.id.postsRecyclerView);

        // ? Botones
        addButtonA = view.findViewById(R.id.action_a);
        addButtonB = view.findViewById(R.id.action_b);
        fabMenu = view.findViewById(R.id.multiple_actions);
        progressBar = view.findViewById(R.id.progressBar);

        view.findViewById(R.id.chatsImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.chatsHomeFragment);
            }
        });

        addButtonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.newPostFragment);
                fabMenu.collapse();
            }
        });

        addButtonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.chatAstraFragment);
                fabMenu.collapse();
            }
        });

        Query query = FirebaseFirestore.getInstance().collection("posts").limit(50).orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();

        adapter = new PostsAdapter(getContext(), options, progressBar, this);
        postsRecyclerView.setAdapter(adapter);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }


    @Override
    public void onPostClick(Post post) {
        Bundle bundle = new Bundle();
        bundle.putString("postId", post.getPostId());
        bundle.putString("uid", post.getUid());
        bundle.putString("content", post.getContent());
        bundle.putString("media", post.getMedia());
        bundle.putString("mediaType", post.getMediaType());
        bundle.putString("timestamp", String.valueOf(post.getTimestamp()));
        navController.navigate(R.id.mediaFragment, bundle);
    }
}
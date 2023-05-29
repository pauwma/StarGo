package com.example.mp08_firebase;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mp08_firebase.items.Post;
import com.example.mp08_firebase.items.PostsAdapter;
import com.example.mp08_firebase.items.User;
import com.example.mp08_firebase.items.UsersAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BuscadorFragment extends Fragment implements UsersAdapter.OnUserClickListener, PostsAdapter.OnPostClickListener {

    private NavController navController;
    private EditText searchEditText;
    private RecyclerView searchUsersRecyclerView, postsRecyclerView, postsSearchedRecyclerView;
    private UsersAdapter usersAdapter;
    private View separacionSearchUsers;
    private TextView searchResultsLabel;
    private ProgressBar progressBar;
    private PostsAdapter adapter, searchedPostsAdapter;

    // ? Delay de searchUsers
    private final long DEBOUNCE_DELAY = 300; // 300 milliseconds
    private Handler searchHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buscador, container, false);

        searchEditText = view.findViewById(R.id.searchEditText);
        searchUsersRecyclerView = view.findViewById(R.id.searchUsersRecyclerView);
        separacionSearchUsers = view.findViewById(R.id.viewSearchUsers);
        searchResultsLabel = view.findViewById(R.id.searchResultsLabel);
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        postsSearchedRecyclerView = view.findViewById(R.id.postsSearchedRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        com.google.firebase.firestore.Query query = FirebaseFirestore.getInstance().collection("posts").limit(30).orderBy("postId", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();

        adapter = new PostsAdapter(getContext(), options, progressBar, this);
        postsRecyclerView.setAdapter(adapter);
        adapter.startListening();
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchedPostsAdapter = new PostsAdapter(getContext(), options, progressBar, this);
        postsSearchedRecyclerView.setAdapter(searchedPostsAdapter);
        searchedPostsAdapter.startListening();
        postsSearchedRecyclerView.setVisibility(View.VISIBLE);
        postsRecyclerView.setVisibility(View.GONE);

        setupSearchUsers();

        return view;
    }

    private void setupSearchUsers() {
        usersAdapter = new UsersAdapter(this);
        searchUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchUsersRecyclerView.setAdapter(usersAdapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacksAndMessages(null);
                if (s.length() > 0 && s.charAt(0) == '#') {
                    searchHandler.postDelayed(() -> searchPosts(s.toString().substring(1)), DEBOUNCE_DELAY);
                } else {
                    searchHandler.postDelayed(() -> searchUsers(s.toString()), DEBOUNCE_DELAY);
                    postsRecyclerView.setVisibility(View.VISIBLE);
                    postsSearchedRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void searchUsers(String query) {
        // ? No realizar la búsqueda si el contenido está vacío
        if (query.isEmpty()) {
            usersAdapter.setUsers(new ArrayList<>());
            searchUsersRecyclerView.setVisibility(View.GONE);
            searchResultsLabel.setVisibility(View.GONE);
            separacionSearchUsers.setVisibility(View.GONE);
            return;
        }

        String lowerCaseQuery = query.toLowerCase(); // ? Convierte la consulta a minúsculas

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // Obtiene el ID del usuario actual
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .orderBy("username")
                .whereGreaterThanOrEqualTo("username", lowerCaseQuery)
                .whereLessThanOrEqualTo("username", lowerCaseQuery + "\uf8ff")
                .limit(3)
                .get()
                .addOnCompleteListener(task -> {
                    List<User> userList = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String uid = document.getString("uid");

                            // Comprueba si el uid del documento es igual al uid del usuario actual
                            if (uid.equals(currentUserId)) {
                                continue;  // Si es el usuario actual, salta este documento
                            }

                            String username = document.getString("username");
                            String displayName = document.getString("displayName");
                            if (displayName == null ||displayName.isEmpty()){
                                displayName = username;
                            }
                            String profileImageUrl = document.getString("avatar");

                            if (username != null && profileImageUrl != null) {
                                User user = new User(uid, username,profileImageUrl,displayName);
                                userList.add(user);
                            }
                        }
                    }

                    // Verifica si se encontraron usuarios
                    if (userList.isEmpty()) {
                        // No se encontraron usuarios
                        searchResultsLabel.setText("No se han encontrado usuarios");
                        searchResultsLabel.setTextColor(ContextCompat.getColor(requireContext(),R.color.quick_silver));
                        searchResultsLabel.setVisibility(View.VISIBLE);
                    } else {
                        // Se encontraron usuarios
                        usersAdapter.setUsers(userList);
                        searchUsersRecyclerView.setVisibility(View.VISIBLE);
                        searchResultsLabel.setTextColor(ContextCompat.getColor(requireContext(),R.color.white));
                        searchResultsLabel.setText("Resultados de la búsqueda");
                        searchResultsLabel.setVisibility(View.VISIBLE);
                        separacionSearchUsers.setVisibility(View.VISIBLE);
                    }
                });
    }
    private void searchPosts(String tag) {
        // Aquí va tu lógica para buscar posts por etiqueta
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .get()
                .addOnCompleteListener(task -> {
                    List<String> postIds = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Aquí va tu lógica para crear un objeto Post a partir del documento
                            Post post = document.toObject(Post.class);
                            // Filtrar los posts en el cliente
                            List<String> hashtags = post.getHashtags();
                            for (String hashtag : hashtags) {
                                if (hashtag.toLowerCase().contains(tag.toLowerCase())) {
                                    postIds.add(post.getPostId());
                                    break;
                                }
                            }
                        }
                    }

                    if (postIds.isEmpty()) {
                        // No se encontraron posts
                        searchResultsLabel.setText("No se han encontrado posts");
                        searchResultsLabel.setTextColor(ContextCompat.getColor(requireContext(),R.color.quick_silver));
                        searchResultsLabel.setVisibility(View.VISIBLE);
                        postsSearchedRecyclerView.setVisibility(View.GONE);
                    } else {
                        // Se encontraron posts
                        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                                .setQuery(db.collection("posts").whereIn("postId", postIds), Post.class)
                                .build();

                        searchedPostsAdapter = new PostsAdapter(getContext(), options, progressBar, this);
                        postsSearchedRecyclerView.setAdapter(searchedPostsAdapter);
                        searchedPostsAdapter.startListening();
                        postsSearchedRecyclerView.setVisibility(View.VISIBLE);
                        searchResultsLabel.setTextColor(ContextCompat.getColor(requireContext(),R.color.white));
                        searchResultsLabel.setText("Resultados de la búsqueda");
                        searchResultsLabel.setVisibility(View.VISIBLE);
                        postsRecyclerView.setVisibility(View.GONE);
                    }
                });
    }

    public void onUserClick(User otherUser) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUserId = otherUser.getUid();

        goToProfile(getView(), otherUser.getUid());

        searchEditText.setText(null);
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
package com.example.mp08_firebase;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ChatsHomeFragment extends Fragment implements UsersAdapter.OnUserClickListener, ChatsAdapter.OnChatClickListener {

    NavController navController;

    private EditText searchUsersEditText;
    private RecyclerView searchUsersRecyclerView;
    private View separacionSearchUsers;
    private RecyclerView chatsRecyclerView;
    private UsersAdapter usersAdapter;
    private ChatsAdapter chatsAdapter;

    private TextView searchResultsLabel;

    // ? Delay de searchUsers
    private final long DEBOUNCE_DELAY = 300; // 300 milliseconds
    private Handler searchHandler = new Handler();

    public ChatsHomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // ? Flecha Back
        view.findViewById(R.id.flechaBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats_home, container, false);

        searchUsersEditText = view.findViewById(R.id.search_users);
        searchUsersRecyclerView = view.findViewById(R.id.search_users_recyclerview);
        separacionSearchUsers = view.findViewById(R.id.viewSearchUsers);
        chatsRecyclerView = view.findViewById(R.id.chats_recyclerview);
        searchResultsLabel = view.findViewById(R.id.search_results_label);

        setupSearchUsers();
        setupChatsList();

        return view;
    }



    private void setupSearchUsers() {
        usersAdapter = new UsersAdapter(this);
        searchUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchUsersRecyclerView.setAdapter(usersAdapter);

        searchUsersEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacksAndMessages(null);
                searchHandler.postDelayed(() -> searchUsers(s.toString()), DEBOUNCE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupChatsList() {
        chatsAdapter = new ChatsAdapter(this);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsRecyclerView.setAdapter(chatsAdapter);

        loadChats();
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

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .orderBy("username")
                .whereGreaterThanOrEqualTo("username", lowerCaseQuery)
                .whereLessThanOrEqualTo("username", lowerCaseQuery + "\uf8ff")
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    List<User> userList = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String username = document.getString("username");
                            String profileImageUrl = document.getString("avatar");

                            if (username != null && profileImageUrl != null) {
                                User user = new User(username, profileImageUrl);
                                userList.add(user);
                            }
                        }
                    }

                    usersAdapter.setUsers(userList);
                    searchUsersRecyclerView.setVisibility(View.VISIBLE);
                    searchResultsLabel.setVisibility(View.VISIBLE);
                    separacionSearchUsers.setVisibility(View.VISIBLE);
                });
    }

    @Override
    public void onUserClick(User user) {
        // Crear un nuevo chat con el usuario seleccionado y navegar a la conversación
        createNewChat(user);
    }

    @Override
    public void onChatClick(Chat chat) {
        // Navegar al fragmento o actividad que muestra los mensajes del chat seleccionado
        navigateToChatMessages(chat);
    }


    private void loadChats() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Asume que el UID del usuario actual está almacenado en una variable llamada currentUserId
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        db.collection("chats")
                .whereArrayContains("users", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ChatsHomeFragment", "Error loading chats", error);
                        return;
                    }

                    List<Chat> chats = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Chat chat = document.toObject(Chat.class);
                            chat.setChatId(document.getId());

                            // Añadir la URL de la imagen de perfil del otro usuario al objeto Chat
                            for (String userId : chat.getUsers()) {
                                if (!userId.equals(currentUserId)) {
                                    db.collection("users")
                                            .document(userId)
                                            .get()
                                            .addOnSuccessListener(userDoc -> {
                                                String profileImageUrl = userDoc.getString("avatar");
                                                chat.setOtherUserProfileImageUrl(profileImageUrl);

                                                // Notificar al adaptador que los datos han cambiado
                                                chatsAdapter.notifyDataSetChanged();
                                            });
                                    break;
                                }
                            }

                            chats.add(chat);
                        }
                    }

                    chatsAdapter.setChats(chats);
                });
    }


    private void createNewChat(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Obtén el ID del usuario seleccionado a partir de su nombre de usuario
        db.collection("users")
                .whereEqualTo("username", user.getUsername())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        String selectedUserId = task.getResult().getDocuments().get(0).getId();

                        // Crear un nuevo documento de chat en Firestore
                        List<String> participants = Arrays.asList(currentUserId, selectedUserId);
                        Chat newChat = new Chat(null, "Chat with " + user.getUsername(), participants);

                        db.collection("chats")
                                .add(newChat)
                                .addOnSuccessListener(documentReference -> {
                                    String chatId = documentReference.getId();
                                    newChat.setChatId(chatId); // Establece el ID del chat en el objeto Chat
                                    // Navegar a la conversación
                                    navigateToChatMessages(newChat);
                                })
                                .addOnFailureListener(e -> Log.e("ChatsHomeFragment", "Error creating chat", e));
                    } else {
                        Log.e("ChatsHomeFragment", "Error finding selected user");
                    }
                });
    }


    private void navigateToChatMessages(Chat chat) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("selected_chat", chat);
        navController.navigate(R.id.action_chatsHomeFragment_to_chatFragment, bundle);
    }


}
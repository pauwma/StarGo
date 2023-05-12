package com.example.mp08_firebase;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatsHomeFragment extends Fragment implements UsersAdapter.OnUserClickListener, ChatsAdapter.OnChatClickListener {

    private NavController navController;
    private EditText searchUsersEditText;
    private RecyclerView searchUsersRecyclerView;
    private View separacionSearchUsers;
    private RecyclerView chatsRecyclerView;
    private UsersAdapter usersAdapter;
    private ChatsAdapter chatsAdapter;
    private  ProgressBar loadingChatsProgressBar;
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
        loadingChatsProgressBar = view.findViewById(R.id.loading_chats_progressbar);

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

    public void onUserClick(User otherUser) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUserId = otherUser.getUid();

        // Combinar los ID de usuario en orden alfabético
        String userPair;
        if (currentUserId.compareTo(otherUserId) < 0) {
            userPair = currentUserId + "_" + otherUserId;
        } else {
            userPair = otherUserId + "_" + currentUserId;
        }

        // Consulta la colección de chats
        FirebaseFirestore.getInstance().collection("chats")
                .whereEqualTo("userPair", userPair)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        if (documents.size() > 0) {
                            // Si la lista de documentos no está vacía, significa que ya existe un chat
                            // Obtiene el primer chat de la lista
                            Chat existingChat = documents.get(0).toObject(Chat.class);
                            // Navega al chat existente
                            navigateToChatMessages(existingChat);
                        } else {
                            createNewChat(otherUser);
                        }
                    } else {
                        // Maneja el error
                    }
                });
        searchUsersEditText.setText(null);
    }
    @Override
    public void onChatClick(Chat chat) {
        navigateToChatMessages(chat);
    }
    private void loadChats() {
        loadingChatsProgressBar.setVisibility(View.VISIBLE); // Muestra el ProgressBar
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        db.collection("chats")
                .whereArrayContains("users", currentUserId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING) // Ordenar los chats por el timestamp del último mensaje
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
                            chats.add(chat);
                        }
                    }

                    chatsAdapter.setChats(chats);
                    loadingChatsProgressBar.setVisibility(View.GONE); // Oculta el ProgressBar
                    chatsRecyclerView.setVisibility(View.VISIBLE); // Muestra el RecyclerView de los chats
                });
    }
    private void createNewChat(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Obtenemos directamente el ID del usuario seleccionado
        String selectedUserId = user.getUid();

        // Crear un nuevo documento de chat en Firestore
        List<String> participants = Arrays.asList(currentUserId, selectedUserId);
        long timestamp = System.currentTimeMillis();
        Chat newChat = new Chat(null, participants, timestamp);

        // Buscar un chat existente con los mismos participantes
        db.collection("chats")
                .whereArrayContains("users", currentUserId)
                .get()
                .addOnCompleteListener(task1 -> {
                    boolean chatExists = false;
                    if (task1.isSuccessful() && task1.getResult() != null && !task1.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot documentSnapshot : task1.getResult()) {
                            Chat chat = documentSnapshot.toObject(Chat.class);
                            if (chat != null && chat.getUsers().contains(selectedUserId)) {
                                chat.setChatId(documentSnapshot.getId());
                                navigateToChatMessages(chat);
                                chatExists = true;
                                break;
                            }
                        }
                    }

                    if (!chatExists) {
                        // Si no existe un chat, crea uno nuevo
                        db.collection("chats")
                                .add(newChat)
                                .addOnSuccessListener(documentReference -> {
                                    String chatId = documentReference.getId();
                                    newChat.setChatId(chatId); // Establece el ID del chat en el objeto Chat

                                    // Actualizar el documento de chat con el nuevo chatId
                                    db.collection("chats").document(chatId)
                                            .update("chatId", chatId)
                                            .addOnSuccessListener(aVoid -> {
                                                // Navegar a la conversación
                                                navigateToChatMessages(newChat);
                                            })
                                            .addOnFailureListener(e -> Log.e("ChatsHomeFragment", "Error updating chatId", e));
                                })
                                .addOnFailureListener(e -> Log.e("ChatsHomeFragment", "Error creating chat", e));
                    }
                });
    }
    private void navigateToChatMessages(Chat chat) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("selected_chat", chat);
        navController.navigate(R.id.action_chatsHomeFragment_to_chatFragment, bundle);
    }
}
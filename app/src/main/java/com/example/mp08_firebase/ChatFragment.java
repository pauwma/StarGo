package com.example.mp08_firebase;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    private NavController navController;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendMessageButton;
    private MessagesAdapter messagesAdapter;
    private FirebaseFirestore db;
    private TextView titleUsername;
    private CircleImageView userImage;
    private Chat chat;

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messagesRecyclerView = view.findViewById(R.id.messages_recyclerview);
        messageInput = view.findViewById(R.id.message_input);
        sendMessageButton = view.findViewById(R.id.send_message_button);
        db = FirebaseFirestore.getInstance();

        titleUsername = view.findViewById(R.id.titleUsername);
        userImage = view.findViewById(R.id.userImage);

        setupMessagesList();

        if (getArguments() != null) {
            chat = (Chat) getArguments().getSerializable("selected_chat");
        }

        loadChatUserDetails();

        sendMessageButton.setOnClickListener(v -> {
            String messageContent = messageInput.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                sendMessage(messageContent);
                messageInput.setText("");
            }
        });

        // ? Flecha según contenido
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    sendMessageButton.setAlpha(0.2f);
                } else {
                    sendMessageButton.setAlpha(1.0f);
                }
            }
        });

        loadMessages();

        return view;
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

    private void setupMessagesList() {
        messagesAdapter = new MessagesAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messagesAdapter);
    }

    private void loadMessages() {
        String chatId = chat.getChatId();
        CollectionReference messagesRef = db.collection("chats").document(chatId).collection("messages");

        messagesRef.orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ChatFragment", "Error loading messages", error);
                        return;
                    }

                    List<Message> messages = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Message message = document.toObject(Message.class);
                            messages.add(message);
                        }
                    }

                    messagesAdapter.setMessages(messages);
                });
    }

    private void loadChatUserDetails() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String chatUserId = chat.getUsers().stream().filter(id -> !id.equals(currentUserId)).findFirst().orElse(null);

        if (chatUserId == null) {
            Log.e(TAG, "Unable to find chat user ID");
            return;
        }

        db.collection("users")
                .document(chatUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User chatUser = documentSnapshot.toObject(User.class);

                    if (chatUser != null) {
                        titleUsername.setText(chatUser.getUsername());

                        if (chatUser.getAvatar() != null && !chatUser.getAvatar().isEmpty()) {
                            Glide.with(requireContext())
                                    .load(chatUser.getAvatar())
                                    .into(userImage);
                        }
                    } else {
                        Log.e(TAG, "Unable to find chat user in Firestore");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching chat user details", e));
    }
    private void sendMessage(String messageContent) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();
        String messageId = generateMessageId();

        Message newMessage = new Message(messageId, messageContent, currentUserId, timestamp);
        List<Message> messages = messagesAdapter.getMessages();
        messages.add(newMessage);
        messagesAdapter.setMessages(messages);
        messagesRecyclerView.scrollToPosition(messages.size() - 1);

        // Aquí puedes guardar el mensaje en tu base de datos o servicio de backend
        saveMessageToFirestore(messageId, newMessage);
    }
    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }
    private void saveMessageToFirestore(String messageId, Message message) {
        // Reemplaza "chatId" por el ID del chat en el que estás trabajando
        String chatId = chat.getChatId();

        // Crear una referencia a la subcolección de mensajes en el documento de chat
        CollectionReference messagesRef = db.collection("chats").document(chatId).collection("messages");

        // Guardar el mensaje en Firestore
        messagesRef.document(messageId).set(message)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Mensaje guardado con éxito");
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al guardar el mensaje", e);
                });
    }

}
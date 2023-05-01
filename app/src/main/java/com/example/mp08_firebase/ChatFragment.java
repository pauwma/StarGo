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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatFragment extends Fragment {

    private NavController navController;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private Button sendMessageButton;
    private MessagesAdapter messagesAdapter;
    private FirebaseFirestore db;
    private TextView titleProfile;
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

        setupMessagesList();

        if (getArguments() != null) {
            chat = (Chat) getArguments().getSerializable("selected_chat");
        }

        sendMessageButton.setOnClickListener(v -> {
            String messageContent = messageInput.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                sendMessage(messageContent);
                messageInput.setText("");
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
package com.example.mp08_firebase;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.example.mp08_firebase.BuildConfig;


public class ChatAstraFragment extends Fragment {

    private NavController navController;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendMessageButton;
    private MessagesAdapter messagesAdapter;
    private String currentUserId;
    private OkHttpClient client;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private String apiKey = BuildConfig.OPEN_AI_API_KEY;
    private List<Message> conversationHistory = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_astra, container, false);
        client = new OkHttpClient();
        messagesRecyclerView = view.findViewById(R.id.messages_recyclerview);
        messageInput = view.findViewById(R.id.message_input);
        sendMessageButton = view.findViewById(R.id.send_message_button);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupMessagesList();

        sendMessageButton.setOnClickListener(v -> {
            String messageContent = messageInput.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                sendMessage(messageContent);
                callAPI(messageContent); // Llama a la API con el contenido del mensaje del usuario
                messageInput.setText("");
                sendMessageButton.setEnabled(false); // Desactiva el botón de enviar
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

        String typingMessage = "Escribiendo... ";
        messagesAdapter.getMessages().add(new Message(generateMessageId(), typingMessage, "astra", System.currentTimeMillis()));
        messagesAdapter.notifyDataSetChanged();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Elimina el mensaje de "está escribiendo..."
                messagesAdapter.getMessages().remove(messagesAdapter.getMessages().size() - 1);

                String startingMessage = "Hola, soy Astra, tu asistente personal de viajes. Estoy aquí para proporcionarte recomendaciones y ayudarte a planificar tu próximo viaje. ¿Cómo puedo asistirte hoy?";
                messagesAdapter.getMessages().add(new Message(generateMessageId(), startingMessage, "astra", System.currentTimeMillis()));
                messagesAdapter.notifyDataSetChanged();
            }
        }, 500);
    }
    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }
    private void sendMessage(String messageContent) {
        if (conversationHistory.size() >= 10) {
            Toast.makeText(getContext(), "Límite de 5 mensajes enviados.", Toast.LENGTH_LONG).show();
            return;
        }

        long timestamp = System.currentTimeMillis();
        String messageId = generateMessageId();

        Message newMessage = new Message(messageId, messageContent, currentUserId, timestamp);
        List<Message> messages = messagesAdapter.getMessages();
        messages.add(newMessage);
        messagesAdapter.setMessages(messages);
        messagesRecyclerView.scrollToPosition(messages.size() - 1);

        conversationHistory.add(newMessage);
    }
    private void callAPI(String question){
        long timestamp = System.currentTimeMillis();
        messagesAdapter.getMessages().add(new Message(generateMessageId(),"Escribiendo... ", "astra", timestamp));

        JSONArray jsonArray = new JSONArray();
        for (Message message : conversationHistory) {
            JSONObject jsonMessage = new JSONObject();
            try {
                jsonMessage.put("role", message.getSenderId().equals(currentUserId) ? "user" : "assistant");
                jsonMessage.put("content", message.getContent());
                jsonArray.put(jsonMessage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model","gpt-3.5-turbo");
            jsonBody.put("messages", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization","Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to "+e.getMessage(), "astra");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    JSONObject  jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                        addResponse(result.trim(), "astra");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    String errorString = response.body() != null ? response.body().string() : "No details provided";
                    addResponse("Failed to load response due to " + errorString, "astra");
                }
            }
        });
    }
    private void addResponse(String response, String senderId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messagesAdapter.getMessages().remove(messagesAdapter.getMessages().size()-1);
                Message newMessage = new Message(UUID.randomUUID().toString(), response, senderId, System.currentTimeMillis());
                List<Message> messages = messagesAdapter.getMessages();
                messages.add(newMessage);
                messagesAdapter.setMessages(messages);
                messagesRecyclerView.scrollToPosition(messages.size() - 1);
                sendMessageButton.setEnabled(true);
            }
        });
    }
}
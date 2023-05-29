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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.mp08_firebase.items.Message;
import com.example.mp08_firebase.items.MessagesAdapter;
import com.example.mp08_firebase.items.Trip;
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
import java.util.concurrent.TimeUnit;


public class ChatAstraFragment extends Fragment {

    private NavController navController;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendMessageButton;
    private MessagesAdapter messagesAdapter;
    private String currentUserId;
    private OkHttpClient client;
    private Trip trip;
    private boolean firstMessage;
    private String firstMessageContent;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private String apiKey = BuildConfig.OPEN_AI_API_KEY;
    private List<Message> conversationHistory = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_astra, container, false);
        client = new OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        messagesRecyclerView = view.findViewById(R.id.messages_recyclerview);
        messageInput = view.findViewById(R.id.message_input);
        sendMessageButton = view.findViewById(R.id.send_message_button);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupMessagesList();

        if (getArguments() != null){
            trip = getArguments().getParcelable("trip");
        }

        sendMessageButton.setOnClickListener(v -> {
            String messageContent = messageInput.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                sendMessage(messageContent);
                messageInput.setText("");
                sendMessageButton.setEnabled(false);
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
                messagesAdapter.getMessages().remove(messagesAdapter.getMessages().size() - 1);

                String startingMessage = "Astra, tu asistente personal de viajes, está listo para recomendarte y asistirte en la planificación de tu viaje. ¿Cómo puedo ayudarte? ✨😊";
                messagesAdapter.getMessages().add(new Message(generateMessageId(), startingMessage, "astra", System.currentTimeMillis()));
                messagesAdapter.notifyDataSetChanged();
            }
        }, 500);
    }
    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }
    private void sendMessage(String messageContent) {
        if (conversationHistory.size() >= 5) {
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

        if (firstMessageContent == null) {
            firstMessageContent = "Eres Astra, mi asistente de viajes espaciales en StarGO. Aquí está la información del viaje: "
                    + "Nombre: " + trip.getName() + ", "
                    + "Descripción: " + trip.getDescription() + ", "
                    + "Nave: " + trip.getSpacecraft() + ", "
                    + "Descripción de la nave: " + trip.getSpacecraftDescription() + ". "
                    + messageContent;
        }

        callAPI(messageContent);
    }

    private void callAPI(String question) {
        Log.d("API_DEBUG", "callAPI method started");
        long timestamp = System.currentTimeMillis();
        messagesAdapter.getMessages().add(new Message(generateMessageId(), "Escribiendo...", "astra", timestamp));

        JSONArray jsonArray = new JSONArray();
        JSONObject firstMessageJson = new JSONObject();
        try {
            firstMessageJson.put("role", "assistant");
            firstMessageJson.put("content", firstMessageContent);
            jsonArray.put(firstMessageJson);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("API_DEBUG", "JSONException while creating firstMessageJson: " + e.getMessage());
        }

        for (Message message : conversationHistory) {
            JSONObject jsonMessage = new JSONObject();
            try {
                jsonMessage.put("role", message.getSenderId().equals(currentUserId) ? "user" : "assistant");
                String tripInfo = " Eres Astra, mi asistente de viajes espaciales en StarGO, da respuestas breves y concisas y solo relacionadas con el viaje. Aquí está la información del viaje: "
                        + "Nombre: " + trip.getName() + ", "
                        + "Descripción: " + trip.getDescription() + ", "
                        + "Nave: " + trip.getSpacecraft() + ", "
                        + "Descripción de la nave: " + trip.getSpacecraftDescription() + ". ";
                if (firstMessage == false){
                    jsonMessage.put("content", question + tripInfo);
                    firstMessage = true;
                } else {
                    jsonMessage.put("content", message.getContent());
                }
                jsonArray.put(jsonMessage);
                Log.d("API_DEBUG", "Message sent to API: " + jsonMessage.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("API_DEBUG", "JSONException while creating jsonMessage: " + e.getMessage());
            }
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-4");
            jsonBody.put("messages", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("API_DEBUG", "JSONException while creating jsonBody: " + e.getMessage());
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        Log.d("API_DEBUG", "Request built, about to make the API call");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to " + e.getMessage(), "astra");
                Log.d("API_DEBUG", "API call failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("API_DEBUG", "Received response from API call");
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                        addResponse(result.trim(), "astra");
                        Log.d("API_DEBUG", "Response from API: " + result.trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("API_DEBUG", "JSONException while parsing API response: " + e.getMessage());
                    }
                } else {
                    String errorString = response.body() != null ? response.body().string() : "No details provided";
                    addResponse("Failed to load response due to " + errorString, "astra");
                    Log.d("API_DEBUG", "API response was not successful: " + errorString);
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
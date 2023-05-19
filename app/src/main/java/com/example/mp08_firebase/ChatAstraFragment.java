package com.example.mp08_firebase;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;

public class ChatAstraFragment extends Fragment {

    private NavController navController;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendMessageButton;
    private MessagesAdapter messagesAdapter;
    private TextView titleUsername;
    private CircleImageView userImage;
    private String currentUserId;
    private OkHttpClient client;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        client = new OkHttpClient();
        messagesRecyclerView = view.findViewById(R.id.messages_recyclerview);
        messageInput = view.findViewById(R.id.message_input);
        sendMessageButton = view.findViewById(R.id.send_message_button);
        titleUsername = view.findViewById(R.id.titleUsername);
        userImage = view.findViewById(R.id.userImage);
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
    }


    private void sendMessage(String messageContent) {
        long timestamp = System.currentTimeMillis();
        String messageId = generateMessageId();

        Message newMessage = new Message(messageId, messageContent, currentUserId, timestamp);
        List<Message> messages = messagesAdapter.getMessages();
        messages.add(newMessage);
        messagesAdapter.setMessages(messages);
        messagesRecyclerView.scrollToPosition(messages.size() - 1);
    }
    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }

    private void callAPI(String question){
        long timestamp = System.currentTimeMillis();
        messagesAdapter.getMessages().add(new Message(generateMessageId(),"Typing... ", "astra", timestamp));

        JSONArray jsonArray = new JSONArray();
        JSONObject message = new JSONObject();
        try {
            message.put("role", "user");
            message.put("content", question);
            jsonArray.put(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model","gpt-4");
            jsonBody.put("messages", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization","Bearer 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")
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
            }
        });
    }
}
package com.example.mp08_firebase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Request;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AvatarGenerationFragment extends Fragment {

    private NavController navController;
    private ProgressBar progressBar;
    private ImageView mediaImageView;
    private String imageUrl;
    private EditText promptEditText;
    private TextView promptTextView;
    private Button generateButton;
    private ImageButton eraseButton, acceptButton;
    private OkHttpClient client;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private String apiKey = BuildConfig.IMAGE_API_KEY;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_avatar_generation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        client = new OkHttpClient().newBuilder().callTimeout(30, TimeUnit.SECONDS).build();

        progressBar = view.findViewById(R.id.progressBar);
        mediaImageView = view.findViewById(R.id.mediaImageView);
        promptTextView = view.findViewById(R.id.promptTextView);
        promptEditText = view.findViewById(R.id.promptEditText);
        generateButton = view.findViewById(R.id.generateButton);
        eraseButton = view.findViewById(R.id.eraseButton);
        acceptButton = view.findViewById(R.id.acceptButton);

        if (getArguments() != null) {
            String avatarUrl = getArguments().getString("avatarUrl");
            Glide.with(requireView()).load(avatarUrl).into(mediaImageView);
        }

        promptEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(256)});
        promptEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                CharacterCountCircleView characterCountCircleView = view.findViewById(R.id.characterCountCircleView);
                characterCountCircleView.setCharacterCount(s.length());
                checkCommentButtonStatus();
            }
        });
        promptEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(promptEditText, InputMethodManager.SHOW_IMPLICIT);

        generateButton = view.findViewById(R.id.generateButton);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prompt = promptEditText.getText().toString().trim();
                if (!prompt.isEmpty()) {
                    generateImage(prompt);
                    promptTextView.setText("Generando imagen...");
                    progressBar.setVisibility(View.VISIBLE);
                    mediaImageView.setAlpha(0.7f);
                    promptEditText.setText("");
                    checkCommentButtonStatus();
                    // Quitar el foco del EditText
                    promptEditText.clearFocus();
                    promptEditText.setFocusable(false);
                    promptEditText.setFocusableInTouchMode(false);
                    // Ocultar el teclado
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(promptEditText.getWindowToken(), 0);

                }
            }
        });


        eraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptEditText.setText("");
                eraseButton.setAlpha(1f);
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("avatarUrl", imageUrl);
                navController.navigate(R.id.editarPerfilFragment, bundle);
            }
        });

        // ? Cruz Back
        view.findViewById(R.id.cruzBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });
    }

    private void checkCommentButtonStatus() {
        String postContent = promptEditText.getText().toString().trim();
        if (!postContent.isEmpty()) {
            generateButton.setBackgroundResource(R.drawable.button_purple);
            eraseButton.setAlpha(1f);
        } else {
            generateButton.setBackgroundResource(R.drawable.button_border_purple);
            eraseButton.setAlpha(0.7f);
        }
    }

    private void generateImage(String prompt) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("text", prompt);
            jsonBody.put("num_images", 1);
            jsonBody.put("resolution", "512x512");
            jsonBody.put("providers", "stabilityai");
            jsonBody.put("response_as_dict", true);
            jsonBody.put("attributes_as_list", false);
            jsonBody.put("show_original_response", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.edenai.run/v2/image/generation")
                .header("Authorization", "Bearer " + apiKey) // Reemplace 'apiKey' con su clave de API de EdenAI
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Handle the error
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // The response will be a URL of the generated image.
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONObject stabilityaiResponse = jsonObject.getJSONObject("stabilityai");
                        JSONArray items = stabilityaiResponse.getJSONArray("items");
                        imageUrl = items.getJSONObject(0).getString("image_resource_url");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                mediaImageView.setAlpha(1f);
                                promptTextView.setText(prompt);
                                promptEditText.setFocusable(true);
                                promptEditText.setFocusableInTouchMode(true);
                                Glide.with(requireView()).load(imageUrl).into(mediaImageView);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle the error
                }
            }
        });
    }


}
package com.example.mp08_firebase;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.mp08_firebase.items.Post;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewPostFragment extends Fragment {

    Button publishButton;
    EditText postConentEditText;
    NavController navController;
    public AppViewModel appViewModel;
    Uri mediaUri;
    String mediaTipo;
    private EditText editText;
    private ImageView mediaImageView;
    private VideoView mediaVideoView;
    private ImageButton clearMediaButton;
    private MaterialCardView mediaCardView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_post, container, false);
        EditText editText = (EditText) rootView.findViewById(R.id.postContentEditText);
        editText.requestFocus();
        // Mostrar el teclado virtual
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        mediaImageView = view.findViewById(R.id.mediaImageView);
        mediaVideoView = view.findViewById(R.id.mediaVideoView);
        mediaCardView = view.findViewById(R.id.mediaCardView);

        publishButton = view.findViewById(R.id.publishButton);
        postConentEditText = view.findViewById(R.id.postContentEditText);
        postConentEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(postConentEditText, InputMethodManager.SHOW_IMPLICIT);

        // Asegurarse de que todos los controles de la imagen estén ocultos inicialmente
        mediaImageView.setVisibility(View.GONE);
        mediaVideoView.setVisibility(View.GONE);
        mediaCardView.setVisibility(View.GONE);


        postConentEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(256)});
        postConentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                CharacterCountCircleView characterCountCircleView = view.findViewById(R.id.characterCountCircleView);
                characterCountCircleView.setCharacterCount(s.length());
                checkPublishButtonStatus();
            }
        });

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publish();
            }
        });

        // ? Cruz Back
        view.findViewById(R.id.cruzBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearImageAndMediaSeleccionado();
                navController.navigateUp();
            }
        });

        clearMediaButton = view.findViewById(R.id.clearMediaButton);
        clearMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearImageAndMediaSeleccionado();
            }
        });

        view.findViewById(R.id.camera_fotos).setOnClickListener(v -> tomarFoto());
        view.findViewById(R.id.camera_videos).setOnClickListener(v -> tomarVideo());
        view.findViewById(R.id.imagen_galeria).setOnClickListener(v -> seleccionarImagen());
        view.findViewById(R.id.video_galeria).setOnClickListener(v -> seleccionarVideo());
        appViewModel.mediaSeleccionado.observe(getViewLifecycleOwner(), media -> {
            this.mediaUri = media.uri;
            this.mediaTipo = media.tipo;

            if (mediaTipo == null || mediaUri == null){
                mediaImageView.setVisibility(View.GONE);
                mediaVideoView.setVisibility(View.GONE);
                mediaCardView.setVisibility(View.GONE);
                return;
            }

            if (mediaTipo.equals("image")) {
                // Cargar la imagen usando Glide
                Glide.with(this).load(media.uri).into(mediaImageView);

                // Mostrar la ImageView y esconder la VideoView
                mediaImageView.setVisibility(View.VISIBLE);
                mediaVideoView.setVisibility(View.GONE);
            } else if (mediaTipo.equals("video")) {
                // Cargar y reproducir el video
                mediaVideoView.setVideoURI(media.uri);
                mediaVideoView.start();

                // Mostrar la VideoView y esconder la ImageView
                mediaVideoView.setVisibility(View.VISIBLE);
                mediaImageView.setVisibility(View.GONE);
            } else {
                // En caso de que el tipo de medio no sea ni imagen ni video
                mediaImageView.setVisibility(View.GONE);
                mediaVideoView.setVisibility(View.GONE);
            }

            clearMediaButton.setVisibility(View.VISIBLE);
            mediaCardView.setVisibility(View.VISIBLE);
            checkPublishButtonStatus();
        });

    }

    private List<String> extractHashtags(String content) {
        List<String> hashtags = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?:^|\\s)#(\\w+)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            hashtags.add(matcher.group(1));
        }
        return hashtags;
    }
    private void publish() {
        hideKeyboard();
        if (!validPost()) {
            return;
        }
        postUpload();
    }
    private boolean validPost() {
        String postContent = postConentEditText.getText().toString().trim();
        if (postContent.isEmpty() && mediaUri == null) {
            return false;
        }
        return true;
    }
    private void postUpload() {
        publishButton.setEnabled(false);
        String postContent = postConentEditText.getText().toString().trim();
        if (mediaUri != null) {
            mediaToFirestore(postContent);
        } else {
            postToFirestore(postContent, null, null);
        }
    }
    private void postToFirestore(String postContent, String mediaUrl, String mediaType) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // (String uid, long timestamp, String media, String mediaType, String content, List<String> hashtags)
        Post post = new Post(user.getUid(), System.currentTimeMillis(), mediaUrl, mediaType, postContent, extractHashtags(postContent));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String postId = documentReference.getId();
                        post.setPostId(postId);
                        db.collection("posts").document(postId).set(post)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        publishButton.setEnabled(true);
                                        navController.popBackStack();
                                        clearImageAndMediaSeleccionado();
                                    }
                                });
                    }
                });
    }
    private void mediaToFirestore(String postContent) {
        FirebaseStorage.getInstance().getReference(mediaTipo + "/" + UUID.randomUUID())
                .putFile(mediaUri)
                .continueWithTask(task -> task.getResult().getStorage().getDownloadUrl())
                .addOnSuccessListener(url -> postToFirestore(postContent, url.toString(), mediaTipo));
    }
    private final ActivityResultLauncher<String> galeria = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            appViewModel.setMediaSeleccionado(uri, mediaTipo);
        }
    });
    private final ActivityResultLauncher<Uri> camaraFotos = registerForActivityResult(new ActivityResultContracts.TakePicture(), isSuccess -> {
        if (isSuccess) {
            appViewModel.setMediaSeleccionado(mediaUri, "image");
        }
    });
    private final ActivityResultLauncher<Uri> camaraVideos; {
        camaraVideos = registerForActivityResult(new ActivityResultContracts.TakeVideo(), uri -> {
            if (uri != null) {
                appViewModel.setMediaSeleccionado(mediaUri, "video");
            }
        });
    }
    private void seleccionarImagen() {
        mediaTipo = "image";
        galeria.launch("image/*");
    }
    private void seleccionarVideo() {
        mediaTipo = "video";
        galeria.launch("video/*");
    }
    private void tomarFoto() {
        try {
            mediaUri = FileProvider.getUriForFile(requireContext(),
                    BuildConfig.APPLICATION_ID + ".fileprovider", File.createTempFile("img",
                            ".jpg",
                            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)));
            camaraFotos.launch(mediaUri);
        } catch (IOException e) {}
    }
    private void tomarVideo() {
        try {
            mediaUri = FileProvider.getUriForFile(requireContext(),
                    BuildConfig.APPLICATION_ID + ".fileprovider", File.createTempFile("vid",
                            ".mp4",
                            requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES)));
            camaraVideos.launch(mediaUri);
        } catch (IOException e) {}
    }
    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void clearImageAndMediaSeleccionado() {
        // Cambia el valor de mediaSeleccionado
        appViewModel.setMediaSeleccionado(null, null);
        // Limpia la imagen en ImageView
        mediaCardView.setVisibility(View.GONE);
        mediaImageView.setVisibility(View.GONE);
        mediaImageView.setImageDrawable(null);
        mediaVideoView.setVisibility(View.GONE);
        mediaVideoView.stopPlayback();
        clearMediaButton.setVisibility(View.GONE);
        // Limpiar la imagen usando Glide
        Glide.with(this).clear(mediaImageView);
        checkPublishButtonStatus();

    }
    private void checkPublishButtonStatus() {
        String postContent = postConentEditText.getText().toString().trim();
        if (!postContent.isEmpty() || mediaUri != null) {
            publishButton.setBackgroundResource(R.drawable.button_purple);
        } else {
            publishButton.setBackgroundResource(R.drawable.button_border_purple);
        }
    }

}
package com.example.mp08_firebase;

import static android.content.ContentValues.TAG;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MediaFragment extends Fragment {

    private NavController navController;
    private ImageButton crossImageButton;

    // ? Post & User info
    private String postID, postUID, content, media, mediaType, timestamp;
    private TextView displayNameTextView, usernameTextView, contentTextView, timestampTextView;
    private CircleImageView profile_image;
    private ImageView mediaImageView;
    private ConstraintLayout userLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        displayNameTextView = view.findViewById(R.id.displayNameTextView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        contentTextView = view.findViewById(R.id.contentTextView);
        timestampTextView = view.findViewById(R.id.timestampTextView);
        profile_image = view.findViewById(R.id.profile_image);
        mediaImageView = view.findViewById(R.id.mediaImageView);
        userLayout = view.findViewById(R.id.userLayout);

        if (getArguments() != null) {
            postID = getArguments().getString("postId");
            postUID = getArguments().getString("uid");
            content = getArguments().getString("content");
            media = getArguments().getString("media");
            mediaType = getArguments().getString("mediaType");
            timestamp = getArguments().getString("timestamp");
        }

        // ? Content
        if (content != null && !content.trim().equals("")){
            contentTextView.setText(content);
            contentTextView.setVisibility(View.VISIBLE);
        } else {
            contentTextView.setVisibility(View.GONE);
        }

        // ? Media
        if (mediaType != null && !mediaType.isEmpty()){
            if (mediaType.equals("image") && media != null){
                int dpValue = 20;
                float density = getContext().getResources().getDisplayMetrics().density;
                int radius = (int) (dpValue * density);
                Glide.with(getContext()).load(media)
                        .transform(new RoundedCorners(radius))
                        .into(mediaImageView);
                mediaImageView.setVisibility(View.VISIBLE);
            } else {
                mediaImageView.setVisibility(View.GONE);
            }
        } else {
            mediaImageView.setVisibility(View.GONE);
        }

        // ? Timestamp
        timestampTextView.setText(formatDate(Long.parseLong(timestamp)));

        userInfo();

        // ? Perfil del usuario
        userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfile(v, postUID);
            }
        });

        // ? Flecha Back
        view.findViewById(R.id.crossImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });

    }

    public void postInfo(){

    }

    public void userInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(postUID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String username = document.getString("username");
                        String displayName = document.getString("displayName");
                        String avatarURL = document.getString("avatar");

                        usernameTextView.setText("@" + username);
                        displayNameTextView.setText(displayName);
                        Glide.with(requireView()).load(avatarURL).into(profile_image);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public String formatDate(long timestamp) {
        // Convertir timestamp a Date
        Date date = new Date(timestamp);

        // Crear el formato de fecha
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a '·' d MMM. yyyy", new Locale("es", "ES"));

        // Formatear la fecha
        return sdf.format(date);
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
}
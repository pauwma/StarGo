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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MediaFragment extends Fragment {

    private NavController navController;
    private ImageButton crossImageButton;

    // ? Post & User info
    private String postID, postUID, content, media, mediaType, timestamp;
    private TextView displayNameTextView, usernameTextView;
    private CircleImageView profile_image;

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
        profile_image = view.findViewById(R.id.profile_image);

        if (getArguments() != null) {
            postID = getArguments().getString("postId");
            postUID = getArguments().getString("uid");
            content = getArguments().getString("content");
            media = getArguments().getString("media");
            mediaType = getArguments().getString("mediaType");
            timestamp = getArguments().getString("timestamp");
        }

        userInfo();

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


}
package com.example.mp08_firebase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

public class MediaFragment extends Fragment {
    VideoView videoView;
    ImageView imageView, photoImageView;
    ImageButton crossButton;
    TextView authorTextView, timeTextView;
    public AppViewModel appViewModel;
    NavController navController;
    String userUID;
    Post post;
    FirebaseFirestore fStore;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        fStore = FirebaseFirestore.getInstance();
        navController = Navigation.findNavController(view);

        imageView = view.findViewById(R.id.imageView);
        videoView = view.findViewById(R.id.videoView);
        photoImageView = view.findViewById(R.id.photoImageView);
        authorTextView = view.findViewById(R.id.authorTextView);
        timeTextView = view.findViewById(R.id.timeTextView);
        crossButton = view.findViewById(R.id.crossImageButton);
        userUID = Objects.requireNonNull(appViewModel.postSeleccionado.getValue()).uid;

        // ? Flecha Back
        crossButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });



        appViewModel.postSeleccionado.observe(getViewLifecycleOwner(), post ->
        {
            this.post = post;
            userUID = post.uid;
            if ("video".equals(post.mediaType) ||
                    "audio".equals(post.mediaType)) {
                MediaController mc = new MediaController(requireContext());
                mc.setAnchorView(videoView);
                videoView.setMediaController(mc);
                videoView.setVideoPath(post.mediaUrl);
                videoView.start();
            } else if ("image".equals(post.mediaType)) {
                Glide.with(requireView()).load(post.mediaUrl).into(imageView);
            }
            Calendar now = Calendar.getInstance();
            Calendar postDate = Calendar.getInstance();
            postDate.setTime(post.date);
            long diff = now.getTimeInMillis() - postDate.getTimeInMillis();
            long diffHours = diff / (60 * 60 * 1000);
            if (diffHours < 24) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                String formattedDate = dateFormat.format(post.date);
                timeTextView.setText(formattedDate + " h");
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
                String formattedDate = dateFormat.format(post.date);
                timeTextView.setText(formattedDate);
            }

            Glide.with(getContext()).load(post.authorPhotoUrl).circleCrop().into(photoImageView);
            authorTextView.setText(post.author);
        });

        // ? Perfil del usuario
        view.findViewById(R.id.userInfoLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post.uid.equals(FirebaseAuth.getInstance().getUid())){
                    navController.navigate(R.id.profileFragment);
                } else {
                    appViewModel.postSeleccionado.setValue(post);
                    navController.navigate(R.id.usersProfileFragment);
                }
            }
        });
    }

}
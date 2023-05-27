package com.example.mp08_firebase;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.mp08_firebase.items.Comment;
import com.example.mp08_firebase.items.CommentAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MediaFragment extends Fragment {

    private NavController navController;
    private ImageButton crossImageButton;

    // ? Post & User info
    private String postID, postUID, content, media, mediaType, timestamp, userUID;
    private int currentLikes;
    private boolean commentButtonPressed;
    private TextView displayNameTextView, usernameTextView, contentTextView, timestampTextView, likesNumTextView, commentsNumTextView;
    private EditText commentEditText;
    private CircleImageView profile_image;
    private ImageView mediaImageView;
    private ImageButton likeButton, commentButton, eraseButton;
    private Button newCommentButton;
    private ConstraintLayout userLayout, likeLayout, commentLayout, commentUtilsLayout;
    private RecyclerView commentsRecyclerView;
    private FirebaseFirestore db;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener;


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
        likesNumTextView = view.findViewById(R.id.likesNumTextView);
        commentsNumTextView = view.findViewById(R.id.commentsNumTextView);
        likeLayout = view.findViewById(R.id.likeLayout);
        commentLayout = view.findViewById(R.id.commentLayout);
        likeButton = view.findViewById(R.id.likeButton);
        commentButton = view.findViewById(R.id.commentButton);
        commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView);
        commentEditText = view.findViewById(R.id.newCommentEditText);
        commentEditText.clearComposingText();
        newCommentButton = view.findViewById(R.id.newCommentButton);
        commentUtilsLayout = view.findViewById(R.id.commentUtilsLayout);
        eraseButton = view.findViewById(R.id.eraseButton);
        db = FirebaseFirestore.getInstance();

        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (getArguments() != null) {

            postID = getArguments().getString("postId");
            postUID = getArguments().getString("uid");
            content = getArguments().getString("content");
            media = getArguments().getString("media");
            mediaType = getArguments().getString("mediaType");
            timestamp = getArguments().getString("timestamp");
            commentButtonPressed = getArguments().getBoolean("commentButton");
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

        eraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentEditText.setText("");
                eraseButton.setAlpha(1f);
            }
        });
        newCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCommentToPost();
            }
        });

        // ? Imagen
        mediaImageView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("imageUrl", media);
            navController.navigate(R.id.detailedImageFragment, bundle);
        });

        // ? Verificar si el usuario actual ha dado "like" a la publicación
        FirebaseFirestore.getInstance().collection("posts").document(postID).collection("likes").document(userUID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Si el usuario ha dado "like", cambiar el ícono a "like_solid_icon"
                    likeButton.setImageResource(R.drawable.heart_solid);
                    likeButton.setAlpha(1f);
                    likesNumTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.morado));
                } else {
                    // Si el usuario no ha dado "like", cambiar el ícono a "like_icon"
                    likeButton.setImageResource(R.drawable.heart_regular);
                    likesNumTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                    likesNumTextView.setAlpha(0.7f);
                }
            }
        });

        // ? Establecer el número de "likes"
        FirebaseFirestore.getInstance().collection("posts").document(postID).collection("likes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@android.support.annotation.NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int likesCount = task.getResult().size();
                    likesNumTextView.setText(String.valueOf(likesCount));
                } else {
                    likesNumTextView.setText("0");
                }
            }
        });
        likeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Agregar o eliminar "like" en Firestore
                FirebaseFirestore.getInstance().collection("posts").document(postID).collection("likes").document(userUID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // Verificar si el texto actual es un número válido
                        try {
                            currentLikes = Integer.parseInt(likesNumTextView.getText().toString());
                        } catch (NumberFormatException e) {
                            // Si no es un número válido, establecer el contador de "likes" en 0
                            currentLikes = 0;
                        }

                        if (documentSnapshot.exists()) {
                            // Si el usuario ya ha dado "like", eliminar el "like"
                            FirebaseFirestore.getInstance().collection("posts").document(postID).collection("likes").document(userUID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Actualizar el botón y el contador de "likes"
                                    likeButton.setImageResource(R.drawable.heart_regular);
                                    likeButton.setAlpha(0.7f);
                                    likesNumTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                                    likesNumTextView.setAlpha(0.7f);
                                    likesNumTextView.setText(String.valueOf(getCurrentLikes() - 1));
                                }
                            });


                        } else {
                            // Si el usuario no ha dado "like", agregar un nuevo "like"
                            FirebaseFirestore.getInstance().collection("posts").document(postID).collection("likes").document(userUID).set(Collections.singletonMap("timestamp", FieldValue.serverTimestamp())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Actualizar el botón y el contador de "likes"
                                    likeButton.setImageResource(R.drawable.heart_solid);
                                    likeButton.setAlpha(1f);
                                    likesNumTextView.setText(String.valueOf(getCurrentLikes() + 1));
                                    likesNumTextView.setAlpha(1f);
                                    likesNumTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.morado));
                                }
                            });
                        }
                    }
                });
            }
        });

        // ? Establecer el número de "comments"
        FirebaseFirestore.getInstance().collection("posts").document(postID).collection("comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@android.support.annotation.NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int likesCount = task.getResult().size();
                    if (likesCount <= 0){
                        commentsNumTextView.setText("0");
                    } else {
                        commentsNumTextView.setText(String.valueOf(likesCount));
                    }
                } else {
                    commentsNumTextView.setText("0");
                }
            }
        });
        commentsRecyclerView.setNestedScrollingEnabled(false);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (commentsRecyclerView != null) {
            getCommentsFromFirebase(postID, new OnCommentsReceivedListener() {
                @Override
                public void onCommentsReceived(List<Comment> comments) {
                    Log.d("getCommentsFromFirebase", "Received " + comments.size() + " comments");
                    CommentAdapter adapter = new CommentAdapter(getContext(), comments);
                    commentsRecyclerView.setAdapter(adapter);
                }
            });
        } else {
            Log.d("getCommentsFromFirebase", "commentsRecyclerView is null");
        }

        commentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(commentEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        if (commentButtonPressed){
            commentEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(commentEditText, InputMethodManager.SHOW_IMPLICIT);
        }
        commentEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(256)});
        commentEditText.addTextChangedListener(new TextWatcher() {
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
        commentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    commentUtilsLayout.setVisibility(View.VISIBLE);
                } else {
                    commentUtilsLayout.setVisibility(View.GONE);
                }
            }
        });

        final View activityRootView = view.getRootView();
        final boolean[] wasKeyboardOpen = new boolean[1]; // Nuevo estado para rastrear si el teclado estaba abierto

        keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Comprobar si el Fragment todavía está adjunto a su Activity
                if (!isAdded()) {
                    return;
                }

                int heightDiff = activityRootView.getHeight() - view.getHeight();
                if (heightDiff > dpToPx(getActivity(), 200)) { // Aquí se usa el contexto de la actividad
                    wasKeyboardOpen[0] = true; // Actualizar el estado a que el teclado está abierto
                } else {
                    if (wasKeyboardOpen[0] && commentEditText.hasFocus()) { // Solo si el teclado estaba abierto antes
                        commentEditText.clearFocus();
                    }
                    wasKeyboardOpen[0] = false; // Actualizar el estado a que el teclado está cerrado
                }
            }
        };
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
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
    public int getCurrentLikes() {
        String currentLikesText = likesNumTextView.getText().toString();
        int currentLikes = 0;

        try {
            currentLikes = Integer.parseInt(currentLikesText);
        } catch (NumberFormatException e) {
            // Manejar excepción si el texto no se puede convertir en un número entero
            Log.e("ERROR", "Error al convertir el número de likes a entero", e);
        }

        return currentLikes;
    }

    // ? Obtención de comentarios
    public interface OnCommentsReceivedListener {
        void onCommentsReceived(List<Comment> comments);
    }
    public void getCommentsFromFirebase(String postId, OnCommentsReceivedListener listener) {
        db.collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Comment> comments = new ArrayList<>();
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            String content = document.getString("content");
                            String uid = document.getString("uid");
                            long timestamp = Long.parseLong(document.getString("timestamp"));
                            String commentID = document.getId();

                            comments.add(new Comment(content, postId, commentID, uid, timestamp));
                        }
                        listener.onCommentsReceived(comments);
                    } else {
                        Log.d("GetCommentsError", "Error getting documents: ", task.getException());
                    }
                });
    }

    // ? Creación de comentario
    private void checkCommentButtonStatus() {
        String postContent = commentEditText.getText().toString().trim();
        if (!postContent.isEmpty()) {
            newCommentButton.setBackgroundResource(R.drawable.button_purple);
            eraseButton.setAlpha(1f);
        } else {
            newCommentButton.setBackgroundResource(R.drawable.button_border_purple);
            eraseButton.setAlpha(0.7f);
        }
    }
    public void addCommentToPost() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Crear un Map para el comentario
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("content", commentEditText.getText().toString());
        commentData.put("uid", userUID);
        commentData.put("postID", postID);
        // Obtener el timestamp actual como un long
        long timestamp = System.currentTimeMillis();
        commentData.put("timestamp", String.valueOf(timestamp));

        // Añadir el comentario a la subcolección "comments" del post correspondiente
        // Usa add() en lugar de set() para generar un ID de documento único automáticamente
        db.collection("posts").document(postID).collection("comments").add(commentData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Comment successfully written!");

                        // Aquí tienes el ID del comentario
                        String commentID = documentReference.getId();

                        // Actualiza el documento con el commentID
                        documentReference.update("commentID", commentID)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully updated with commentID!");
                                        commentEditText.setText(null);
                                        hideKeyboard();
                                        commentEditText.clearFocus();
                                        String tmpCommentNum = String.valueOf(Integer.valueOf(commentsNumTextView.getText().toString()) + 1);
                                        commentsNumTextView.setText(tmpCommentNum);
                                        getCommentsFromFirebase(postID, new OnCommentsReceivedListener() {
                                            @Override
                                            public void onCommentsReceived(List<Comment> comments) {
                                                CommentAdapter adapter = new CommentAdapter(getContext(), comments);
                                                commentsRecyclerView.setAdapter(adapter);
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error updating document", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing comment", e);
                    }
                });
    }

    // Convertir dp a pixels
    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cuando la vista se destruye, se elimina el OnGlobalLayoutListener para evitar fugas de memoria
        if (getView() != null) {
            getView().getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
        }
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
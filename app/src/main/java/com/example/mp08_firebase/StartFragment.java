package com.example.mp08_firebase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StartFragment extends Fragment {

    private NavController navController;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Button signInButton, createAccountButton, googleSignInButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;

    public StartFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        signInButton = view.findViewById(R.id.signInButton);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.signInFragment);
            }
        });

        createAccountButton = view.findViewById(R.id.createAccountButton);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.registerFragment);
            }
        });

        googleSignInButton = view.findViewById(R.id.googleButton);
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            try {
                                firebaseAuthWithGoogle(GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class));
                            } catch (ApiException e) {
                                Log.e("ABCD", "signInResult:failed code=" +
                                        e.getStatusCode());
                            }
                        }
                    }
                });
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accederConGoogle();
            }
        });
    }

    private void accederConGoogle() {
        GoogleSignInClient googleSignInClient =
                GoogleSignIn.getClient(requireActivity(), new
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build());
        activityResultLauncher.launch(googleSignInClient.getSignInIntent());
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        if (acct == null) return;

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users")
                                    .whereEqualTo("uid", user.getUid())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                                if (documents.size() > 0) {
                                                    actualizarUI(user);
                                                } else {
                                                    String userUID = mAuth.getCurrentUser().getUid(); // Obtiene el UID del usuario creado.

                                                    generateUniqueUsername(Objects.requireNonNull(user.getDisplayName()), db, new OnUniqueUsernameCompleteListener() {
                                                        @Override
                                                        public void onComplete(String uniqueUsername) {
                                                            DocumentReference documentReference = fStore.collection("users").document(userUID); // Crea un documento en la colección "users" con el UID.
                                                            Map<String,Object> userAdd = new HashMap<>();
                                                            userAdd.put("uid",userUID);
                                                            userAdd.put("username", uniqueUsername);
                                                            userAdd.put("email",user.getEmail());
                                                            userAdd.put("avatar",user.getPhotoUrl());
                                                            userAdd.put("displayName", user.getDisplayName());
                                                            documentReference.set(userAdd);
                                                            actualizarUI(mAuth.getCurrentUser());
                                                        }
                                                    });
                                                }
                                            } else {
                                                Log.e("ABCD", "Error getting documents: ", task.getException());
                                            }
                                        }
                                    });
                        } else {
                            Log.e("ABCD", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }


    private String validateAndTransformUsername(String username) {
        username = username.trim().toLowerCase();
        if (username.length() > 32) {
            username = username.substring(0, 32);
        }
        username = username.replaceAll("\\s+", "");

        return username;
    }

    private void generateUniqueUsername(String displayName, FirebaseFirestore db, OnUniqueUsernameCompleteListener onCompleteListener) {
        String transformedUsername = displayName.trim().toLowerCase().replaceAll("\\s+", "_").substring(0, Math.min(displayName.length(), 32));

        checkUsernameExists(transformedUsername, db, onCompleteListener, 1);
    }


    private void checkUsernameExists(String username, FirebaseFirestore db, OnUniqueUsernameCompleteListener onCompleteListener, int attempt) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            if (documents.size() > 0) {
                                checkUsernameExists(username + attempt, db, onCompleteListener, attempt + 1);
                            } else {
                                onCompleteListener.onComplete(username);
                            }
                        } else {
                            onCompleteListener.onComplete(username);
                        }
                    }
                });
    }

    private void actualizarUI(FirebaseUser currentUser) {
        if(currentUser != null){
            navController.navigate(R.id.homeFragment);
        }
    }

    public interface OnUniqueUsernameCompleteListener {
        void onComplete(String uniqueUsername);
    }

}
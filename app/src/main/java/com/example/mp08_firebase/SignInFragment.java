package com.example.mp08_firebase;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
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

public class SignInFragment extends Fragment {

    NavController navController;
    private SignInButton googleSignInButton;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private EditText emailEditText, passwordEditText;
    private Typeface originalTypeface;
    private ImageButton showPasswordButton, emailSignInButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        emailEditText = view.findViewById(R.id.emailEditText);
        emailSignInButton = view.findViewById(R.id.emailSignInButton);

        passwordEditText = view.findViewById(R.id.passwordEditText);
        showPasswordButton = view.findViewById(R.id.showPasswordButton);
        originalTypeface = passwordEditText.getTypeface();

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // ? Botón crear cuenta
        view.findViewById(R.id.gotoCreateAccountTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.registerFragment);
            }
        });

        //? Contraseña olvidada
        view.findViewById(R.id.contraTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.recuperacionFragment);
            }
        });


        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (passwordEditText.getText().toString().isEmpty()) {
                    passwordEditText.setTypeface(originalTypeface);
                    showPasswordButton.setVisibility(View.GONE);
                } else {
                    showPasswordButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        if(passwordEditText.getText().toString().isEmpty()){
            passwordEditText.setTypeface(originalTypeface);
            showPasswordButton.setVisibility(View.GONE);
        }
        showPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passwordEditText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showPasswordButton.setImageResource(R.drawable.ocultar_icon);
                } else {
                    passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showPasswordButton.setImageResource(R.drawable.ocultar_no_icon);
                }
            }
        });

        emailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accederConEmail();
            }
        });

        googleSignInButton = view.findViewById(R.id.googleSignInButton);
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
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
        if(acct == null) return;

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
                                                    DocumentReference documentReference = fStore.collection("users").document(userUID); // Crea un documento en la colección "users" con el UID.
                                                    Map<String,Object> userAdd = new HashMap<>();
                                                    userAdd.put("uid",userUID);
                                                    userAdd.put("username", Objects.requireNonNull(user.getDisplayName()).trim());
                                                    userAdd.put("email",user.getEmail());
                                                    userAdd.put("avatar",user.getPhotoUrl());
                                                    try {
                                                        userAdd.put("phone",user.getPhoneNumber());
                                                    } catch (Exception e){}
                                                    documentReference.set(userAdd);
                                                    actualizarUI(mAuth.getCurrentUser());
                                                }
                                            } else {
                                            }
                                        }
                                    });
                        } else {
                        }
                    }
                });
    }



    private void actualizarUI(FirebaseUser currentUser) {
        if(currentUser != null){
            navController.navigate(R.id.homeFragment);
        }
    }

    private void accederConEmail() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            Snackbar.make(getView(), "Por favor ingrese su email y contraseña", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Verificar si el correo electrónico ya está registrado en Firebase
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();
                        if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                            // El correo electrónico ya está registrado, proceder con el inicio de sesión
                            mAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(requireActivity(), task2 -> {
                                        if (task2.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            // El inicio de sesión fue exitoso
                                        } else {
                                            Snackbar.make(getView(), "Error al iniciar sesión", Snackbar.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // El correo electrónico no está registrado en Firebase
                            Toast.makeText(getContext(), "El usuario no está registrado", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make(getView(), "Error al verificar el correo electrónico", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false);

    }

}
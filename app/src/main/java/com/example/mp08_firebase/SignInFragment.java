package com.example.mp08_firebase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
    private NavController navController;
    private EditText userInfoEditText, passwordEditText;
    private Button nextButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        userInfoEditText = view.findViewById(R.id.userInfoEditText);
        userInfoEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(userInfoEditText, InputMethodManager.SHOW_IMPLICIT);
        passwordEditText = view.findViewById(R.id.passwordEditText);

        // ? Cambios de botón de siguiente
        CustomTextWatcher textWatcher = new CustomTextWatcher();

        userInfoEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);

        nextButton = view.findViewById(R.id.nextButton);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        //? Contraseña olvidada
        view.findViewById(R.id.passwordButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.recuperacionFragment);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceder();
            }
        });

        // ? Flecha Back
        view.findViewById(R.id.flechaBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });
    }

    private void actualizarUI(FirebaseUser currentUser) {
        if(currentUser != null){
            navController.navigate(R.id.homeFragment);
        }
    }

    private void acceder() {
        String email = userInfoEditText.getText().toString();
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
                                            actualizarUI(user);
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

    private class CustomTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            updateButtonBackground();
        }
    }

    private void updateButtonBackground() {
        boolean allFieldsFilled = !TextUtils.isEmpty(userInfoEditText.getText()) &&
                !TextUtils.isEmpty(passwordEditText.getText());

        if (allFieldsFilled) {
            nextButton.setBackgroundResource(R.drawable.button_purple);
        } else {
            nextButton.setBackgroundResource(R.drawable.button_border_purple);
        }
    }
}
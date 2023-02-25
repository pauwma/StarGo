package com.example.mp08_firebase;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    NavController navController;
    private String username, email, phone, password, userUID;
    private EditText emailEditText, passwordEditText, usernameEditText, phoneEditText;
    private ImageButton registerButton, showPasswordButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private Typeface originalTypeface;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        usernameEditText = view.findViewById(R.id.usernameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        showPasswordButton = view.findViewById(R.id.showPasswordButton);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // ? Botón de registrar
        registerButton = view.findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crearCuenta();
            }
        });

        // ? Función de mostrar contraseña
        originalTypeface = passwordEditText.getTypeface();
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

    }

    private void crearCuenta() {
        if (!validarFormulario()) {
            return;
        }

        registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            userUID = mAuth.getCurrentUser().getUid(); // Obtiene el UID del usuario creado.
                            DocumentReference documentReference = fStore.collection("users").document(userUID); // Crea un documento en la colección "users" con el UID.
                            Map<String,Object> user = new HashMap<>();
                            user.put("username",username);
                            user.put("email",email);
                            user.put("phone",phone);
                            documentReference.set(user);
                            actualizarUI(mAuth.getCurrentUser());
                        } else {
                            Snackbar.make(requireView(), "Error: " + task.getException(), Snackbar.LENGTH_LONG).show();

                        }
                        registerButton.setEnabled(true);
                    }
                });

    }

    private void actualizarUI(FirebaseUser currentUser) {
        if(currentUser != null){
            navController.navigate(R.id.homeFragment);
        }
    }

    // ? Comprobación de los inputs
    private boolean validarFormulario() {
        boolean valid = true;

        username = usernameEditText.getText().toString().trim();
        email = emailEditText.getText().toString().trim();
        phone = phoneEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Debes introducir un nombre de usuario.");
            valid = false;
        } else if (username.contains(" ")) {
            usernameEditText.setError("El nombre de usuario no puede contener espacios.");
            valid = false;
        } else {
            usernameEditText.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Debes introducir un correo.");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Debes introducir un correo válido.");
            valid = false;
        } else {
            emailEditText.setError(null);
        }

        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Debes introducir un número de teléfono.");
            valid = false;
        } else if (!Patterns.PHONE.matcher(phone).matches()) {
            phoneEditText.setError("Debes introducir un número de teléfono válido.");
            valid = false;
        } else {
            phoneEditText.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Debes introducir una contraseña.");
            valid = false;
        } else if (password.length() < 8) {
            passwordEditText.setError("La contraseña debe tener al menos " + 8 + " caracteres.");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        return valid;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }
}
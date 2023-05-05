package com.example.mp08_firebase;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private NavController navController;
    private String username, email, phone, password, userUID;
    private EditText usernameEditText, mailEditText, phoneEditText, passwordEditText;
    private Button nextButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        usernameEditText = view.findViewById(R.id.usernameEditText);

        // ? Focus al iniciar el fragment
        usernameEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(usernameEditText, InputMethodManager.SHOW_IMPLICIT);

        mailEditText = view.findViewById(R.id.mailEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);

        // ? Cambios de botón de siguiente
        CustomTextWatcher textWatcher = new CustomTextWatcher();

        usernameEditText.addTextChangedListener(textWatcher);
        mailEditText.addTextChangedListener(textWatcher);
        phoneEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // ? Botón de registrar
        nextButton = view.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarFormulario();
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

    private void crearCuenta() {
        nextButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(mailEditText.getText().toString(), passwordEditText.getText().toString())
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            userUID = mAuth.getCurrentUser().getUid(); // Obtiene el UID del usuario creado.
                            DocumentReference documentReference = fStore.collection("users").document(userUID); // Crea un documento en la colección "users" con el UID.
                            Map<String,Object> user = new HashMap<>();
                            user.put("uid",userUID);
                            user.put("username",username.toLowerCase());
                            user.put("email",email);
                            user.put("phone",phone);
                            documentReference.set(user);
                            actualizarUI(mAuth.getCurrentUser());
                        } else {
                            Snackbar.make(requireView(), "Error: " + task.getException(), Snackbar.LENGTH_LONG).show();
                        }
                        nextButton.setEnabled(true);
                    }
                });

    }

    private void actualizarUI(FirebaseUser currentUser) {
        if(currentUser != null){
            navController.navigate(R.id.homeFragment);
        }
    }

    // ? Comprobación de los inputs
    private void validarFormulario() {
        boolean valid = true;

        username = usernameEditText.getText().toString().trim();
        email = mailEditText.getText().toString().trim();
        phone = phoneEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Debes introducir un nombre de usuario.");
            valid = false;
        } else if (username.contains(" ")) {
            usernameEditText.setError("El nombre de usuario no puede contener espacios.");
            valid = false;
        } else if (!username.toLowerCase().equals(username)) {
            usernameEditText.setError("El nombre de usuario no puede contener mayúsculas.");
            valid = false;
        } else {
            usernameEditText.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            mailEditText.setError("Debes introducir un correo.");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mailEditText.setError("Debes introducir un correo válido.");
            valid = false;
        } else {
            mailEditText.setError(null);
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

        if (valid) {
            checkEmailExists(email);
        }
    }

    private void checkUsernameExists(final String username) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("username", username.toLowerCase())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                usernameEditText.setError("El nombre de usuario ya está en uso.");
                            } else {
                                crearCuenta();
                            }
                        } else {
                            Log.e("RegisterFragment", "Error checking for existing username", task.getException());
                        }
                    }
                });
    }

    private void checkEmailExists(final String email) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("email", email.toLowerCase())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                mailEditText.setError("El correo electrónico ya está en uso.");
                            } else {
                                checkUsernameExists(username);
                            }
                        } else {
                            Log.e("RegisterFragment", "Error checking for existing email", task.getException());
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
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
        boolean allFieldsFilled = !TextUtils.isEmpty(usernameEditText.getText()) &&
                !TextUtils.isEmpty(mailEditText.getText()) &&
                !TextUtils.isEmpty(phoneEditText.getText()) &&
                !TextUtils.isEmpty(passwordEditText.getText());

        if (allFieldsFilled) {
            nextButton.setBackgroundResource(R.drawable.button_purple);
        } else {
            nextButton.setBackgroundResource(R.drawable.button_border_purple);
        }
    }
}
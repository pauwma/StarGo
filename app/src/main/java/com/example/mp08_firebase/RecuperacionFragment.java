package com.example.mp08_firebase;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class RecuperacionFragment extends Fragment {

    private NavController navController;
    private EditText userInfo;
    private Button nextButton;
    private FirebaseAuth mAuth;

    public RecuperacionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recuperacion, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        userInfo = view.findViewById(R.id.userInfoEditText);
        nextButton = view.findViewById(R.id.nextButton);

        mAuth = FirebaseAuth.getInstance();

        // ? Cambios de botón de siguiente
        CustomTextWatcher textWatcher = new CustomTextWatcher();
        userInfo.addTextChangedListener(textWatcher);

        // ? Flecha Back
        view.findViewById(R.id.flechaBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });

        view.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperarContraseña();
            }
        });
    }

    private void recuperarContraseña(){
        if (TextUtils.isEmpty(userInfo.getText())){
            return;
        }

        mAuth.sendPasswordResetEmail(userInfo.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Correo de recuperación de contraseña enviado", Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.signInFragment);
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            userInfo.setError("Usuario no encontrado");
                        } else {
                            Toast.makeText(getActivity(), "Error al enviar el correo de recuperación", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
        boolean allFieldsFilled = !TextUtils.isEmpty(userInfo.getText());
        if (allFieldsFilled) {
            nextButton.setBackgroundResource(R.drawable.button_purple);
        } else {
            nextButton.setBackgroundResource(R.drawable.button_border_purple);
        }
    }
}
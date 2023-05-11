package com.example.mp08_firebase;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditarPerfilFragment extends Fragment {

    private NavController navController;
    private ImageView photoEditProfile;
    private EditText displayNameEditText, usernameEditText, bioEditText;
    private String initialDisplayName, initialUsername, initialBio;
    private boolean hasDisplayNameChanged = false;
    private boolean hasUsernameChanged = false;
    private boolean hasBioChanged = false;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String uid;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private Uri selectedImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editar_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();

        photoEditProfile = view.findViewById(R.id.photoEditProfile);
        displayNameEditText = view.findViewById(R.id.displayNameEditText);
        usernameEditText = view.findViewById(R.id.usernameEditText);
        bioEditText = view.findViewById(R.id.bioEditText);


        // ? Cruz Back
        view.findViewById(R.id.cruzBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });

        DocumentReference userRef = firestore.collection("users").document(uid);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String avatarUrl = document.getString("avatar");
                        initialDisplayName = document.getString("displayName");
                        initialUsername = document.getString("username");
                        initialBio = document.getString("description");

                        if (avatarUrl != null) {
                            Glide.with(requireView()).load(avatarUrl).into(photoEditProfile);
                        }
                        displayNameEditText.setText(initialDisplayName);
                        usernameEditText.setText(initialUsername);
                        if (initialBio != null) {
                            bioEditText.setText(initialBio);
                        }

                    }
                }
            }
        });

        // Encuentra los dos TextViews que agregaste para mostrar el contador de caracteres
        TextView displayNameCounterTextView = view.findViewById(R.id.displayNameCounterTextView);
        TextView usernameCounterTextView = view.findViewById(R.id.usernameCounterTextView);
        TextView bioCounterTextView = view.findViewById(R.id.bioCounterTextView);
        // Establece los límites de caracteres para displayNameEditText y bioEditText
        displayNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        usernameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        bioEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(136)});
        bioEditText.setFilters(new InputFilter[]{new InputFilter() {
            public CharSequence filter(CharSequence src, int start, int end, Spanned dst, int dstart, int dend) {
                if (src.equals("\n")) { // si se trata de un salto de línea
                    if (dst.toString().split("\n").length >= 5) { // si ya hay 5 líneas
                        return ""; // ignora el salto de línea
                    }
                }
                return src; // de lo contrario, continúa con la entrada original
            }
        }});

        // Añade addTextChangedListener para actualizar el contador de caracteres dinámicamente
        displayNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                displayNameCounterTextView.setText(s.length() + "/32");
            }
        });
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                usernameCounterTextView.setText(s.length() + "/32");
            }
        });
        bioEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                bioCounterTextView.setText(s.length() + "/136");
            }
        });
        displayNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    displayNameCounterTextView.setVisibility(View.VISIBLE);
                } else {
                    displayNameCounterTextView.setVisibility(View.INVISIBLE);
                }
            }
        });
        displayNameEditText.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (displayNameEditText.hasFocus()) {
                    displayNameCounterTextView.setVisibility(View.VISIBLE);
                } else {
                    displayNameCounterTextView.setVisibility(View.INVISIBLE);
                }
            }
        });
        usernameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    usernameCounterTextView.setVisibility(View.VISIBLE);
                } else {
                    usernameCounterTextView.setVisibility(View.INVISIBLE);
                }
            }
        });
        usernameEditText.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (usernameEditText.hasFocus()) {
                    usernameCounterTextView.setVisibility(View.VISIBLE);
                } else {
                    usernameCounterTextView.setVisibility(View.INVISIBLE);
                }
            }
        });
        bioEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    bioCounterTextView.setVisibility(View.VISIBLE);
                } else {
                    bioCounterTextView.setVisibility(View.INVISIBLE);
                }
            }
        });
        bioEditText.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bioEditText.hasFocus()) {
                    bioCounterTextView.setVisibility(View.VISIBLE);
                } else {
                    bioCounterTextView.setVisibility(View.INVISIBLE);
                }
            }
        });
        photoEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarImagen();
            }
        });
        view.findViewById(R.id.tickAceptar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarDialogoConfirmacion();
            }
        });

    }

    private void updateProfile() {
        checkIfFieldsChanged();
        if (hasDisplayNameChanged || hasUsernameChanged || hasBioChanged) {
            if (hasUsernameChanged) {
                validateAndCheckUsernameAvailability();
            } else if (validateChanges()){
                updateProfileFields();
            }
        } else if (selectedImage != null) {
            updateProfilePicture();
        } else {
            Toast.makeText(getContext(), "No se realizaron cambios.", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean validateChanges() {
        boolean valid = true;

        String tmpDisplayName = displayNameEditText.getText().toString().trim();
        String tmpUsername = usernameEditText.getText().toString().trim();
        String tmpBio = bioEditText.getText().toString().trim();

        if (TextUtils.isEmpty(tmpUsername)) {
            usernameEditText.setError("Debes introducir un nombre de usuario.");
            valid = false;
        } else if (tmpUsername.contains(" ")) {
            usernameEditText.setError("El nombre de usuario no puede contener espacios.");
            valid = false;
        } else if (!tmpUsername.toLowerCase().equals(tmpUsername)) {
            usernameEditText.setError("El nombre de usuario no puede contener mayúsculas.");
            valid = false;
        } else {
            usernameEditText.setError(null);
        }

        if (tmpDisplayName.length() > 32) {
            displayNameEditText.setError("La biografía no puede tener más de 136 caracteres.");
            valid = false;
        } else {
            displayNameEditText.setError(null);
        }

        if (tmpBio.split("\n").length > 5) {
            bioEditText.setError("La biografía no puede tener más de 5 líneas.");
            valid = false;
        } else if (tmpBio.length() > 136) {
            bioEditText.setError("La biografía no puede tener más de 136 caracteres.");
            valid = false;
        } else {
            bioEditText.setError(null);
        }

        return valid;
    }
    private void updateProfileFields() {
        DocumentReference userRef = firestore.collection("users").document(uid);

        if (hasDisplayNameChanged) {
            userRef.update("displayName", displayNameEditText.getText().toString().trim());
        }
        if (hasUsernameChanged) {
            userRef.update("username", usernameEditText.getText().toString().trim());
        }
        if (hasBioChanged) {
            userRef.update("description", bioEditText.getText().toString().trim());
        }
        userRef.update(hasDisplayNameChanged ? "displayName" : "username", hasDisplayNameChanged ? displayNameEditText.getText().toString().trim() : usernameEditText.getText().toString().trim())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                            navController.navigateUp();
                        } else {
                            Toast.makeText(getContext(), "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void updateProfilePicture() {
        if (selectedImage != null) {
            // Crear una referencia al archivo de imagen en el Storage de Firebase
            StorageReference imageRef = storage.getReference().child("avatar").child(uid);
            // Subir la imagen al Storage de Firebase
            UploadTask uploadTask = imageRef.putFile(selectedImage);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continuar con la tarea para obtener la URL de descarga de la imagen
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        // Obtener la URL de descarga de la imagen subida
                        Uri downloadUri = task.getResult();

                        // Actualizar el parámetro "avatar" del usuario en la colección "users" de Firestore
                        DocumentReference userRef = firestore.collection("users").document(uid);
                        userRef.update("avatar", downloadUri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    navController.navigateUp();
                                } else {
                                    // Ocurrió un error al actualizar el parámetro "avatar" del usuario en la colección "users" de Firestore
                                    Toast.makeText(getContext(), "Error al actualizar el parámetro", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        // Ocurrió un error al obtener la URL de descarga de la imagen subida
                        Toast.makeText(getContext(), "Error al obtener la URL de descarga", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void checkIfFieldsChanged() {
        String currentDisplayName = displayNameEditText.getText().toString().trim();
        String currentUsername = usernameEditText.getText().toString().trim();
        String currentBio = bioEditText.getText().toString().trim();

        hasDisplayNameChanged = !initialDisplayName.equals(currentDisplayName);
        hasUsernameChanged = !initialUsername.equals(currentUsername);
        hasBioChanged = (initialBio == null && currentBio.length() > 0) || (initialBio != null && !initialBio.equals(currentBio));
    }
    private void validateAndCheckUsernameAvailability() {
        String currentUsername = usernameEditText.getText().toString().trim();

        if (currentUsername.contains(" ")) {
            usernameEditText.setError("El nombre de usuario no puede contener espacios.");
            return;
        } else if (!currentUsername.toLowerCase().equals(currentUsername)) {
            usernameEditText.setError("El nombre de usuario no puede contener mayúsculas.");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("username", currentUsername.toLowerCase())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                usernameEditText.setError("El nombre de usuario ya está en uso.");
                            } else {
                                // Nombre de usuario válido y disponible
                                updateProfile();
                            }
                        } else {
                            Log.e("EditarPerfilFragment", "Error checking for existing username", task.getException());
                        }
                    }
                });
    }
    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // Obtiene la Uri de la imagen seleccionada
            selectedImage = data.getData();

            try {
                Glide.with(requireView()).load(selectedImage).into(photoEditProfile);
            } catch (Exception e){}
            // Aquí debe implementar la lógica para guardar la imagen en su aplicación
        }
    }

    private void mostrarDialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("¿Seguro que quieres hacer los cambios?");
        builder.setCancelable(false);

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Aquí es donde colocarías la lógica para aplicar los cambios
                updateProfile();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
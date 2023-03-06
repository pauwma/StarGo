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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditarPerfilFragment extends Fragment {

    public AppViewModel appViewModel;
    NavController navController;
    private ImageView photoEditProfile;
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


        // ? Flecha Back
        view.findViewById(R.id.cruzBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });

        photoEditProfile = view.findViewById(R.id.photoEditProfile);
        DocumentReference userRef = firestore.collection("users").document(uid);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String avatarUrl = document.getString("avatar");
                        if (avatarUrl != null) {
                            Glide.with(requireView()).load(avatarUrl).into(photoEditProfile);
                        }
                    }
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
                subirAvatar();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Aquí es donde colocarías la lógica para cancelar los cambios
                // ...
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void subirAvatar(){
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
                                    // La imagen se subió correctamente y se guardó la referencia en la colección "users" de Firestore
                                    Toast.makeText(getContext(),"Imagen actualizada", Toast.LENGTH_SHORT).show();
                                    navController.navigateUp();
                                } else {
                                    // Ocurrió un error al actualizar el parámetro "avatar" del usuario en la colección "users" de Firestore
                                    Toast.makeText(getContext(),"Error al actualizar el parámetro", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        // Ocurrió un error al obtener la URL de descarga de la imagen subida
                        Toast.makeText(getContext(),"Error al obtener la URL de descarga", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

}
package com.example.mp08_firebase;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class BuscadorFragment extends Fragment {

    private DatabaseReference databaseReference;
    private LinearLayout postsContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        // Obtener una instancia de Firebase Realtime Database.
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        // Obtener una referencia a la ubicación donde se almacenan los datos de los posts en Firebase.
        databaseReference = firebaseDatabase.getReference("posts");
        */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buscador, container, false);

        /*postsContainer = view.findViewById(R.id.postsContainer);*/

        return view;
    }

    /*
    @Override
    public void onStart() {
        super.onStart();

        // Crear una consulta para obtener todos los posts.
        Query query = databaseReference.orderByChild("random").limitToFirst(8);

        // Agregar un listener a la consulta para recibir los datos de Firebase.
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postsContainer.removeAllViews();

                // En el listener, iterar sobre los datos recibidos y mostrar los posts en la vista correspondiente.
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    View postView = LayoutInflater.from(getContext()).inflate(R.layout.item_post, postsContainer, false);
                    TextView titleTextView = postView.findViewById(R.id.titleTextView);
                    titleTextView.setText(post.getTitle());
                    // Establecer la imagen del post
                    ImageView postImageView = postView.findViewById(R.id.postImageView);
                    Glide.with(getContext()).load(post.getImageUrl()).into(postImageView);
                    postsContainer.addView(postView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar el error
            }
        });
    }
    */
}
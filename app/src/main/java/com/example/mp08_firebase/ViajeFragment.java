package com.example.mp08_firebase;

import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mp08_firebase.items.Planet;
import com.example.mp08_firebase.items.PlanetAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ViajeFragment extends Fragment {

    private NavController navController;
    private List<Planet> planetList = new ArrayList<>();
    private RecyclerView planetRecyclerView;
    private PlanetAdapter planetAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_viaje, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        planetRecyclerView = view.findViewById(R.id.planetRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        planetRecyclerView.setLayoutManager(layoutManager);

        planetAdapter = new PlanetAdapter(getContext(), planetList);
        planetRecyclerView.setAdapter(planetAdapter);
        int startSpace = getResources().getDimensionPixelSize(R.dimen.start_space);
        planetRecyclerView.addItemDecoration(new StartSpaceItemDecoration(startSpace));


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("planets")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            planetList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Planet planet = document.toObject(Planet.class);
                                System.out.println(planet.getImage());
                                planetList.add(planet);
                            }
                            planetAdapter.notifyDataSetChanged();
                        } else {
                            // Algo salió mal, muestra un mensaje al usuario
                            Toast.makeText(getContext(), "No se han cargado los planetas", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

class StartSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final int startSpace;

    public StartSpaceItemDecoration(int startSpace) {
        this.startSpace = startSpace;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.left = startSpace;
        }
    }
}

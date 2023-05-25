package com.example.mp08_firebase;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mp08_firebase.items.Planet;
import com.example.mp08_firebase.items.PlanetAdapter;
import com.example.mp08_firebase.items.Trip;
import com.example.mp08_firebase.items.TripAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PlanetFragment extends Fragment {

    private NavController navController;
    private Planet planet;
    private ImageView bannerPlanetImageView;
    private TextView nameTextView, descriptionTextView, gravityTextView, temperatureTextView;
    private List<Trip> tripList = new ArrayList<>();
    private RecyclerView tripsRecyclerView;
    private TripAdapter tripAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        bannerPlanetImageView = view.findViewById(R.id.bannerPlanetImageView);
        nameTextView = view.findViewById(R.id.nameTextView);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        gravityTextView = view.findViewById(R.id.gravityTextView);
        temperatureTextView = view.findViewById(R.id.temperatureTextView);
        tripsRecyclerView = view.findViewById(R.id.tripsRecyclerView);

        if (getArguments() != null) {
            planet = new Planet();
            planet.setName(getArguments().getString("name"));
            planet.setDescription(getArguments().getString("description"));
            planet.setTemperature(getArguments().getString("temperature"));
            planet.setGravity(getArguments().getString("gravity"));
            planet.setImage(getArguments().getString("image"));
            planet.setBanner(getArguments().getString("banner"));
        }

        Glide.with(getContext()).load(planet.getBanner()).into(bannerPlanetImageView);
        nameTextView.setText(planet.getName());
        descriptionTextView.setText(planet.getDescription());
        temperatureTextView.setText(planet.getTemperature());
        gravityTextView.setText(planet.getGravity());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        tripsRecyclerView.setLayoutManager(layoutManager);

        tripAdapter = new TripAdapter(getContext(), tripList);
        tripsRecyclerView.setAdapter(tripAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("planets")
                .whereEqualTo("name", planet.getName())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String planetId = document.getId();

                                db.collection("trips")
                                        .whereEqualTo("arrivalPlanet", planetId)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    tripList.clear();
                                                    for (QueryDocumentSnapshot tripDocument : task.getResult()) {
                                                        Trip trip = tripDocument.toObject(Trip.class);
                                                        trip.setId(document.getId());
                                                        tripList.add(trip);
                                                    }
                                                    tripAdapter.notifyDataSetChanged();
                                                } else {
                                                    Toast.makeText(getContext(), "No se han cargado los viajes", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(getContext(), "No se ha encontrado el planeta", Toast.LENGTH_SHORT).show();
                        }
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
}
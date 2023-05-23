package com.example.mp08_firebase;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mp08_firebase.items.Planet;

public class PlanetFragment extends Fragment {

    private NavController navController;
    private Planet planet;
    private ImageView bannerPlanetImageView;
    private TextView nameTextView, descriptionTextView, gravityTextView, temperatureTextView;

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


        // ? Flecha Back
        view.findViewById(R.id.flechaBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });



    }
}
package com.example.mp08_firebase.items;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mp08_firebase.R;

import java.util.List;

public class PlanetAdapter extends RecyclerView.Adapter<PlanetAdapter.PlanetViewHolder> {

    private Context context;
    private List<Planet> planetList;

    public PlanetAdapter(Context context, List<Planet> planetList) {
        this.planetList = planetList;
        this.context = context;
    }

    @NonNull
    @Override
    public PlanetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_planet, parent, false);
        return new PlanetViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanetViewHolder holder, int position) {
        Planet planet = planetList.get(position);
        holder.planetName.setText(planet.getName());
        Glide.with(context).load(planet.getImage()).into(holder.planetImage);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPlanet(v, planet);
            }
        });
    }

    @Override
    public int getItemCount() {
        return planetList.size();
    }

    public void goToPlanet(View v, Planet planet){
        Bundle bundle = new Bundle();
        bundle.putString("name", planet.getName());
        bundle.putString("description", planet.getDescription());
        bundle.putString("temperature", planet.getTemperature());
        bundle.putString("gravity", planet.getGravity());
        bundle.putString("image", planet.getImage());
        bundle.putString("banner", planet.getBanner());
        Navigation.findNavController(v).navigate(R.id.planetFragment, bundle);
    }

    public static class PlanetViewHolder extends RecyclerView.ViewHolder {

        TextView planetName;
        ImageView planetImage;

        public PlanetViewHolder(@NonNull View itemView) {
            super(itemView);
            planetName = itemView.findViewById(R.id.planet_name);
            planetImage = itemView.findViewById(R.id.planet_image);
        }
    }
}

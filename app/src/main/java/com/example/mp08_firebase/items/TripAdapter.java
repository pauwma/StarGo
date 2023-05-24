package com.example.mp08_firebase.items;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mp08_firebase.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder>{
    private Context context;
    private List<Trip> tripList;

    public TripAdapter(Context context, List<Trip> tripList) {
        this.tripList = tripList;
        this.context = context;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = tripList.get(position);
        holder.tituloTextView.setText(trip.getName());
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("planets").document(trip.getArrivalPlanet())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Planet arrivalPlanet = documentSnapshot.toObject(Planet.class);
                        holder.planetTextView.setText(arrivalPlanet.getName());
                    }
                });

        Glide.with(context).load(trip.getImages().get(0)).into(holder.imageView);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToTrip(v, trip);
            }
        });
    }

    public void goToTrip(View v, Trip trip) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("trip", trip);
        Navigation.findNavController(v).navigate(R.id.tripFragment, bundle);
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {

        TextView tituloTextView, planetTextView;
        ImageView imageView;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tituloTextView = itemView.findViewById(R.id.tituloTextView);
            planetTextView = itemView.findViewById(R.id.planetTextView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
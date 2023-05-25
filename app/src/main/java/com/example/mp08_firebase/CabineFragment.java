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
import com.example.mp08_firebase.items.Cabin;
import com.example.mp08_firebase.items.CabinAdapter;
import com.example.mp08_firebase.items.Planet;
import com.example.mp08_firebase.items.Trip;
import com.example.mp08_firebase.items.TripAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.List;

public class CabineFragment extends Fragment implements CabinAdapter.CabinItemClickListener {

    private NavController navController;
    private ArrayList<Cabin> cabinList = new ArrayList<>();;
    private CabinAdapter cabinAdapter;
    private RecyclerView cabinRecyclerView;
    private Trip trip;
    private String planetName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cabine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        cabinAdapter = new CabinAdapter(getContext(), cabinList, getViewLifecycleOwner().getLifecycle(), this);
        cabinRecyclerView = view.findViewById(R.id.cabinRecyclerView);

        if (getArguments() != null) {
            trip = getArguments().getParcelable("trip");
            planetName = getArguments().getString("planetName");

            cabinRecyclerView = view.findViewById(R.id.cabinRecyclerView);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            cabinRecyclerView.setLayoutManager(layoutManager);

            cabinAdapter = new CabinAdapter(getContext(), cabinList, getViewLifecycleOwner().getLifecycle(), this);
            cabinRecyclerView.setAdapter(cabinAdapter);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String tripId = trip.getId();
            db.collection("trips").document(tripId).collection("cabins")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                cabinList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Cabin cabin = document.toObject(Cabin.class);
                                    cabinList.add(cabin);
                                }
                                cabinAdapter.notifyDataSetChanged();
                            } else {
                                // Algo salió mal, muestra un mensaje al usuario
                                Toast.makeText(getContext(), "No se han cargado las cabinas", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }

        // ? Flecha Back
        view.findViewById(R.id.flechaBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });

    }


    public void goToReserveSelection(View v, Trip trip, Cabin cabin, String planetName) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("trip", trip);
        bundle.putParcelable("cabin", cabin);
        bundle.putString("planetName", planetName);
        Navigation.findNavController(v).navigate(R.id.reserveFragment, bundle);
    }

    @Override
    public void onCabinReserveClick(View v, Cabin cabin) {
        goToReserveSelection(v, trip, cabin, planetName);
    }
}
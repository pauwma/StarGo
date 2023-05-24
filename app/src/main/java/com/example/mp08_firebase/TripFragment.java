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
import com.example.mp08_firebase.items.Trip;
import com.example.mp08_firebase.items.TripAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.List;

public class TripFragment extends Fragment {

    private NavController navController;
    private TextView tituloTextView, descriptionTextView, priceTextView, lowPriceTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        tituloTextView = view.findViewById(R.id.tituloTextView);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        priceTextView = view.findViewById(R.id.priceTextView);
        lowPriceTextView = view.findViewById(R.id.lowPriceTextView);

        if (getArguments() != null) {
            Trip trip = getArguments().getParcelable("trip");

            tituloTextView.setText(trip.getName());
            descriptionTextView.setText(trip.getDescription());

            String priceStr = String.valueOf(trip.getPrice());
            try {
                String[] priceParts = priceStr.split("\\.");
                if (priceParts.length == 2) {
                    priceTextView.setText(priceParts[0]); // ? Parte entera del precio
                    lowPriceTextView.setText("." + priceParts[1]+"€"); // ? Parte decimal del precio
                } else {
                    // ? Manejo de casos en los que el precio no tiene una parte decimal
                    priceTextView.setText(priceParts[0]);
                    lowPriceTextView.setText(".00€");
                }
            } catch (NumberFormatException e) {
                // ? Manejo de errores en caso de que la cadena de precio no pueda ser dividida correctamente
                e.printStackTrace();
                Toast.makeText(getContext(), "Error al mostrar el precio", Toast.LENGTH_SHORT).show();
            }

            // ? Carousel
            ImageCarousel carousel = view.findViewById(R.id.sliderImages);
            carousel.registerLifecycle(getLifecycle());
            carousel.setShowNavigationButtons(false);
            carousel.setAutoPlay(true);
            carousel.setAutoPlayDelay(3500);
            List<CarouselItem> list = new ArrayList<>();
            for (String imageUrl : trip.getImages()) {
                list.add(new CarouselItem(imageUrl));
            }
            carousel.setData(list);
        }

        // ? Flecha Back
        view.findViewById(R.id.flechaBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });

    }
}
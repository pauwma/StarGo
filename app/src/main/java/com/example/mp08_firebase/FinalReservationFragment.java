package com.example.mp08_firebase;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp08_firebase.items.Cabin;
import com.example.mp08_firebase.items.Trip;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FinalReservationFragment extends Fragment {

    private NavController navController;
    private Trip trip;
    private Cabin cabin;
    private String planetName, startDate, endDate;
    private ImageButton crossImageButton;
    private TextView tituloTextView, durationTextView, planetTextView, priceTextView, lowPriceTextView, salidaTextView, llegadaTextView, spacecraftTextView, cabinTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_final_reservation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        crossImageButton = view.findViewById(R.id.crossImageButton);
        tituloTextView = view.findViewById(R.id.tituloTextView);
        durationTextView = view.findViewById(R.id.durationTextView);
        salidaTextView = view.findViewById(R.id.salidaTextView);
        llegadaTextView = view.findViewById(R.id.llegadaTextView);
        spacecraftTextView = view.findViewById(R.id.spacecraftTextView);
        cabinTextView = view.findViewById(R.id.cabinTextView);
        priceTextView = view.findViewById(R.id.priceTextView);
        lowPriceTextView = view.findViewById(R.id.lowPriceTextView);

        if (getArguments() != null) {
            trip = getArguments().getParcelable("trip");
            cabin = getArguments().getParcelable("cabin");
            planetName = getArguments().getString("planetName");
            endDate = getArguments().getString("endDate");
            startDate = getArguments().getString("startDate");

            tituloTextView.setText(trip.getName());
            cabinTextView.setText(cabin.getName());
            spacecraftTextView.setText(trip.getSpacecraft());
            llegadaTextView.setText(endDate);
            salidaTextView.setText(startDate);

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
            for (String imageUrl : cabin.getImages()) {
                list.add(new CarouselItem(imageUrl));
            }
            carousel.setData(list);

            durationTextView.setText(trip.getDuration());
        }

        // ? Cruz Back
        crossImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.viajeFragment);
            }
        });
    }
}
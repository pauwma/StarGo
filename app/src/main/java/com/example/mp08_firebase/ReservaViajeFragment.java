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

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.List;

public class ReservaViajeFragment extends Fragment {

    NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reserva_viaje, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // ? Carousel
        ImageCarousel carousel = view.findViewById(R.id.sliderImages);
        carousel.registerLifecycle(getLifecycle());
        carousel.setShowNavigationButtons(false);
        carousel.setAutoPlay(true);
        carousel.setAutoPlayDelay(3500);
        List<CarouselItem> list = new ArrayList<>();
        list.add(new CarouselItem(R.drawable.suite_2));
        list.add(new CarouselItem(R.drawable.suite_1));
        list.add(new CarouselItem(R.drawable.suite_3));
        carousel.setData(list);

        // ? Flecha Back
        view.findViewById(R.id.flechaBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });

        // ? Reservar botón
        view.findViewById(R.id.reservarButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.ticketViajeFragment);
            }
        });
    }
}
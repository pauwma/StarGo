package com.example.mp08_firebase;

import android.net.Uri;
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

public class SeleccionViajeFragment extends Fragment {

    NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seleccion_viaje, container, false);
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
        carousel.setShowIndicator(false);
        carousel.setAutoPlayDelay(3500);
        List<CarouselItem> list = new ArrayList<>();
        list.add(new CarouselItem(R.drawable.starship));
        list.add(new CarouselItem(R.drawable.starship_expose_1));
        list.add(new CarouselItem(R.drawable.starship_expose_2));
        list.add(new CarouselItem(R.drawable.starship_expose_3));
        list.add(new CarouselItem(R.drawable.starship_expose_4));
        carousel.setData(list);


        // ? Flecha Back
        view.findViewById(R.id.flechaBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });
    }
}
package com.example.mp08_firebase;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mp08_firebase.items.Cabin;
import com.example.mp08_firebase.items.Trip;
import java.util.Calendar;
import java.util.Date;
import android.app.DatePickerDialog;
import java.text.SimpleDateFormat;
import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;
import java.util.ArrayList;
import java.util.List;

public class ReserveFragment extends Fragment {

    private NavController navController;
    private Trip trip;
    private Cabin cabin;
    private String planetName;
    private boolean dateSelected;
    private Button reservarButton;
    private TextView tituloTextView, durationTextView, planetTextView, priceTextView, lowPriceTextView, salidaTextView, llegadaTextView, spacecraftTextView, cabinTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reserve, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        tituloTextView = view.findViewById(R.id.tituloTextView);
        durationTextView = view.findViewById(R.id.durationTextView);
        salidaTextView = view.findViewById(R.id.salidaTextView);
        llegadaTextView = view.findViewById(R.id.llegadaTextView);
        spacecraftTextView = view.findViewById(R.id.spacecraftTextView);
        cabinTextView = view.findViewById(R.id.cabinTextView);
        priceTextView = view.findViewById(R.id.priceTextView);
        lowPriceTextView = view.findViewById(R.id.lowPriceTextView);
        reservarButton = view.findViewById(R.id.reservarButton);

        if (getArguments() != null) {
            trip = getArguments().getParcelable("trip");
            cabin = getArguments().getParcelable("cabin");
            planetName = getArguments().getString("planetName");

            tituloTextView.setText(trip.getName());

            cabinTextView.setText(cabin.getName());
            spacecraftTextView.setText(trip.getSpacecraft());

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

            String priceStr = String.valueOf(cabin.getPrice());
            try {
                String[] priceParts = priceStr.split("\\.");
                if (priceParts.length == 2) {
                    String decimalPart = String.format("%-2s", priceParts[1]).replace(' ', '0');
                    priceTextView.setText(priceParts[0]); // ? Parte entera del precio
                    lowPriceTextView.setText("." + decimalPart + "€"); // ? Parte decimal del precio
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
        }

        // ? Flecha Back
        view.findViewById(R.id.flechaBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });

        view.findViewById(R.id.salidaLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate(false);
            }
        });

        view.findViewById(R.id.llegadaLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate(true);
            }
        });

        view.findViewById(R.id.reservarButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dateSelected){
                    selectDate(false);
                } else {
                    // ! navController.navigate(R.id.fragment);
                }
            }
        });
    }

    public void selectDate(boolean isArrivalDate){
        // Extrae el número de días de la cadena de duración
        String duration = trip.getDuration();
        String daysString = duration.split("-")[0].trim();
        int daysApart = Integer.parseInt(daysString.replaceAll("\\D+",""));

        // Obtén la fecha actual
        final Calendar c = Calendar.getInstance();

        if(isArrivalDate) {
            c.add(Calendar.DAY_OF_YEAR, daysApart + 1); // Añade la duración del viaje + 1 a la fecha actual
        } else {
            c.add(Calendar.DAY_OF_YEAR, 1); // Añade un día a la fecha actual
        }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Crea un nuevo DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Crea un objeto Calendar a partir de la fecha seleccionada
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, monthOfYear, dayOfMonth);

                        // Guarda la fecha seleccionada
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                        if(isArrivalDate) {
                            String formattedEndDate = sdf.format(selectedDate.getTime());
                            llegadaTextView.setText(formattedEndDate);

                            // Resta X días a la fecha seleccionada para obtener la fecha de salida
                            selectedDate.add(Calendar.DAY_OF_MONTH, -daysApart);
                            String formattedStartDate = sdf.format(selectedDate.getTime());
                            salidaTextView.setText(formattedStartDate);
                        } else {
                            String formattedSelectedDate = sdf.format(selectedDate.getTime());
                            salidaTextView.setText(formattedSelectedDate);

                            // Añade X días a la fecha seleccionada para obtener la fecha de llegada
                            selectedDate.add(Calendar.DAY_OF_MONTH, daysApart);
                            String formattedEndDate = sdf.format(selectedDate.getTime());
                            llegadaTextView.setText(formattedEndDate);
                        }
                        dateSelected = true;
                        checkDateButton();
                    }
                }, year, month, day);

        // Establece el título dependiendo de si se seleccionará la fecha de llegada o de salida
        if (isArrivalDate) {
            datePickerDialog.setMessage("Selecciona el día de llegada");
        } else {
            datePickerDialog.setMessage("Selecciona el día de salida");
        }

        datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis()); // Establece la fecha mínima del DatePickerDialog

        datePickerDialog.show();
    }


    public void checkDateButton(){
        if (dateSelected){
            reservarButton.setBackgroundResource(R.drawable.button_purple);
        } else {
            reservarButton.setBackgroundResource(R.drawable.button_border_purple);
        }
    }

    public void goToReservation(View v, Trip trip, Cabin cabin, String planetName, String startDate, String endDate) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("trip", trip);
        bundle.putParcelable("cabin", cabin);
        bundle.putString("planetName", planetName);
        bundle.putString("startDate", startDate);
        bundle.putString("endDate", endDate);
        Navigation.findNavController(v).navigate(R.id.reserveFragment, bundle);
    }

}
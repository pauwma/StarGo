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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class ViajeFragment extends Fragment {

    NavController navController;
    private Button pickDateBtn;
    private ImageView peopleImg;
    private LinearLayout moreButton, lessButton;
    private TextView selectedDates, peopleNumber;

    // Variable para llevar el registro de la fecha de inicio y finalización
    private Calendar startDate = null;
    private Calendar endDate = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_viaje, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pickDateBtn = view.findViewById(R.id.dateButton);
        lessButton = view.findViewById(R.id.less_people);
        moreButton = view.findViewById(R.id.more_people);
        selectedDates = view.findViewById(R.id.dateText);
        peopleNumber = view.findViewById(R.id.people_number);
        peopleImg = view.findViewById(R.id.peopleImg);

        // Llamar a checkNumberOfPeople() en la creación del fragmento
        checkNumberOfPeople();

        pickDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear dos instancias de calendario para almacenar la fecha de inicio y finalización
                final Calendar startDateCalendar = Calendar.getInstance();
                final Calendar endDateCalendar = Calendar.getInstance();

                // Crear dos date picker dialog, uno para la fecha de inicio y otro para la fecha de finalización
                DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Actualizar la fecha de inicio y mostrar la fecha en el botón
                        startDateCalendar.set(year, month, dayOfMonth);
                        startDate = startDateCalendar;
                        updateSelectedDatesText();
                    }
                };

                DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Actualizar la fecha de finalización y mostrar la fecha en el botón
                        endDateCalendar.set(year, month, dayOfMonth);
                        endDate = endDateCalendar;
                        updateSelectedDatesText();
                    }
                };

                // Mostrar los dos date picker dialogs
                DatePickerDialog startDatePickerDialog = new DatePickerDialog(getContext(), startDateListener,
                        startDateCalendar.get(Calendar.YEAR), startDateCalendar.get(Calendar.MONTH), startDateCalendar.get(Calendar.DAY_OF_MONTH));

                DatePickerDialog endDatePickerDialog = new DatePickerDialog(getContext(), endDateListener,
                        endDateCalendar.get(Calendar.YEAR), endDateCalendar.get(Calendar.MONTH), endDateCalendar.get(Calendar.DAY_OF_MONTH));

                startDatePickerDialog.show();
                endDatePickerDialog.show();
            }
        });

        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                cambioNumeroPersonas(true);
            }
        });

        lessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                cambioNumeroPersonas(false);
            }
        });
    }



    // Actualizar el texto del botón con la fecha de inicio y finalización seleccionadas
    private void updateSelectedDatesText() {
        if (startDate != null && endDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            selectedDates.setText(sdf.format(startDate.getTime()) + " - " + sdf.format(endDate.getTime()));
        }
    }

    private void cambioNumeroPersonas(boolean cambio){
        int tmpNumber = Integer.parseInt(String.valueOf(peopleNumber.getText()));
        if (cambio){
            tmpNumber++;
            if (tmpNumber > 4) { // Si el número supera 4, no se aumenta más
                tmpNumber = 4;
            }
            peopleNumber.setText(String.valueOf(tmpNumber));
        } else {
            tmpNumber--;
            if (tmpNumber < 1) { // Si el número es menor que 1, no se disminuye más
                tmpNumber = 1;
            }
            peopleNumber.setText(String.valueOf(tmpNumber));
        }

        switch (tmpNumber){
            case 1:
                peopleImg.setImageResource(R.drawable.ic_user_icon);
                lessButton.setAlpha(0.5f);
                break;
            case 2:
                peopleImg.setImageResource(R.drawable.ic_user_icon2);
                lessButton.setAlpha(1f);
                break;
            case 3:
                peopleImg.setImageResource(R.drawable.ic_user_icon3);
                moreButton.setAlpha(1f);
                break;
            case 4:
                peopleImg.setImageResource(R.drawable.ic_user_icon3);
                moreButton.setAlpha(0.5f);
                break;
        }
    }

    // Comprobar el número de personas y ajustar la opacidad de los botones moreButton y lessButton en consecuencia
    private void checkNumberOfPeople() {
        int tmpNumber = Integer.parseInt(String.valueOf(peopleNumber.getText()));

        // Comprobar si el número de personas es menor o igual a 1 y ajustar la opacidad del botón lessButton en consecuencia
        if (tmpNumber <= 1) {
            lessButton.setAlpha(0.5f); // Deshabilitar el botón lessButton reduciendo su opacidad
        } else {
            lessButton.setAlpha(1f); // Habilitar el botón lessButton aumentando su opacidad
        }

        // Comprobar si el número de personas es mayor o igual a 4 y ajustar la opacidad del botón moreButton en consecuencia
        if (tmpNumber >= 4) {
            moreButton.setAlpha(0.5f); // Deshabilitar el botón moreButton reduciendo su opacidad
        } else {
            moreButton.setAlpha(1f); // Habilitar el botón moreButton aumentando su opacidad
        }
    }

}

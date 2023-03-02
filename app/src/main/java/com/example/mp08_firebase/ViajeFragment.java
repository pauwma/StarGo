package com.example.mp08_firebase;

import android.app.DatePickerDialog;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class ViajeFragment extends Fragment {

    NavController navController;
    private ImageView peopleImg;
    private LinearLayout moreButton, lessButton;
    private TextView dateText, dateText2, peopleNumber;

    // ? Variable para llevar el registro de la fecha de inicio y finalización
    private Calendar startDate = null;
    private Calendar endDate = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_viaje, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        lessButton = view.findViewById(R.id.less_people);
        moreButton = view.findViewById(R.id.more_people);
        peopleNumber = view.findViewById(R.id.people_number);
        peopleImg = view.findViewById(R.id.peopleImg);
        dateText = view.findViewById(R.id.dateText);
        dateText2 = view.findViewById(R.id.dateText2);

        // Obtener el calendario con la fecha actual
        Calendar currentDate = Calendar.getInstance();

        // Mostrar la fecha actual en el dateText
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateText.setText(sdf.format(currentDate.getTime()));
        currentDate.add(Calendar.DAY_OF_MONTH,1);
        dateText2.setText(sdf.format(currentDate.getTime()));
        dateText2.setAlpha(0.4f);

        // Llamar a checkNumberOfPeople() en la creación del fragmento
        checkNumberOfPeople();

        view.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.seleccionViajeFragment);
            }
        });

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar startDateCalendar = Calendar.getInstance();
                final Calendar minDateCalendar = Calendar.getInstance();
                minDateCalendar.set(Calendar.HOUR_OF_DAY, 0);

                DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Actualizar la fecha de inicio y mostrar la fecha en el botón
                        startDateCalendar.set(year, month, dayOfMonth);
                        startDate = startDateCalendar;
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        dateText.setText(sdf.format(startDate.getTime()));
                    }
                };

                DatePickerDialog startDatePickerDialog = new DatePickerDialog(getContext(), startDateListener,
                        startDateCalendar.get(Calendar.YEAR), startDateCalendar.get(Calendar.MONTH), startDateCalendar.get(Calendar.DAY_OF_MONTH));

                startDatePickerDialog.getDatePicker().setMinDate(minDateCalendar.getTimeInMillis());
                startDatePickerDialog.show();
            }
        });

        dateText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar endDateCalendar = Calendar.getInstance();
                final Calendar minDateCalendar = Calendar.getInstance();
                minDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                minDateCalendar.set(Calendar.MINUTE, 0);
                minDateCalendar.set(Calendar.SECOND, 0);
                minDateCalendar.set(Calendar.MILLISECOND, 0);

                DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Actualizar la fecha de finalización y mostrar la fecha en el botón
                        endDateCalendar.set(year, month, dayOfMonth);
                        endDate = endDateCalendar;
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        dateText2.setText(sdf.format(endDate.getTime()));
                    }
                };

                DatePickerDialog endDatePickerDialog = new DatePickerDialog(getContext(), endDateListener,
                        endDateCalendar.get(Calendar.YEAR), endDateCalendar.get(Calendar.MONTH), endDateCalendar.get(Calendar.DAY_OF_MONTH));

                endDatePickerDialog.getDatePicker().setMinDate(minDateCalendar.getTimeInMillis());
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

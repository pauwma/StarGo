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
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class ViajeFragment extends Fragment {

    NavController navController;
    private Button pickDateBtn;
    private TextView selectedDates;

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
        selectedDates = view.findViewById(R.id.dateText);

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
    }

    // Actualizar el texto del botón con la fecha de inicio y finalización seleccionadas
    private void updateSelectedDatesText() {
        if (startDate != null && endDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            selectedDates.setText(sdf.format(startDate.getTime()) + " - " + sdf.format(endDate.getTime()));
        }
    }
}

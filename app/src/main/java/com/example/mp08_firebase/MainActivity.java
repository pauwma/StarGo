package com.example.mp08_firebase;

import android.os.Bundle;
import com.example.mp08_firebase.databinding.ActivityMainBinding;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        navController = Navigation.findNavController(this, R.id.frameLayout);


        if (savedInstanceState == null) {
            navController.navigate(R.id.signInFragment);
        }
    }
}
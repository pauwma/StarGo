package com.example.mp08_firebase;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.mp08_firebase.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    NavController navController;
    private BottomNavigationView bottomNavigationView;

    // Lista con los ID de los fragmentos que no deben mostrar el BottomNavigationView
    private List<Integer> fragmentsWithoutBottomNav = Arrays.asList(R.id.signInFragment, R.id.settingsFragment, R.id.recuperacionFragment, R.id.registerFragment);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navController = Navigation.findNavController(this, R.id.frameLayout);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        // Configura el NavController
        NavController navController = Navigation.findNavController(this, R.id.frameLayout);

        // Configura el BottomNavigationView con el NavController
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        navController.navigate(R.id.homeFragment);
                        break;
                    case R.id.search:
                        navController.navigate(R.id.buscadorFragment);
                        break;
                    case R.id.profile:
                        navController.navigate(R.id.profileFragment);
                        break;
                    case R.id.settings:
                        navController.navigate(R.id.settingsFragment);
                        break;
                }
                return true;
            }
        });

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                // verifica si el ID del fragmento actual está en la lista de fragmentos sin BottomNavigationView
                if (fragmentsWithoutBottomNav.contains(destination.getId())) {
                    bottomNavigationView.setVisibility(View.GONE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            }
        });

        if (savedInstanceState == null) {
            navController.navigate(R.id.signInFragment);
        }
    }
}
package com.example.mp08_firebase;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.mp08_firebase.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

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
    private List<Integer> fragmentsWithoutBottomNav = Arrays.asList(R.id.chatAstraFragment ,R.id.mediaFragment ,R.id.detailedImageFragment ,R.id.editarPerfilFragment, R.id.startFragment, R.id.chatFragment, R.id.chatsHomeFragment, R.id.signInFragment, R.id.settingsFragment, R.id.recuperacionFragment, R.id.registerFragment, R.id.newPostFragment, R.id.tripFragment, R.id.planetFragment, R.id.reserveFragment, R.id.cabineFragment);

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Configura el NavController
        navController = Navigation.findNavController(this, R.id.mainLayout);

        // Configura el BottomNavigationView con el NavController
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Persistencia del user de Firebase
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int currentFragmentId = navController.getCurrentDestination().getId();
                int selectedFragmentId = 0;
                switch (item.getItemId()) {
                    case R.id.home:
                        selectedFragmentId = R.id.homeFragment;
                        break;
                    case R.id.search:
                        selectedFragmentId = R.id.buscadorFragment;
                        break;
                    case R.id.profile:
                        selectedFragmentId = R.id.profileFragment;
                        break;
                    case R.id.settings:
                        selectedFragmentId = R.id.viajeFragment;
                        break;
                }
                if (currentFragmentId != selectedFragmentId) {
                    navController.navigate(selectedFragmentId);
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

        // Verifica si el usuario está autenticado
        if (mAuth.getCurrentUser() == null) {
            navController.navigate(R.id.startFragment);
        } else {
            navController.navigate(R.id.homeFragment);
        }
    }
}

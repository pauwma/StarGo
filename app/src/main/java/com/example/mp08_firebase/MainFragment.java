package com.example.mp08_firebase;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.mp08_firebase.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainFragment extends Fragment {

    NavController navController;

    public MainFragment() {}

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        BottomNavigationView bottomNav = view.findViewById(R.id.bottom_navigation_view);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.homeFragment:
                    navController.navigate(R.id.homeFragment);
                    break;
                case R.id.buscadorFragment:
                    navController.navigate(R.id.buscadorFragment);
                    break;
                case R.id.profileFragment:
                    navController.navigate(R.id.profileFragment);
                    break;
            }
            return true;
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }
}
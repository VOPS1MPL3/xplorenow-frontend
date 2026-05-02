package com.xplorenow;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.xplorenow.util.TokenManager;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    TokenManager tokenManager;

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_XploreNow);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(bottomNav, navController);

        // Si hay token válido al abrir la app -> ir al login
        // (para que el usuario elija huella o contraseña)
        // Si no hay token -> queda en el home directamente
        if (savedInstanceState == null && tokenManager.isTokenValid()) {
            navController.navigate(R.id.loginFragment);
        }

        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            int id = destination.getId();
            if (id == R.id.homeFragment
                    || id == R.id.misReservasFragment
                    || id == R.id.perfilFragment
                    || id == R.id.favoritosFragment) {
                bottomNav.setVisibility(View.VISIBLE);
            } else {
                bottomNav.setVisibility(View.GONE);
            }
        });
    }
}

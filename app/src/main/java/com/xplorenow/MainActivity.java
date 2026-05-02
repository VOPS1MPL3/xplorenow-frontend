package com.xplorenow;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.xplorenow.data.repository.ReservaRepository;
import com.xplorenow.util.NetworkObserver;

import javax.inject.Inject;

import com.xplorenow.util.TokenManager;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    TokenManager tokenManager;
    @Inject
    ReservaRepository reservaRepository;

    private BottomNavigationView bottomNav;
    private View tvOfflineBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_XploreNow);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        tvOfflineBanner = findViewById(R.id.tvOfflineBanner);

        // Observar estado de conexión para el Modo Offline y Sincronización
        new NetworkObserver(this).observe(this, isConnected -> {
            if (tvOfflineBanner != null) {
                if (isConnected) {
                    tvOfflineBanner.setVisibility(View.GONE);
                    // Solo sincronizar si hay internet Y el usuario está logueado
                    if (reservaRepository != null && tokenManager.isTokenValid()) {
                        reservaRepository.sincronizarAccionesPendientes();
                    }
                } else {
                    tvOfflineBanner.setVisibility(View.VISIBLE);
                }
            }
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);

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
}

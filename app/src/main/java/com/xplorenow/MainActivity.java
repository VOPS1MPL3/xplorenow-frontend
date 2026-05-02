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

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private View tvOfflineBanner;

    @Inject
    ReservaRepository reservaRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // findViewById - Inicialización manual según documentación
        bottomNav = findViewById(R.id.bottomNav);
        tvOfflineBanner = findViewById(R.id.tvOfflineBanner);

        // Observar estado de conexión para el Modo Offline y Sincronización
        new NetworkObserver(this).observe(this, isConnected -> {
            if (tvOfflineBanner != null) {
                // Solo mostrar si realmente no hay conexión detectada
                if (isConnected) {
                    tvOfflineBanner.setVisibility(View.GONE);
                    if (reservaRepository != null) {
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

            // Control de visibilidad del menú inferior
            navController.addOnDestinationChangedListener((controller, destination, args) -> {
                int id = destination.getId();
                if (id == R.id.homeFragment
                        || id == R.id.misReservasFragment
                        || id == R.id.historialFragment
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

package com.xplorenow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.xplorenow.data.repository.ReservaRepository;
import com.xplorenow.notificacion.NotificacionPollingService;
import com.xplorenow.util.NetworkObserver;
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
    private NavController navController;

    private final ActivityResultLauncher<String> permisoNotificacionesLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // Si lo rechaza, el Service igual sigue funcionando -- el long
                // polling corre igual, pero las notificaciones no se muestran
                // hasta que el usuario habilite el permiso manualmente.
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_XploreNow);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        tvOfflineBanner = findViewById(R.id.tvOfflineBanner);

        pedirPermisoNotificacionesSiHaceFalta();

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
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);

            Long reservaIdDeNotificacion = extraerReservaIdDeIntent(getIntent());

            if (savedInstanceState == null) {
                if (reservaIdDeNotificacion != null && tokenManager.isTokenValid()) {
                    // Punto 12: se abrio la app tocando una notificacion (app no
                    // estaba corriendo) -> vamos directo al voucher, sin pasar
                    // por la pantalla de login/biometria.
                    abrirVoucher(reservaIdDeNotificacion);
                } else if (tokenManager.isTokenValid()) {
                    // Si hay token válido al abrir la app -> ir al login
                    // (para que el usuario elija huella o contraseña)
                    // Si no hay token -> queda en el home directamente
                    navController.navigate(R.id.loginFragment);
                }
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

                // Punto 12: arrancar/parar el long polling de novedades segun
                // la sesion. Volver a loginFragment = logout -> se para.
                // Cualquier pantalla logueado -> corriendo (llamar a
                // startService de nuevo si ya esta corriendo no hace nada raro,
                // Android solo re-dispara onStartCommand, no onCreate).
                if (id == R.id.loginFragment) {
                    detenerServicioNotificaciones();
                } else if (tokenManager.isTokenValid()) {
                    iniciarServicioNotificaciones();
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        // Punto 12: la app ya estaba corriendo y el usuario toco una
        // notificacion -> navegamos directo al voucher de esa reserva.
        Long reservaIdDeNotificacion = extraerReservaIdDeIntent(intent);
        if (reservaIdDeNotificacion != null && navController != null) {
            abrirVoucher(reservaIdDeNotificacion);
        }
    }

    private Long extraerReservaIdDeIntent(Intent intent) {
        if (intent == null) return null;
        long id = intent.getLongExtra(NotificacionPollingService.EXTRA_RESERVA_ID, -1L);
        return (id != -1L) ? id : null;
    }

    private void abrirVoucher(long reservaId) {
        Bundle bundle = new Bundle();
        bundle.putLong("reservaId", reservaId);
        navController.navigate(R.id.reservaDetalleFragment, bundle);
    }

    private void iniciarServicioNotificaciones() {
        startService(new Intent(this, NotificacionPollingService.class));
    }

    private void detenerServicioNotificaciones() {
        stopService(new Intent(this, NotificacionPollingService.class));
    }

    private void pedirPermisoNotificacionesSiHaceFalta() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permisoNotificacionesLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}

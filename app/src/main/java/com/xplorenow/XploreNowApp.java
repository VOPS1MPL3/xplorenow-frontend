package com.xplorenow;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.xplorenow.notificacion.NotificacionPollingService;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class XploreNowApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        crearCanalesDeNotificaciones();
    }

    /**
     * Punto 12 del TPO: en Android 8+ (API 26+) toda notificacion necesita
     * un NotificationChannel creado antes de poder mostrarse.
     *
     * Usamos dos canales con importancias distintas:
     *  - Novedades (HIGH): los avisos reales. Suenan y aparecen como pop-up.
     *  - Servicio (LOW): la notificacion persistente que Android exige a todo
     *    foreground service. Es silenciosa para no molestar al usuario.
     */
    private void crearCanalesDeNotificaciones() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null) return;

        NotificationChannel canalNovedades = new NotificationChannel(
                NotificacionPollingService.CHANNEL_ID,
                "Novedades de tus actividades",
                NotificationManager.IMPORTANCE_HIGH);
        canalNovedades.setDescription("Recordatorios, cancelaciones y reprogramaciones de tus reservas");
        manager.createNotificationChannel(canalNovedades);

        NotificationChannel canalServicio = new NotificationChannel(
                NotificacionPollingService.CHANNEL_ID_FOREGROUND,
                "Servicio de notificaciones",
                NotificationManager.IMPORTANCE_LOW);
        canalServicio.setDescription("Mantiene la app escuchando novedades en segundo plano");
        manager.createNotificationChannel(canalServicio);
    }
}

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
        crearCanalDeNotificaciones();
    }

    /**
     * Punto 12 del TPO: en Android 8+ (API 26+) toda notificacion necesita
     * un NotificationChannel creado antes de poder mostrarse.
     */
    private void crearCanalDeNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NotificacionPollingService.CHANNEL_ID,
                    "Novedades de tus actividades",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Recordatorios, cancelaciones y reprogramaciones de tus reservas");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}

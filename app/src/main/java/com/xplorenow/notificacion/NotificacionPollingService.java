package com.xplorenow.notificacion;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.xplorenow.MainActivity;
import com.xplorenow.R;
import com.xplorenow.data.dto.NovedadDTO;
import com.xplorenow.data.repository.NotificacionRepository;
import com.xplorenow.util.TokenManager;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;

@AndroidEntryPoint
public class NotificacionPollingService extends Service {

    public static final String CHANNEL_ID = "xplorenow_novedades";
    public static final String EXTRA_RESERVA_ID = "reserva_id";

    @Inject NotificacionRepository notificacionRepository;
    @Inject TokenManager tokenManager;

    private final AtomicBoolean activo = new AtomicBoolean(false);
    private Thread pollingThread;

    @Override
    public void onCreate() {
        super.onCreate();
        activo.set(true);
        pollingThread = new Thread(this::loopDePolling, "notificaciones-polling");
        pollingThread.start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void loopDePolling() {
        String ultimaFecha = null;

        while (activo.get()) {
            try {
                if (!tokenManager.isTokenValid()) {
                    dormir(5_000);
                    continue;
                }

                List<NovedadDTO> novedades = notificacionRepository.esperarNovedades(ultimaFecha);

                for (NovedadDTO n : novedades) {
                    mostrarNotificacion(n);
                    ultimaFecha = n.getFecha();
                }
            } catch (Throwable t) {
                dormir(3_000);
            }
        }
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            activo.set(false);
        }
    }

    private void mostrarNotificacion(NovedadDTO novedad) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_RESERVA_ID, novedad.getReservaId());

        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            piFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        int notifId = novedad.getId().intValue();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notifId, intent, piFlags);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_bell)
                .setContentTitle(tituloSegunTipo(novedad))
                .setContentText(novedad.getMensaje())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(novedad.getMensaje()))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(notifId, notification);
        }
    }

    private String tituloSegunTipo(NovedadDTO novedad) {
        if (novedad.getTipo() == null) return "XploreNow";
        switch (novedad.getTipo()) {
            case CANCELACION: return "Actividad cancelada";
            case REPROGRAMACION: return "Actividad reprogramada";
            case RECORDATORIO_24H: return "Recordatorio de actividad";
            default: return "XploreNow";
        }
    }

    @Override
    public void onDestroy() {
        activo.set(false);
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
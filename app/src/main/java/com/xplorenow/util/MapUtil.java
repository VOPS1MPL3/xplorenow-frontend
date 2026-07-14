package com.xplorenow.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MapUtil {

    public static void abrirMapaNavegacion(Context context, Double latitud, Double longitud, String label) {
        if (latitud == null || longitud == null || latitud == 0.0 || longitud == 0.0) {
            Toast.makeText(context, "Las coordenadas de este sitio no están disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        String destination = latitud + "," + longitud;
        if (label != null && !label.trim().isEmpty()) {
            try {
                String encoded = URLEncoder.encode(label.trim(), StandardCharsets.UTF_8.name());
                destination = encoded + "@" + latitud + "," + longitud;
            } catch (UnsupportedEncodingException ignored) {
                // UTF-8 siempre disponible; se mantiene destino por coordenadas
            }
        }

        String url = "https://www.google.com/maps/dir/?api=1&destination=" + destination;

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }
}

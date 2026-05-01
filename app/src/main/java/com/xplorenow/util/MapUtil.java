package com.xplorenow.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class MapUtil {

    public static void abrirMapaNavegacion(Context context, Double latitud, Double longitud, String label) {
        if (latitud == null || longitud == null || latitud == 0.0 || longitud == 0.0) {
            Toast.makeText(context, "Las coordenadas de este sitio no están disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://www.google.com/maps/dir/?api=1&destination=" + latitud + "," + longitud;
        
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }
}
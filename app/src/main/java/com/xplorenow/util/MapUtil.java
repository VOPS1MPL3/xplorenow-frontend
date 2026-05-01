package com.xplorenow.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class MapUtil {

    /**
     * Abre Google Maps en modo navegación desde la ubicación actual hasta el destino especificado.
     *
     * @param context   Contexto de la aplicación.
     * @param latitud   Latitud del destino.
     * @param longitud  Longitud del destino.
     * @param label     Nombre del lugar para mostrar en el marcador.
     */
    public static void abrirMapaNavegacion(Context context, Double latitud, Double longitud, String label) {
        if (latitud == null || longitud == null || latitud == 0.0 || longitud == 0.0) {
            Toast.makeText(context, "Las coordenadas de este sitio no están disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        // El esquema 'google.navigation:q=lat,lon' abre directamente el modo "Cómo llegar"
        // desde la ubicación actual del dispositivo.
        String uriString = "google.navigation:q=" + latitud + "," + longitud;
        
        Uri gmmIntentUri = Uri.parse(uriString);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Verificamos si la app de Google Maps está instalada
        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            // Si no está instalada, abrimos en el navegador con el formato de Directions API
            String webUrl = "https://www.google.com/maps/dir/?api=1&destination=" + latitud + "," + longitud;
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
            context.startActivity(webIntent);
        }
    }
}

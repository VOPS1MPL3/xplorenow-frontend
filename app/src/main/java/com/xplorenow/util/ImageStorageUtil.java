package com.xplorenow.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilidad para guardar imagenes (foto de perfil) en almacenamiento interno
 * de la app. Sigue el patron explicado en el apunte de la materia (seccion 9):
 * leer el Uri devuelto por la galeria, decodificarlo a Bitmap, y guardarlo
 * como JPEG en getFilesDir().
 *
 * Decision: se guarda con prefijo "file://" para que Glide pueda cargarlo
 * directamente y para que el backend reciba un URI valido en fotoUrl.
 */
public final class ImageStorageUtil {

    private static final String PROFILE_IMAGE_NAME = "profile_image.jpg";
    private static final int JPEG_QUALITY = 90;

    private ImageStorageUtil() {}

    /**
     * Lee la imagen seleccionada en la galeria y la guarda dentro del
     * almacenamiento interno de la app. Devuelve el path absoluto del
     * archivo guardado (con prefijo "file://"), o null si fallo.
     */
    public static String guardarFotoPerfil(Context context, Uri imageUri) {
        if (imageUri == null) return null;

        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = context.getContentResolver().openInputStream(imageUri);
            if (in == null) return null;

            Bitmap bmp = BitmapFactory.decodeStream(in);
            if (bmp == null) return null;

            File file = new File(context.getFilesDir(), PROFILE_IMAGE_NAME);
            out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
            out.flush();

            return "file://" + file.getAbsolutePath();
        } catch (IOException e) {
            return null;
        } finally {
            cerrar(in);
            cerrar(out);
        }
    }

    /**
     * Devuelve un objeto cargable por Glide a partir de la fotoUrl que viene
     * del backend o que persistimos localmente. Glide acepta tanto String
     * como File, asi que devolvemos el tipo mas adecuado segun el caso.
     */
    public static Object resolverParaGlide(String fotoUrl) {
        if (fotoUrl == null || fotoUrl.isEmpty()) return null;
        if (fotoUrl.startsWith("file://")) {
            return new File(fotoUrl.substring("file://".length()));
        }
        return fotoUrl;
    }

    private static void cerrar(java.io.Closeable c) {
        if (c == null) return;
        try { c.close(); } catch (IOException ignored) {}
    }
}

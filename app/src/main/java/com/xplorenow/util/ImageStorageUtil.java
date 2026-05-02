package com.xplorenow.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ImageStorageUtil {

    private static final String PROFILE_IMAGE_NAME = "profile_image.jpg";
    private static final int JPEG_QUALITY = 90;

    private ImageStorageUtil() {}

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

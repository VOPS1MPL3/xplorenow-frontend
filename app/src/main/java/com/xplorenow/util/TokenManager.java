package com.xplorenow.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import android.util.Base64;
import org.json.JSONObject;

@Singleton
public class TokenManager {

    private static final String PREFS_NAME = "xplorenow_secure_prefs";
    private static final String KEY_TOKEN = "jwt_token";

    private final SharedPreferences prefs;

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        SharedPreferences sp;
        try {
            String masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sp = EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKey,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        this.prefs = sp;
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }

    public boolean hasToken() {
        return getToken() != null;
    }

    /**
     * Decodifica el payload del JWT y verifica si el campo "exp" ya pasó.
     * No hace validación criptográfica — eso lo hace el backend.
     * Si no puede parsear el token, asume que venció (fuerza re-login).
     *
     * @return true si el token existe y NO venció todavía
     */
    public boolean isTokenValid() {
        String token = getToken();
        if (token == null) return false;

        try {
            // JWT = header.payload.signature — nos interesa el payload (índice 1)
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            // El payload está en Base64 URL — lo decodificamos
            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_PADDING);
            String payload = new String(decoded, "UTF-8");

            JSONObject json = new JSONObject(payload);
            long exp = json.getLong("exp"); // segundos desde epoch

            long nowSeconds = System.currentTimeMillis() / 1000;

            return nowSeconds < exp;

        } catch (Exception e) {
            // Si algo falla al parsear, limpiamos el token y forzamos re-login
            clearToken();
            return false;
        }
    }
}

package com.xplorenow.util;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.xplorenow.R;

/**
 * Helper para proteger acciones que requieren sesion activa.
 * Navega directo al loginFragment sin depender de una action especifica,
 * lo que evita crashes cuando se llama desde fragments que no tienen
 * action_home_to_login definida (ej: detalleFragment).
 */
public class SessionGuard {

    public static boolean verificar(Fragment fragment, TokenManager tokenManager, Runnable accion) {
        if (tokenManager.isTokenValid()) {
            accion.run();
            return true;
        } else {
            tokenManager.clearToken();
            Navigation.findNavController(fragment.requireView())
                    .navigate(R.id.loginFragment);
            return false;
        }
    }
}

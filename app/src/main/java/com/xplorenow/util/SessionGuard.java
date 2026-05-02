package com.xplorenow.util;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.xplorenow.R;

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

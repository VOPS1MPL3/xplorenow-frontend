package com.xplorenow.auth;

/**
 * Body de POST /auth/password/olvide.
 * El usuario ingresa su email y el backend manda un OTP por correo.
 */
public class OlvideContrasenaRequest {
    private String email;

    public OlvideContrasenaRequest(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
}

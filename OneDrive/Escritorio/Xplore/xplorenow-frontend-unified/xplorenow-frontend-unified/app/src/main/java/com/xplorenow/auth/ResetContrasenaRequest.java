package com.xplorenow.auth;

/**
 * Body de POST /auth/password/reset.
 * Verifica el OTP y, si es valido, reemplaza la contrasena.
 */
public class ResetContrasenaRequest {
    private String email;
    private String codigo;
    private String nuevaPassword;

    public ResetContrasenaRequest(String email, String codigo, String nuevaPassword) {
        this.email = email;
        this.codigo = codigo;
        this.nuevaPassword = nuevaPassword;
    }

    public String getEmail()         { return email; }
    public String getCodigo()        { return codigo; }
    public String getNuevaPassword() { return nuevaPassword; }
}

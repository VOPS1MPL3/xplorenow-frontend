package com.xplorenow.model.auth;

public class OtpConfirmarRequest {
    private String email;
    private String codigo;

    public OtpConfirmarRequest(String email, String codigo) {
        this.email = email;
        this.codigo = codigo;
    }

    public String getEmail() { return email; }
    public String getCodigo() { return codigo; }
}

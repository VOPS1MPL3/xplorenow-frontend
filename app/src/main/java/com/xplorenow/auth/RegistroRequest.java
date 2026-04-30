package com.xplorenow.auth;

public class RegistroRequest {
    private String email;
    private String password;
    private String nombre;

    public RegistroRequest(String email, String password, String nombre) {
        this.email    = email;
        this.password = password;
        this.nombre   = nombre;
    }

    public String getEmail()    { return email; }
    public String getPassword() { return password; }
    public String getNombre()   { return nombre; }
}

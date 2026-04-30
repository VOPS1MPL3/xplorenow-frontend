package com.xplorenow.model.auth;

public class RegistroRequest {
    private String email;
    private String password;
    private String nombre;
    private String telefono;

    public RegistroRequest(String email, String password, String nombre, String telefono) {
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
}

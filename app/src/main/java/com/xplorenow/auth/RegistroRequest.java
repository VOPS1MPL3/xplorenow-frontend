package com.xplorenow.auth;

/**
 * Body de POST /auth/registro.
 * Campos sincronizados con el RegistroRequest del backend.
 */
public class RegistroRequest {
    private String email;
    private String password;
    private String nombre;
    private String apellido;
    private String telefono;

    public RegistroRequest(String email, String password,
                           String nombre, String apellido, String telefono) {
        this.email    = email;
        this.password = password;
        this.nombre   = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
    }

    public String getEmail()    { return email; }
    public String getPassword() { return password; }
    public String getNombre()   { return nombre; }
    public String getApellido() { return apellido; }
    public String getTelefono() { return telefono; }
}

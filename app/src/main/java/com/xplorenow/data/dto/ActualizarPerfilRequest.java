package com.xplorenow.data.dto;

/**
 * Body de PUT /perfil.
 * El email NO se puede modificar desde acá (regla de negocio del backend, doc 6.2).
 */
public class ActualizarPerfilRequest {

    private String nombre;
    private String telefono;
    private String fotoUrl;

    public ActualizarPerfilRequest(String nombre, String telefono, String fotoUrl) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.fotoUrl = fotoUrl;
    }

    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getFotoUrl() { return fotoUrl; }
}

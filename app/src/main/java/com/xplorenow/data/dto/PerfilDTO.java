package com.xplorenow.data.dto;

import java.util.List;

public class PerfilDTO {

    private Long id;
    private String email;
    private String nombre;
    private String telefono;
    private String fotoUrl;
    private List<CategoriaDTO> preferencias;

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getFotoUrl() { return fotoUrl; }
    public List<CategoriaDTO> getPreferencias() { return preferencias; }
}

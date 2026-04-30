package com.xplorenow.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PerfilResponse {
    private Long id;
    private String email;
    private String nombre;
    private String telefono;

    @SerializedName("fotoUrl")
    private String fotoUrl;

    private List<CategoriaResponse> preferencias;

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getFotoUrl() { return fotoUrl; }
    public List<CategoriaResponse> getPreferencias() { return preferencias; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public void setPreferencias(List<CategoriaResponse> preferencias) { this.preferencias = preferencias; }
}
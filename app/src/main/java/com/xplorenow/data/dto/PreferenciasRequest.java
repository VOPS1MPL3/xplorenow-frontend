package com.xplorenow.data.dto;

import java.util.List;

/**
 * Body de PUT /perfil/preferencias.
 * Reemplaza la lista completa de categorias preferidas del usuario
 * (regla de negocio del backend: las preferencias se sobreescriben, doc 6.2).
 */
public class PreferenciasRequest {

    private List<Long> categoriaIds;

    public PreferenciasRequest(List<Long> categoriaIds) {
        this.categoriaIds = categoriaIds;
    }

    public List<Long> getCategoriaIds() { return categoriaIds; }
}

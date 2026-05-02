package com.xplorenow.data.dto;

import java.util.List;

public class PreferenciasRequest {

    private List<Long> categoriaIds;

    public PreferenciasRequest(List<Long> categoriaIds) {
        this.categoriaIds = categoriaIds;
    }

    public List<Long> getCategoriaIds() { return categoriaIds; }
}

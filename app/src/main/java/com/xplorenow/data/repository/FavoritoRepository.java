package com.xplorenow.data.repository;

import com.xplorenow.data.api.XploreNowApi;
import com.xplorenow.data.dto.FavoritoDTO;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class FavoritoRepository {

    private final XploreNowApi api;

    @Inject
    public FavoritoRepository(XploreNowApi api) {
        this.api = api;
    }

    public Call<List<FavoritoDTO>> misFavoritos() {
        return api.misFavoritos();
    }

    public Call<FavoritoDTO> marcar(long actividadId) {
        return api.marcarFavorito(actividadId);
    }

    public Call<Void> desmarcar(long actividadId) {
        return api.desmarcarFavorito(actividadId);
    }
}

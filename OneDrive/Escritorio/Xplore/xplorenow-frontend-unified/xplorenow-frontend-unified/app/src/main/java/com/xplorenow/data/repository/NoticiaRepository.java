package com.xplorenow.data.repository;

import com.xplorenow.data.api.XploreNowApi;
import com.xplorenow.data.dto.NoticiaDTO;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Call;

@Singleton
public class NoticiaRepository {
    private final XploreNowApi api;

    @Inject
    public NoticiaRepository(XploreNowApi api) {
        this.api = api;
    }

    public Call<List<NoticiaDTO>> listarNoticias() {
        return api.listarNoticias();
    }

    public Call<NoticiaDTO> obtenerNoticia(long id) {
        return api.obtenerNoticia(id);
    }
}
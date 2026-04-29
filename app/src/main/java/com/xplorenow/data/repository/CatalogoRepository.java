package com.xplorenow.data.repository;
import com.xplorenow.data.api.XploreNowApi;
import com.xplorenow.data.dto.CategoriaDTO;
import com.xplorenow.data.dto.DestinoDTO;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Call;

@Singleton
public class CatalogoRepository {

    private final XploreNowApi api;

    @Inject
    public CatalogoRepository(XploreNowApi api) {
        this.api = api;
    }

    public Call<List<DestinoDTO>> listarDestinos() {
        return api.listarDestinos();
    }

    public Call<List<CategoriaDTO>> listarCategorias() {
        return api.listarCategorias();
    }
}
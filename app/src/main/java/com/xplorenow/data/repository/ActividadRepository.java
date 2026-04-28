package com.xplorenow.data.repository;
import com.xplorenow.data.api.XploreNowApi;
import com.xplorenow.data.dto.ActividadDTO;
import com.xplorenow.data.dto.ActividadDetalleDTO;
import com.xplorenow.data.dto.PageResponseDTO;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Call;

@Singleton
public class ActividadRepository {

    private final XploreNowApi api;

    @Inject
    public ActividadRepository(XploreNowApi api) {
        this.api = api;
    }

    public Call<PageResponseDTO<ActividadDTO>> listarActividades(int page, int size) {
        return api.listarActividades(page, size);
    }

    public Call<ActividadDetalleDTO> obtenerActividad(long id) {
        return api.obtenerActividad(id);
    }
}
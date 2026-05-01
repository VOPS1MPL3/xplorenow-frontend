package com.xplorenow.data.repository;
import com.xplorenow.data.api.XploreNowApi;
import com.xplorenow.data.dto.ActividadDTO;
import com.xplorenow.data.dto.ActividadDetalleDTO;
import com.xplorenow.data.dto.FiltrosActividad;
import com.xplorenow.data.dto.PageResponseDTO;
import java.util.List;
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
        return api.listarActividades(page, size, null, null, null, null, null);
    }

    public Call<PageResponseDTO<ActividadDTO>> listarActividades(
            int page, int size, FiltrosActividad filtros) {
        if (filtros == null) {
            return listarActividades(page, size);
        }
        String fecha = filtros.getFechaDesde();
        return api.listarActividades(
                page, size,
                filtros.getDestinoId(),
                filtros.getCategoriaId(),
                fecha,
                filtros.getPrecioMin(),
                filtros.getPrecioMax()
        );
    }

    public Call<ActividadDetalleDTO> obtenerActividad(long id) {
        return api.obtenerActividad(id);
    }

    public Call<List<ActividadDTO>> obtenerDestacadas() {
        return api.obtenerDestacadas();
    }
}
package com.xplorenow.data.repository;
import com.xplorenow.data.api.XploreNowApi;
import com.xplorenow.data.dto.EstadoReserva;
import com.xplorenow.data.dto.ReservaDTO;
import com.xplorenow.data.dto.ReservaDetalleDTO;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Call;

@Singleton
public class ReservaRepository {
    private final XploreNowApi api;

    @Inject
    public ReservaRepository(XploreNowApi api) {
        this.api = api;
    }

    public Call<List<ReservaDTO>> misReservas(EstadoReserva estado) {
        String filtro = (estado == null) ? null : estado.name();
        return api.misReservas(filtro);
    }

    public Call<ReservaDetalleDTO> obtenerReserva(long id) {
        return api.obtenerReserva(id);
    }
}
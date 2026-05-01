package com.xplorenow.data.repository;
import com.xplorenow.data.api.XploreNowApi;
import com.xplorenow.data.dto.EstadoReserva;
import com.xplorenow.data.dto.ReservaDTO;
import com.xplorenow.data.dto.ReservaDetalleDTO;
import com.xplorenow.data.dto.CrearReservaRequest;
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

    public Call<ReservaDetalleDTO> cancelarReserva(long id) {
        return api.cancelarReserva(id);
    }

    public Call<List<ReservaDTO>> historial(Long destinoId, String fechaDesde, String fechaHasta) {
        return api.historial(destinoId, fechaDesde, fechaHasta);
    }
    public Call<ReservaDetalleDTO> crearReserva(CrearReservaRequest request) {
    return api.crearReserva(request);
    }
}
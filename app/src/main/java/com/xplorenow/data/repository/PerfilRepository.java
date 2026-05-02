package com.xplorenow.data.repository;

import com.xplorenow.data.api.XploreNowApi;
import com.xplorenow.data.dto.ActualizarPerfilRequest;
import com.xplorenow.data.dto.PerfilDTO;
import com.xplorenow.data.dto.PreferenciasRequest;
import com.xplorenow.data.dto.ReservaDTO;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Call;

@Singleton
public class PerfilRepository {

    private final XploreNowApi api;

    @Inject
    public PerfilRepository(XploreNowApi api) {
        this.api = api;
    }

    public Call<PerfilDTO> obtenerPerfil() {
        return api.obtenerPerfil();
    }

    public Call<PerfilDTO> actualizarPerfil(String nombre, String telefono, String fotoUrl) {
        return api.actualizarPerfil(new ActualizarPerfilRequest(nombre, telefono, fotoUrl));
    }

    public Call<PerfilDTO> actualizarPreferencias(List<Long> categoriaIds) {
        return api.actualizarPreferencias(new PreferenciasRequest(categoriaIds));
    }

    /**
     * Listado de reservas del usuario, opcionalmente filtrado por estado.
     * Reusa el endpoint /reservas/mis. Pasar null para traer todas.
     */
    public Call<List<ReservaDTO>> misReservas(String estado) {
        return api.misReservas(estado);
    }
}

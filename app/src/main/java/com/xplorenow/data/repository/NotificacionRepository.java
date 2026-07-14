package com.xplorenow.data.repository;

import com.xplorenow.data.api.XploreNowApi;
import com.xplorenow.data.dto.NovedadDTO;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Response;

@Singleton
public class NotificacionRepository {

    private final XploreNowApi api;

    @Inject
    public NotificacionRepository(XploreNowApi api) {
        this.api = api;
    }

    public List<NovedadDTO> esperarNovedades(String ultimaFecha) {
        try {
            Response<List<NovedadDTO>> response = api.obtenerNovedades(ultimaFecha).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            }
            return Collections.emptyList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public List<NovedadDTO> obtenerPendientes() {
        try {
            Response<List<NovedadDTO>> response = api.obtenerPendientes().execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            }
            return Collections.emptyList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}

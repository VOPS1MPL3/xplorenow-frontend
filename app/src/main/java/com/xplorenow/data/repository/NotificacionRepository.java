package com.xplorenow.data.repository;

import com.xplorenow.data.api.XploreNowApi;
import com.xplorenow.data.dto.NovedadDTO;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Response;

/**
 * Punto 12 del TPO. Wrapper sincrono (.execute(), no .enqueue()) sobre
 * /notificaciones/novedades a proposito: quien llama a esto
 * (NotificacionPollingService) ya corre en su propio thread de background,
 * igual que el ejemplo de Android nativo del apunte de la ultima clase
 * (okHttpClient.newCall(request).execute() dentro de un while).
 */
@Singleton
public class NotificacionRepository {

    private final XploreNowApi api;

    @Inject
    public NotificacionRepository(XploreNowApi api) {
        this.api = api;
    }

    /**
     * Bloquea el thread que la llama hasta que el servidor responde con
     * novedades (200) o hasta que corta por timeout (204) o error de red.
     *
     * @return la lista de novedades nuevas, o lista vacia si no hubo o si
     *         fallo la conexion (el service reintenta en el proximo ciclo).
     */
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
}

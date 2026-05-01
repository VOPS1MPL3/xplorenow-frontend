package com.xplorenow.data.api;

import com.xplorenow.data.dto.ActividadDTO;
import com.xplorenow.data.dto.ActividadDetalleDTO;
import com.xplorenow.data.dto.CalificacionDTO;
import com.xplorenow.data.dto.CalificacionRequest;
import com.xplorenow.data.dto.CategoriaDTO;
import com.xplorenow.data.dto.CrearReservaRequest;
import com.xplorenow.data.dto.DestinoDTO;
import com.xplorenow.data.dto.HorarioDTO;
import com.xplorenow.data.dto.PageResponseDTO;
import com.xplorenow.data.dto.ReservaDTO;
import com.xplorenow.data.dto.ReservaDetalleDTO;
import com.xplorenow.data.dto.NoticiaDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface XploreNowApi {

    @GET("actividades")
    Call<PageResponseDTO<ActividadDTO>> listarActividades(
            @Query("page") int page,
            @Query("size") int size,
            @Query("destinoId") Long destinoId,
            @Query("categoriaId") Long categoriaId,
            @Query("fecha") String fecha,
            @Query("precioMin") Double precioMin,
            @Query("precioMax") Double precioMax
    );

    @GET("actividades/{id}")
    Call<ActividadDetalleDTO> obtenerActividad(@Path("id") long id);

    @GET("destinos")
    Call<List<DestinoDTO>> listarDestinos();

    @GET("categorias")
    Call<List<CategoriaDTO>> listarCategorias();

    @GET("actividades/destacadas")
    Call<List<ActividadDTO>> obtenerDestacadas();

    @GET("reservas/mis")
    Call<List<ReservaDTO>> misReservas(@Query("estado") String estado);

    @GET("reservas/{id}")
    Call<ReservaDetalleDTO> obtenerReserva(@Path("id") long id);

    @POST("reservas/{id}/cancelar")
    Call<ReservaDetalleDTO> cancelarReserva(@Path("id") long id);

    @GET("reservas/historial")
    Call<List<ReservaDTO>> historial(
            @Query("destinoId") Long destinoId,
            @Query("fechaDesde") String fechaDesde,
            @Query("fechaHasta") String fechaHasta
    );

    @GET("actividades/{id}/horarios")
    Call<List<HorarioDTO>> getHorarios(
            @Path("id") long actividadId,
            @Query("fecha") String fecha
    );

    @POST("reservas")
    Call<ReservaDetalleDTO> crearReserva(@Body CrearReservaRequest body);

    // Punto 6 - Calificaciones
    @POST("reservas/{id}/calificacion")
    Call<CalificacionDTO> calificarReserva(
            @Path("id") long reservaId,
            @Body CalificacionRequest body
    );

    @GET("reservas/{id}/calificacion")
    Call<CalificacionDTO> obtenerCalificacion(@Path("id") long reservaId);

    // Punto 9 Noticias
    @GET("noticias")
    Call<List<NoticiaDTO>> listarNoticias();

    @GET("noticias/{id}")
    Call<NoticiaDTO> obtenerNoticia(@Path("id") long id);
}

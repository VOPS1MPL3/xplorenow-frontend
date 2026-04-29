package com.xplorenow.data.api;
import com.xplorenow.data.dto.ActividadDTO;
import com.xplorenow.data.dto.ActividadDetalleDTO;
import com.xplorenow.data.dto.CategoriaDTO;
import com.xplorenow.data.dto.DestinoDTO;
import com.xplorenow.data.dto.PageResponseDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
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
}
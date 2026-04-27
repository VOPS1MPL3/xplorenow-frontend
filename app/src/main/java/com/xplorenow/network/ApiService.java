package com.xplorenow.network;

import com.xplorenow.model.ActualizarPerfilRequest;
import com.xplorenow.model.CategoriaResponse;
import com.xplorenow.model.PerfilResponse;
import com.xplorenow.model.ReservaResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {

    @GET("perfil")
    Call<PerfilResponse> getPerfil();

    @PUT("perfil")
    Call<PerfilResponse> actualizarPerfil(@Body ActualizarPerfilRequest body);

    @PUT("perfil/preferencias")
    Call<Void> actualizarPreferencias(@Body Map<String, List<Long>> body);

    @GET("categorias")
    Call<List<CategoriaResponse>> getCategorias();

    @GET("reservas/mis")
    Call<List<ReservaResponse>> getMisReservas();

    @GET("reservas/mis")
    Call<List<ReservaResponse>> getMisReservasPorEstado(@Query("estado") String estado);
}
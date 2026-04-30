package com.xplorenow.network;

import com.xplorenow.model.ActualizarPerfilRequest;
import com.xplorenow.model.CategoriaResponse;
import com.xplorenow.model.PerfilResponse;
import com.xplorenow.model.ReservaResponse;
import com.xplorenow.model.auth.LoginRequest;
import com.xplorenow.model.auth.LoginResponse;
import com.xplorenow.model.auth.RegistroRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {

    // Auth endpoints
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @POST("auth/registro")
    Call<LoginResponse> register(@Body RegistroRequest body);

    // Profile endpoints
    @GET("perfil")
    Call<PerfilResponse> getPerfil();

    @PUT("perfil")
    Call<PerfilResponse> actualizarPerfil(@Body ActualizarPerfilRequest body);

    @PUT("perfil/preferencias")
    Call<Void> actualizarPreferencias(@Body Map<String, List<Long>> body);

    @GET("categorias")
    Call<List<CategoriaResponse>> getCategorias();

    // Activities and Reservations
    @GET("reservas/mis")
    Call<List<ReservaResponse>> getMisReservas();

    @GET("reservas/mis")
    Call<List<ReservaResponse>> getMisReservasPorEstado(@Query("estado") String estado);
}

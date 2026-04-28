package com.xplorenow.data.api;

import com.xplorenow.data.dto.ActividadDTO;
import com.xplorenow.data.dto.PageResponseDTO;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
public interface XploreNowApi {
    @GET("actividades")
    Call<PageResponseDTO<ActividadDTO>> listarActividades(
            @Query("page") int page,
            @Query("size") int size
    );
}
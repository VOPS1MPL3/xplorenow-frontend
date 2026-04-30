package com.xplorenow.network;

import com.xplorenow.auth.AuthResponse;
import com.xplorenow.auth.LoginRequest;
import com.xplorenow.auth.OtpConfirmarRequest;
import com.xplorenow.auth.OtpSolicitarRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/otp/solicitar")
    Call<Void> solicitarOtp(@Body OtpSolicitarRequest request);

    @POST("auth/otp/confirmar")
    Call<AuthResponse> confirmarOtp(@Body OtpConfirmarRequest request);
}
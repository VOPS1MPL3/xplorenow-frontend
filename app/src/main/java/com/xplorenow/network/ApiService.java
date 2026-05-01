package com.xplorenow.network;

import com.xplorenow.auth.AuthResponse;
import com.xplorenow.auth.LoginRequest;
import com.xplorenow.auth.OlvideContrasenaRequest;
import com.xplorenow.auth.OtpConfirmarRequest;
import com.xplorenow.auth.OtpSolicitarRequest;
import com.xplorenow.auth.RegistroRequest;
import com.xplorenow.auth.ResetContrasenaRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/registro")
    Call<AuthResponse> registro(@Body RegistroRequest request);

    @POST("auth/otp/solicitar")
    Call<Void> solicitarOtp(@Body OtpSolicitarRequest request);

    @POST("auth/otp/confirmar")
    Call<AuthResponse> confirmarOtp(@Body OtpConfirmarRequest request);

    // ===== Olvide mi contrasena =====

    @POST("auth/password/olvide")
    Call<Void> olvideContrasena(@Body OlvideContrasenaRequest request);

    @POST("auth/password/reset")
    Call<Void> resetContrasena(@Body ResetContrasenaRequest request);
}

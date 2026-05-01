package com.xplorenow.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.xplorenow.R;
import com.xplorenow.network.ApiService;
import com.xplorenow.util.TokenManager;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    @Inject
    ApiService apiService;

    @Inject
    TokenManager tokenManager;

    private TextView tvTitulo;
    private EditText etEmail;
    private EditText etPassword;
    private TextView tvOlvideContrasena;
    private Button btnLogin;
    private Button btnIrOtp;
    private Button btnRegistro;
    private Button btnBiometria;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitulo           = view.findViewById(R.id.tvTitulo);
        etEmail            = view.findViewById(R.id.etEmail);
        etPassword         = view.findViewById(R.id.etPassword);
        tvOlvideContrasena = view.findViewById(R.id.tvOlvideContrasena);
        btnLogin           = view.findViewById(R.id.btnLogin);
        btnIrOtp           = view.findViewById(R.id.btnIrOtp);
        btnRegistro        = view.findViewById(R.id.btnRegistro);
        btnBiometria       = view.findViewById(R.id.btnBiometria);

        // Mostrar biometría solo si el token existe Y no venció
        if (tokenManager.isTokenValid()) {
            btnBiometria.setVisibility(View.VISIBLE);
        } else {
            // Si hay token pero venció, lo limpiamos
            tokenManager.clearToken();
            btnBiometria.setVisibility(View.GONE);
        }

        // Login clásico
        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Completá todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            apiService.login(new LoginRequest(email, password))
                    .enqueue(new Callback<AuthResponse>() {
                        @Override
                        public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                tokenManager.saveToken(response.body().getToken());
                                Navigation.findNavController(view).navigate(R.id.action_login_to_home);
                            } else {
                                Toast.makeText(requireContext(), "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<AuthResponse> call, Throwable t) {
                            Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Ir a OTP
        btnIrOtp.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_login_to_otpSolicitar)
        );

        // Ir a registro
        btnRegistro.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_login_to_registro)
        );

        // Olvide mi contrasena: navegar al fragment que pide el email para
        // disparar el envio de OTP.
        tvOlvideContrasena.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_login_to_olvide)
        );

        // Biometría
        btnBiometria.setOnClickListener(v -> autenticarConBiometria(view));
    }

    private void autenticarConBiometria(View view) {
        // Doble chequeo: verificar validez justo antes de lanzar el prompt
        if (!tokenManager.isTokenValid()) {
            tokenManager.clearToken();
            btnBiometria.setVisibility(View.GONE);
            Toast.makeText(requireContext(),
                    "Sesión expirada, ingresá con tu contraseña",
                    Toast.LENGTH_LONG).show();
            return;
        }

        androidx.biometric.BiometricManager manager =
                androidx.biometric.BiometricManager.from(requireContext());

        int canAuth = manager.canAuthenticate(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG |
                androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );

        if (canAuth != androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(requireContext(), "Biometría no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        androidx.biometric.BiometricPrompt.PromptInfo info =
                new androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                        .setTitle("XploreNow")
                        .setSubtitle("Ingresá con tu huella")
                        .setAllowedAuthenticators(
                                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG |
                                androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )
                        .build();

        androidx.biometric.BiometricPrompt prompt = new androidx.biometric.BiometricPrompt(
                this,
                androidx.core.content.ContextCompat.getMainExecutor(requireContext()),
                new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                        // Token válido confirmado -> ir al Home
                        Navigation.findNavController(view).navigate(R.id.action_login_to_home);
                    }
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        Toast.makeText(requireContext(), "Error: " + errString, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onAuthenticationFailed() {
                        Toast.makeText(requireContext(), "Huella no reconocida", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        prompt.authenticate(info);
    }
}

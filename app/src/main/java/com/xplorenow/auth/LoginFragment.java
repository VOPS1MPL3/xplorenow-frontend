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
    private Button btnLogin;
    private Button btnIrOtp;
    private Button btnBiometria;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitulo = view.findViewById(R.id.tvTitulo);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnIrOtp = view.findViewById(R.id.btnIrOtp);
        btnBiometria = view.findViewById(R.id.btnBiometria);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
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
                                Toast.makeText(requireContext(), "Login exitoso", Toast.LENGTH_SHORT).show();
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
    }
}
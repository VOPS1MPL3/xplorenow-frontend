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
public class OtpConfirmarFragment extends Fragment {

    @Inject
    ApiService apiService;

    @Inject
    TokenManager tokenManager;

    private TextView tvEmailDestino;
    private EditText etCodigo;
    private Button btnConfirmarOtp;
    private Button btnReenviarOtp;
    private String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_otp_confirmar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEmailDestino = view.findViewById(R.id.tvEmailDestino);
        etCodigo = view.findViewById(R.id.etCodigo);
        btnConfirmarOtp = view.findViewById(R.id.btnConfirmarOtp);
        btnReenviarOtp = view.findViewById(R.id.btnReenviarOtp);

        if (getArguments() != null) {
            email = getArguments().getString("email", "");
            tvEmailDestino.setText("Código enviado a: " + email);
        }

        btnConfirmarOtp.setOnClickListener(v -> {
            String codigo = etCodigo.getText().toString().trim();

            if (codigo.length() != 6) {
                Toast.makeText(requireContext(), "Ingresá el código de 6 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

            apiService.confirmarOtp(new OtpConfirmarRequest(email, codigo))
                    .enqueue(new Callback<AuthResponse>() {
                        @Override
                        public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                tokenManager.saveToken(response.body().getToken());
                                // Navegar al Home limpiando el back stack
                                Navigation.findNavController(view)
                                        .navigate(R.id.action_otpConfirmar_to_home);
                            } else {
                                Toast.makeText(requireContext(), "Código incorrecto o expirado", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<AuthResponse> call, Throwable t) {
                            Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnReenviarOtp.setOnClickListener(v -> {
            apiService.solicitarOtp(new OtpSolicitarRequest(email))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            Toast.makeText(requireContext(), "Código reenviado", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}

package com.xplorenow.auth;

import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.google.android.material.appbar.MaterialToolbar;
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

    // El OTP dura 5 minutos en el backend
    private static final long TIMER_MS   = 5 * 60 * 1000L;
    private static final long TIMER_TICK = 1000L;

    @Inject
    ApiService apiService;

    @Inject
    TokenManager tokenManager;

    private MaterialToolbar toolbar;
    private TextView tvEmailDestino;
    private TextView tvTimer;
    private EditText etCodigo;
    private Button btnConfirmarOtp;
    private Button btnReenviarOtp;
    private String email;

    private CountDownTimer countDownTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_otp_confirmar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar         = view.findViewById(R.id.toolbar);
        tvEmailDestino  = view.findViewById(R.id.tvEmailDestino);
        tvTimer         = view.findViewById(R.id.tvTimer);
        etCodigo        = view.findViewById(R.id.etCodigo);
        btnConfirmarOtp = view.findViewById(R.id.btnConfirmarOtp);
        btnReenviarOtp  = view.findViewById(R.id.btnReenviarOtp);

        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        if (getArguments() != null) {
            email = getArguments().getString("email", "");
            tvEmailDestino.setText("Código enviado a: " + email);
        }

        iniciarTimer();

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
                            if (getView() == null) return;
                            if (response.isSuccessful() && response.body() != null) {
                                if (countDownTimer != null) countDownTimer.cancel();
                                tokenManager.saveToken(response.body().getToken());
                                Navigation.findNavController(view)
                                        .navigate(R.id.action_otpConfirmar_to_home);
                            } else {
                                Toast.makeText(requireContext(), "Código incorrecto o expirado", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<AuthResponse> call, Throwable t) {
                            if (getView() == null) return;
                            Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnReenviarOtp.setOnClickListener(v -> {
            btnReenviarOtp.setEnabled(false);

            apiService.solicitarOtp(new OtpSolicitarRequest(email))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (getView() == null) return;
                            Toast.makeText(requireContext(), "Código reenviado", Toast.LENGTH_SHORT).show();
                            iniciarTimer();
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            if (getView() == null) return;
                            btnReenviarOtp.setEnabled(true);
                            Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void iniciarTimer() {
        btnReenviarOtp.setEnabled(false);
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(TIMER_MS, TIMER_TICK) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (getView() == null) return;
                long min = millisUntilFinished / 60000;
                long seg = (millisUntilFinished % 60000) / 1000;
                tvTimer.setText(String.format("El código vence en %02d:%02d", min, seg));
                tvTimer.setVisibility(View.VISIBLE);
            }
            @Override
            public void onFinish() {
                if (getView() == null) return;
                tvTimer.setText("El código expiró");
                btnReenviarOtp.setEnabled(true);
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}
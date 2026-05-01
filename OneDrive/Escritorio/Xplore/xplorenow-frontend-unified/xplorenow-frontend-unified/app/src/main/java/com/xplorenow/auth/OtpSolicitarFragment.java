package com.xplorenow.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.xplorenow.R;
import com.xplorenow.network.ApiService;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class OtpSolicitarFragment extends Fragment {

    @Inject
    ApiService apiService;

    private EditText etEmail;
    private Button btnSolicitarOtp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_otp_solicitar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmail = view.findViewById(R.id.etEmail);
        btnSolicitarOtp = view.findViewById(R.id.btnSolicitarOtp);

        btnSolicitarOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresá tu email", Toast.LENGTH_SHORT).show();
                return;
            }

            apiService.solicitarOtp(new OtpSolicitarRequest(email))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            Bundle args = new Bundle();
                            args.putString("email", email);
                            Navigation.findNavController(view)
                                    .navigate(R.id.action_otpSolicitar_to_otpConfirmar, args);
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
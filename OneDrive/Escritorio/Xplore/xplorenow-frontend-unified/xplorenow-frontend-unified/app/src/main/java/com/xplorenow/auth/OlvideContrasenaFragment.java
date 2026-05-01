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

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla "Olvide mi contrasena" (paso 1 de 2).
 *
 * El usuario ingresa el email de su cuenta. El backend genera un OTP y lo
 * envia por email (mismo mecanismo que el login OTP). Despues navegamos a
 * ResetPasswordFragment pasando el email como argumento, para que el usuario
 * complete el codigo y la nueva contrasena.
 *
 * Decision UX: el backend siempre responde 200 OK aunque el email no exista
 * (asi no revelamos cuentas registradas). Por eso desde aca SIEMPRE pasamos
 * a la siguiente pantalla — si el email no existia, el OTP nunca llega y el
 * intento de cambio fallara con "codigo invalido".
 */
@AndroidEntryPoint
public class OlvideContrasenaFragment extends Fragment {

    @Inject
    ApiService apiService;

    private EditText etEmail;
    private Button btnEnviarCodigo;
    private Button btnVolver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_olvide_contrasena, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmail         = view.findViewById(R.id.etEmail);
        btnEnviarCodigo = view.findViewById(R.id.btnEnviarCodigo);
        btnVolver       = view.findViewById(R.id.btnVolver);

        btnEnviarCodigo.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Ingresá tu email",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            btnEnviarCodigo.setEnabled(false);

            apiService.olvideContrasena(new OlvideContrasenaRequest(email))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            btnEnviarCodigo.setEnabled(true);
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(),
                                        "Si el email está registrado, te llegará un código",
                                        Toast.LENGTH_LONG).show();

                                Bundle args = new Bundle();
                                args.putString("email", email);
                                Navigation.findNavController(view)
                                        .navigate(R.id.action_olvide_to_reset, args);
                            } else {
                                Toast.makeText(requireContext(),
                                        "No se pudo enviar el código (HTTP "
                                                + response.code() + ")",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            btnEnviarCodigo.setEnabled(true);
                            Toast.makeText(requireContext(),
                                    "Error de conexión",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnVolver.setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );
    }
}

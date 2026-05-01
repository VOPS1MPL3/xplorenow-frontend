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

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla "Olvide mi contrasena" (paso 2 de 2).
 *
 * El usuario ingresa el OTP que recibio por email + la nueva contrasena
 * (con confirmacion). Si el OTP es valido, el backend pisa la contrasena
 * vieja con la nueva y volvemos al login.
 *
 * Recibe el email como argumento desde OlvideContrasenaFragment.
 */
@AndroidEntryPoint
public class ResetPasswordFragment extends Fragment {

    @Inject
    ApiService apiService;

    private TextView tvAyuda;
    private EditText etCodigo;
    private EditText etPassword;
    private EditText etPasswordConfirm;
    private Button btnGuardar;
    private Button btnVolver;

    private String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            email = getArguments().getString("email", "");
        }

        tvAyuda           = view.findViewById(R.id.tvAyuda);
        etCodigo          = view.findViewById(R.id.etCodigo);
        etPassword        = view.findViewById(R.id.etPassword);
        etPasswordConfirm = view.findViewById(R.id.etPasswordConfirm);
        btnGuardar        = view.findViewById(R.id.btnGuardar);
        btnVolver         = view.findViewById(R.id.btnVolver);

        if (email != null && !email.isEmpty()) {
            tvAyuda.setText("Te enviamos un código a " + email
                    + ". Ingresalo y elegí una nueva contraseña.");
        }

        btnGuardar.setOnClickListener(v -> {
            String codigo   = etCodigo.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm  = etPasswordConfirm.getText().toString().trim();

            if (codigo.length() != 6) {
                Toast.makeText(requireContext(),
                        "El código debe tener 6 dígitos",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(requireContext(),
                        "La contraseña debe tener al menos 6 caracteres",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(requireContext(),
                        "Las contraseñas no coinciden",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            btnGuardar.setEnabled(false);

            apiService.resetContrasena(new ResetContrasenaRequest(email, codigo, password))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            btnGuardar.setEnabled(true);
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(),
                                        "Contraseña actualizada. Iniciá sesión con la nueva.",
                                        Toast.LENGTH_LONG).show();
                                // Volvemos al login limpiando el back stack
                                // de las pantallas de recuperacion.
                                Navigation.findNavController(view)
                                        .navigate(R.id.action_reset_to_login);
                            } else if (response.code() == 401) {
                                Toast.makeText(requireContext(),
                                        "Código inválido o expirado",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(),
                                        "No se pudo cambiar la contraseña (HTTP "
                                                + response.code() + ")",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            btnGuardar.setEnabled(true);
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

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
public class RegistroFragment extends Fragment {

    @Inject
    ApiService apiService;

    @Inject
    TokenManager tokenManager;

    private MaterialToolbar toolbar;
    private EditText etNombre;
    private EditText etApellido;
    private EditText etEmail;
    private EditText etTelefono;
    private EditText etPassword;
    private EditText etPasswordConfirm;
    private Button btnRegistrar;
    private Button btnVolver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar           = view.findViewById(R.id.toolbar);
        etNombre          = view.findViewById(R.id.etNombre);
        etApellido        = view.findViewById(R.id.etApellido);
        etEmail           = view.findViewById(R.id.etEmail);
        etTelefono        = view.findViewById(R.id.etTelefono);
        etPassword        = view.findViewById(R.id.etPassword);
        etPasswordConfirm = view.findViewById(R.id.etPasswordConfirm);
        btnRegistrar      = view.findViewById(R.id.btnRegistrar);
        btnVolver         = view.findViewById(R.id.btnVolver);

        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        btnRegistrar.setOnClickListener(v -> {
            String nombre   = etNombre.getText().toString().trim();
            String apellido = etApellido.getText().toString().trim();
            String email    = etEmail.getText().toString().trim();
            String telefono = etTelefono.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm  = etPasswordConfirm.getText().toString().trim();

            if (nombre.isEmpty() || apellido.isEmpty()
                    || email.isEmpty() || telefono.isEmpty()
                    || password.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Completá todos los campos",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(requireContext(),
                        "Las contraseñas no coinciden",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(requireContext(),
                        "La contraseña debe tener al menos 6 caracteres",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegistrar.setEnabled(false);

            apiService.registro(new RegistroRequest(
                            email, password, nombre, apellido, telefono))
                    .enqueue(new Callback<AuthResponse>() {
                        @Override
                        public void onResponse(Call<AuthResponse> call,
                                               Response<AuthResponse> response) {
                            btnRegistrar.setEnabled(true);
                            if (response.isSuccessful() && response.body() != null) {
                                tokenManager.saveToken(response.body().getToken());
                                Navigation.findNavController(view)
                                        .navigate(R.id.action_registro_to_home);
                            } else if (response.code() == 400) {
                                Toast.makeText(requireContext(),
                                        "El email ya está registrado",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(),
                                        "Error al registrarse, intentá de nuevo",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<AuthResponse> call, Throwable t) {
                            btnRegistrar.setEnabled(true);
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
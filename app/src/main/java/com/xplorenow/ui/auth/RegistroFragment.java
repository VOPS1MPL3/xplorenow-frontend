package com.xplorenow.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.xplorenow.R;
import com.xplorenow.model.auth.LoginResponse;
import com.xplorenow.model.auth.RegistroRequest;
import com.xplorenow.network.ApiService;
import com.xplorenow.util.TokenManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class RegistroFragment extends Fragment {

    @Inject ApiService apiService;
    @Inject TokenManager tokenManager;

    private TextInputEditText etNombre, etEmail, etPassword, etTelefono;
    private MaterialButton btnRegistro, btnIrALogin;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etNombre = view.findViewById(R.id.etNombre);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etTelefono = view.findViewById(R.id.etTelefono);
        btnRegistro = view.findViewById(R.id.btnRegistro);
        btnIrALogin = view.findViewById(R.id.btnIrALogin);
        progressBar = view.findViewById(R.id.progressBar);

        btnRegistro.setOnClickListener(v -> registrar());
        btnIrALogin.setOnClickListener(v -> 
            Navigation.findNavController(v).popBackStack());
    }

    private void registrar() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Nombre, email y contraseña son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegistro.setEnabled(false);

        RegistroRequest request = new RegistroRequest(email, password, nombre, telefono);

        apiService.register(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                btnRegistro.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    tokenManager.saveToken(response.body().getToken());
                    Toast.makeText(requireContext(), "Registro exitoso", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigate(R.id.action_login_to_home);
                } else {
                    Toast.makeText(requireContext(), "Error al registrarse", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                btnRegistro.setEnabled(true);
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.xplorenow.ui.perfil;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.xplorenow.R;
import com.xplorenow.model.ActualizarPerfilRequest;
import com.xplorenow.model.PerfilResponse;
import com.xplorenow.network.ApiService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class EditarPerfilFragment extends Fragment {

    private static final String TAG = "EditarPerfilFragment";

    @Inject ApiService apiService;

    private ImageView ivFotoEditar;
    private TextInputEditText etNombre, etEmail, etTelefono;
    private MaterialButton btnGuardar, btnCambiarFoto;
    private ImageButton btnVolver;
    private ProgressBar progressBar;

    private String fotoUrlActual = null;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null && isAdded()) {
                            Glide.with(this)
                                    .load(uri)
                                    .circleCrop()
                                    .into(ivFotoEditar);
                            fotoUrlActual = uri.toString();
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editar_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivFotoEditar = view.findViewById(R.id.ivFotoEditar);
        etNombre = view.findViewById(R.id.etNombre);
        etEmail = view.findViewById(R.id.etEmail);
        etTelefono = view.findViewById(R.id.etTelefono);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        btnCambiarFoto = view.findViewById(R.id.btnCambiarFoto);
        btnVolver = view.findViewById(R.id.btnVolver);
        progressBar = view.findViewById(R.id.progressBar);

        btnVolver.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        btnCambiarFoto.setOnClickListener(v ->
                pickImageLauncher.launch("image/*"));

        btnGuardar.setOnClickListener(v -> guardarPerfil());

        cargarPerfilActual();
    }

    private void cargarPerfilActual() {
        apiService.getPerfil().enqueue(new Callback<PerfilResponse>() {
            @Override
            public void onResponse(Call<PerfilResponse> call, Response<PerfilResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    PerfilResponse perfil = response.body();
                    etNombre.setText(perfil.getNombre());
                    etEmail.setText(perfil.getEmail());
                    etTelefono.setText(perfil.getTelefono());
                    fotoUrlActual = perfil.getFotoUrl();

                    if (fotoUrlActual != null && !fotoUrlActual.isEmpty()) {
                        Glide.with(EditarPerfilFragment.this)
                                .load(fotoUrlActual)
                                .placeholder(R.drawable.ic_default_avatar)
                                .circleCrop()
                                .into(ivFotoEditar);
                    }
                }
            }

            @Override
            public void onFailure(Call<PerfilResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarPerfil() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String telefono = etTelefono.getText() != null ? etTelefono.getText().toString().trim() : "";

        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(false);

        ActualizarPerfilRequest request = new ActualizarPerfilRequest(nombre, telefono, fotoUrlActual);

        apiService.actualizarPerfil(request).enqueue(new Callback<PerfilResponse>() {
            @Override
            public void onResponse(Call<PerfilResponse> call, Response<PerfilResponse> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PerfilResponse> call, Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
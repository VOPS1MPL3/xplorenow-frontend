package com.xplorenow.ui.perfil;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.xplorenow.R;
import com.xplorenow.model.CategoriaResponse;
import com.xplorenow.model.PerfilResponse;
import com.xplorenow.model.ReservaResponse;
import com.xplorenow.network.ApiService;
import com.xplorenow.util.TokenManager;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class PerfilFragment extends Fragment {

    private static final String TAG = "PerfilFragment";

    @Inject ApiService apiService;
    @Inject TokenManager tokenManager;

    private ImageView ivFotoPerfil;
    private TextView tvNombreHeader, tvEmailHeader;
    private TextView tvNombre, tvEmail, tvTelefono;
    private TextView tvPreferencias;
    private ChipGroup chipGroupPreferencias;
    private TextView tvConfirmadas, tvFinalizadas, tvCanceladas;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivFotoPerfil = view.findViewById(R.id.ivFotoPerfil);
        tvNombreHeader = view.findViewById(R.id.tvNombreHeader);
        tvEmailHeader = view.findViewById(R.id.tvEmailHeader);
        tvNombre = view.findViewById(R.id.tvNombre);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvTelefono = view.findViewById(R.id.tvTelefono);
        tvPreferencias = view.findViewById(R.id.tvPreferencias);
        chipGroupPreferencias = view.findViewById(R.id.chipGroupPreferencias);
        tvConfirmadas = view.findViewById(R.id.tvConfirmadas);
        tvFinalizadas = view.findViewById(R.id.tvFinalizadas);
        tvCanceladas = view.findViewById(R.id.tvCanceladas);

        MaterialButton btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        MaterialButton btnEditarPreferencias = view.findViewById(R.id.btnEditarPreferencias);
        MaterialButton btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);

        btnEditarPerfil.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_perfil_to_editarPerfil));

        btnEditarPreferencias.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_perfil_to_preferencias));

        btnCerrarSesion.setOnClickListener(v -> {
            tokenManager.clearToken();
            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();
        });

        cargarPerfil();
        cargarResumenReservas();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarPerfil();
    }

    private void cargarPerfil() {
        apiService.getPerfil().enqueue(new Callback<PerfilResponse>() {
            @Override
            public void onResponse(Call<PerfilResponse> call, Response<PerfilResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    mostrarPerfil(response.body());
                } else if (response.code() == 401) {
                    Toast.makeText(requireContext(), "Sesión expirada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PerfilResponse> call, Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Error de red: " + t.getMessage());
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarPerfil(PerfilResponse perfil) {
        tvNombreHeader.setText(perfil.getNombre() != null ? perfil.getNombre() : "Sin nombre");
        tvEmailHeader.setText(perfil.getEmail());
        tvNombre.setText(perfil.getNombre() != null ? perfil.getNombre() : "-");
        tvEmail.setText(perfil.getEmail());
        tvTelefono.setText(perfil.getTelefono() != null ? perfil.getTelefono() : "-");

        if (perfil.getFotoUrl() != null && !perfil.getFotoUrl().isEmpty()) {
            Glide.with(this)
                    .load(perfil.getFotoUrl())
                    .placeholder(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(ivFotoPerfil);
        }

        chipGroupPreferencias.removeAllViews();
        List<CategoriaResponse> prefs = perfil.getPreferencias();
        if (prefs != null && !prefs.isEmpty()) {
            tvPreferencias.setVisibility(View.GONE);
            for (CategoriaResponse cat : prefs) {
                Chip chip = new Chip(requireContext());
                chip.setText(cat.getNombre());
                chip.setCheckable(false);
                chipGroupPreferencias.addView(chip);
            }
        } else {
            tvPreferencias.setVisibility(View.VISIBLE);
            tvPreferencias.setText("Sin preferencias configuradas");
        }
    }

    private void cargarResumenReservas() {
        apiService.getMisReservas().enqueue(new Callback<List<ReservaResponse>>() {
            @Override
            public void onResponse(Call<List<ReservaResponse>> call,
                                   Response<List<ReservaResponse>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    int confirmadas = 0, finalizadas = 0, canceladas = 0;
                    for (ReservaResponse r : response.body()) {
                        switch (r.getEstado()) {
                            case "CONFIRMADA": confirmadas++; break;
                            case "FINALIZADA": finalizadas++; break;
                            case "CANCELADA":  canceladas++;  break;
                        }
                    }
                    tvConfirmadas.setText(String.valueOf(confirmadas));
                    tvFinalizadas.setText(String.valueOf(finalizadas));
                    tvCanceladas.setText(String.valueOf(canceladas));
                }
            }

            @Override
            public void onFailure(Call<List<ReservaResponse>> call, Throwable t) {
                Log.e(TAG, "Error cargando reservas: " + t.getMessage());
            }
        });
    }
}
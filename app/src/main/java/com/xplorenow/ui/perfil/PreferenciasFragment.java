package com.xplorenow.ui.perfil;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.xplorenow.R;
import com.xplorenow.model.CategoriaResponse;
import com.xplorenow.model.PerfilResponse;
import com.xplorenow.network.ApiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class PreferenciasFragment extends Fragment {

    private static final String TAG = "PreferenciasFragment";

    @Inject ApiService apiService;

    private ChipGroup chipGroupCategorias;
    private MaterialButton btnGuardar;
    private ImageButton btnVolver;
    private ProgressBar progressBar;

    private List<CategoriaResponse> todasLasCategorias = new ArrayList<>();
    private List<Long> preferenciasActuales = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preferencias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chipGroupCategorias = view.findViewById(R.id.chipGroupCategorias);
        btnGuardar = view.findViewById(R.id.btnGuardarPreferencias);
        btnVolver = view.findViewById(R.id.btnVolver);
        progressBar = view.findViewById(R.id.progressBar);

        btnVolver.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        btnGuardar.setOnClickListener(v -> guardarPreferencias());

        cargarPreferenciasActuales();
    }

    private void cargarPreferenciasActuales() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getPerfil().enqueue(new Callback<PerfilResponse>() {
            @Override
            public void onResponse(Call<PerfilResponse> call, Response<PerfilResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<CategoriaResponse> prefs = response.body().getPreferencias();
                    if (prefs != null) {
                        for (CategoriaResponse c : prefs) {
                            preferenciasActuales.add(c.getId());
                        }
                    }
                }
                cargarCategorias();
            }

            @Override
            public void onFailure(Call<PerfilResponse> call, Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarCategorias() {
        apiService.getCategorias().enqueue(new Callback<List<CategoriaResponse>>() {
            @Override
            public void onResponse(Call<List<CategoriaResponse>> call,
                                   Response<List<CategoriaResponse>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    todasLasCategorias = response.body();
                    mostrarCategorias();
                }
            }

            @Override
            public void onFailure(Call<List<CategoriaResponse>> call, Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error cargando categorías", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarCategorias() {
        chipGroupCategorias.removeAllViews();

        for (CategoriaResponse cat : todasLasCategorias) {
            Chip chip = new Chip(requireContext());
            chip.setText(cat.getNombre());
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);

            if (preferenciasActuales.contains(cat.getId())) {
                chip.setChecked(true);
            }

            chip.setTag(cat.getId());
            chipGroupCategorias.addView(chip);
        }
    }

    private void guardarPreferencias() {
        List<Long> seleccionadas = new ArrayList<>();

        for (int i = 0; i < chipGroupCategorias.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategorias.getChildAt(i);
            if (chip.isChecked()) {
                seleccionadas.add((Long) chip.getTag());
            }
        }

        progressBar.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(false);

        Map<String, List<Long>> body = new HashMap<>();
        body.put("categoriaIds", seleccionadas);

        apiService.actualizarPreferencias(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(),
                            "Preferencias actualizadas", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    Toast.makeText(requireContext(),
                            "Error al guardar preferencias", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
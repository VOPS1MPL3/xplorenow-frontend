package com.xplorenow.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.xplorenow.R;
import com.xplorenow.data.dto.ActividadDTO;
import com.xplorenow.data.dto.PageResponseDTO;
import com.xplorenow.data.repository.ActividadRepository;
import com.xplorenow.databinding.FragmentHomeBinding;
import com.xplorenow.ui.home.detalle.DetalleFragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;
    private ActividadAdapter adapter;
    private final List<ActividadDTO> actividades = new ArrayList<>();

    @Inject
    ActividadRepository actividadRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ActividadAdapter(requireContext(), actividades);
        binding.lvActividades.setAdapter(adapter);

        // Click en una tarjeta -> ir al detalle
        binding.lvActividades.setOnItemClickListener((parent, v, position, id) -> {
            ActividadDTO seleccionada = actividades.get(position);
            Bundle args = new Bundle();
            args.putLong(DetalleFragment.ARG_ACTIVIDAD_ID, seleccionada.getId());
            Navigation.findNavController(v).navigate(
                    R.id.action_home_to_detalle, args);
        });

        cargarActividades();
    }

    private void cargarActividades() {
        binding.tvStatus.setText("Cargando...");
        binding.tvStatus.setVisibility(View.VISIBLE);
        binding.lvActividades.setVisibility(View.GONE);

        actividadRepository.listarActividades(0, 20).enqueue(
                new Callback<PageResponseDTO<ActividadDTO>>() {
                    @Override
                    public void onResponse(
                            Call<PageResponseDTO<ActividadDTO>> call,
                            Response<PageResponseDTO<ActividadDTO>> response) {
                        if (binding == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            mostrarLista(response.body().getContent());
                        } else {
                            mostrarError("Error HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PageResponseDTO<ActividadDTO>> call,
                            Throwable t) {
                        if (binding == null) return;
                        mostrarError("Error de red: " + t.getMessage());
                        Log.e(TAG, "onFailure", t);
                    }
                });
    }

    private void mostrarLista(List<ActividadDTO> recibidas) {
        if (recibidas == null || recibidas.isEmpty()) {
            mostrarError("No hay actividades disponibles");
            return;
        }
        actividades.clear();
        actividades.addAll(recibidas);
        adapter.notifyDataSetChanged();
        binding.tvStatus.setVisibility(View.GONE);
        binding.lvActividades.setVisibility(View.VISIBLE);
    }

    private void mostrarError(String mensaje) {
        binding.tvStatus.setText(mensaje);
        binding.tvStatus.setVisibility(View.VISIBLE);
        binding.lvActividades.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
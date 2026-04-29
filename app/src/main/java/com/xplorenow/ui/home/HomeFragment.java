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
import com.xplorenow.data.dto.FiltrosActividad;
import com.xplorenow.data.dto.PageResponseDTO;
import com.xplorenow.data.repository.ActividadRepository;
import com.xplorenow.databinding.FragmentHomeBinding;
import com.xplorenow.ui.home.detalle.DetalleFragment;
import com.xplorenow.ui.home.filtros.FiltrosFragment;

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

    private FiltrosActividad filtrosActuales = null;

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

        // Menu con la accion "Filtrar" en la toolbar local del fragment
                binding.toolbar.inflateMenu(R.menu.menu_home);
                binding.toolbar.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_filtrar) {
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_home_to_filtros);
                        return true;
                    }
                    return false;
                });

        getParentFragmentManager().setFragmentResultListener(
                FiltrosFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    filtrosActuales = bundleAFiltros(bundle);
                    actualizarChipFiltros();
                    cargarActividades();
                }
        );

        adapter = new ActividadAdapter(requireContext(), actividades);
        binding.lvActividades.setAdapter(adapter);

        binding.btnLimpiarFiltros.setOnClickListener(v -> {
            filtrosActuales = null;
            actualizarChipFiltros();
            cargarActividades();
        });

        binding.lvActividades.setOnItemClickListener((parent, v, position, id) -> {
            ActividadDTO seleccionada = actividades.get(position);
            Bundle args = new Bundle();
            args.putLong(DetalleFragment.ARG_ACTIVIDAD_ID, seleccionada.getId());
            Navigation.findNavController(v).navigate(
                    R.id.action_home_to_detalle, args);
        });


        cargarActividades();
    }

    /** Convierte el Bundle que devuelve FiltrosFragment en un objeto FiltrosActividad. */
    private FiltrosActividad bundleAFiltros(Bundle b) {
        if (b == null || b.isEmpty()) return null;
        FiltrosActividad f = new FiltrosActividad();
        if (b.containsKey(FiltrosFragment.ARG_DESTINO_ID))
            f.setDestinoId(b.getLong(FiltrosFragment.ARG_DESTINO_ID));
        if (b.containsKey(FiltrosFragment.ARG_CATEGORIA_ID))
            f.setCategoriaId(b.getLong(FiltrosFragment.ARG_CATEGORIA_ID));
        if (b.containsKey(FiltrosFragment.ARG_FECHA_DESDE))
            f.setFechaDesde(b.getString(FiltrosFragment.ARG_FECHA_DESDE));
        if (b.containsKey(FiltrosFragment.ARG_FECHA_HASTA))
            f.setFechaHasta(b.getString(FiltrosFragment.ARG_FECHA_HASTA));
        if (b.containsKey(FiltrosFragment.ARG_PRECIO_MIN))
            f.setPrecioMin(b.getDouble(FiltrosFragment.ARG_PRECIO_MIN));
        if (b.containsKey(FiltrosFragment.ARG_PRECIO_MAX))
            f.setPrecioMax(b.getDouble(FiltrosFragment.ARG_PRECIO_MAX));
        return f.estaVacio() ? null : f;
    }
    private void actualizarChipFiltros() {
        if (filtrosActuales == null || filtrosActuales.estaVacio()) {
            binding.llFiltrosActivos.setVisibility(View.GONE);
        } else {
            binding.llFiltrosActivos.setVisibility(View.VISIBLE);
            binding.tvFiltrosActivos.setText(
                    "Filtros aplicados: " + describirFiltros(filtrosActuales));
        }
    }
    private String describirFiltros(FiltrosActividad f) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        if (f.getDestinoId() != null) { count++; }
        if (f.getCategoriaId() != null) { count++; }
        if (f.getFechaDesde() != null || f.getFechaHasta() != null) { count++; }
        if (f.getPrecioMin() != null || f.getPrecioMax() != null) { count++; }
        sb.append(count).append(count == 1 ? " filtro" : " filtros");
        return sb.toString();
    }
    private void cargarActividades() {
        binding.tvStatus.setText("Cargando...");
        binding.tvStatus.setVisibility(View.VISIBLE);
        binding.lvActividades.setVisibility(View.GONE);

        actividadRepository.listarActividades(0, 20, filtrosActuales).enqueue(
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
            mostrarError("No hay actividades que coincidan con los filtros");
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
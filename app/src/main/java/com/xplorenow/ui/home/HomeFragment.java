package com.xplorenow.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.xplorenow.R;
import com.xplorenow.data.dto.ActividadDTO;
import com.xplorenow.data.dto.FiltrosActividad;
import com.xplorenow.data.dto.PageResponseDTO;
import com.xplorenow.data.repository.ActividadRepository;
import com.xplorenow.data.util.PrecioFormatter;
import com.xplorenow.databinding.FragmentHomeBinding;
import com.xplorenow.databinding.FooterCargarMasBinding;
import com.xplorenow.databinding.HeaderDestacadasBinding;
import com.xplorenow.databinding.ItemDestacadaBinding;
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
    private static final int PAGE_SIZE = 5;
    private FragmentHomeBinding binding;
    private HeaderDestacadasBinding headerBinding;
    private FooterCargarMasBinding footerBinding;
    private ActividadAdapter adapter;
    private final List<ActividadDTO> actividades = new ArrayList<>();
    private FiltrosActividad filtrosActuales = null;
    private int paginaActual = 0;
    private boolean esUltimaPagina = false;
    private boolean cargando = false;

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

        // Menu de filtrar
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
                    reiniciarYRecargar();
                }
        );

        // Header (destacadas + titulo "Catalogo completo")
        headerBinding = HeaderDestacadasBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.lvActividades, false);
        binding.lvActividades.addHeaderView(headerBinding.getRoot(), null, false);

        // Footer (boton "Cargar mas")
        footerBinding = FooterCargarMasBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.lvActividades, false);
        binding.lvActividades.addFooterView(footerBinding.getRoot(), null, false);
        footerBinding.btnCargarMas.setOnClickListener(v -> cargarSiguientePagina());

        adapter = new ActividadAdapter(requireContext(), actividades);
        binding.lvActividades.setAdapter(adapter);

        binding.lvActividades.setOnItemClickListener((parent, v, position, id) -> {
            // position es relativo al adapter -- el header se descuenta automaticamente
            Object item = parent.getItemAtPosition(position);
            if (!(item instanceof ActividadDTO)) {
                return; // tap en header o footer
            }
            ActividadDTO seleccionada = (ActividadDTO) item;
            Bundle args = new Bundle();
            args.putLong(DetalleFragment.ARG_ACTIVIDAD_ID, seleccionada.getId());
            Navigation.findNavController(v).navigate(
                    R.id.action_home_to_detalle, args);
        });

        cargarDestacadas();
        cargarPrimeraPagina();
    }

    private void cargarDestacadas() {
        actividadRepository.obtenerDestacadas().enqueue(
                new Callback<List<ActividadDTO>>() {
                    @Override
                    public void onResponse(Call<List<ActividadDTO>> call,
                                           Response<List<ActividadDTO>> response) {
                        if (binding == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            mostrarDestacadas(response.body());
                        } else {
                            // Sin destacadas, ocultamos toda la seccion
                            ocultarDestacadas();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ActividadDTO>> call, Throwable t) {
                        if (binding == null) return;
                        Log.e(TAG, "destacadas onFailure", t);
                        ocultarDestacadas();
                    }
                });
    }

    private void mostrarDestacadas(List<ActividadDTO> recibidas) {
        headerBinding.llDestacadasContainer.removeAllViews();
        if (recibidas == null || recibidas.isEmpty()) {
            ocultarDestacadas();
            return;
        }
        for (ActividadDTO a : recibidas) {
            ItemDestacadaBinding ib = ItemDestacadaBinding.inflate(
                    LayoutInflater.from(requireContext()),
                    headerBinding.llDestacadasContainer, false);
            ib.tvDestacadaNombre.setText(a.getNombre());
            ib.tvDestacadaPrecio.setText(PrecioFormatter.format(a.getPrecio()));
            Glide.with(this)
                    .load(a.getImagenPrincipal())
                    .placeholder(android.R.color.darker_gray)
                    .into(ib.ivDestacadaImagen);

            // Tap en una destacada -> ir al detalle
            ib.getRoot().setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putLong(DetalleFragment.ARG_ACTIVIDAD_ID, a.getId());
                Navigation.findNavController(v).navigate(
                        R.id.action_home_to_detalle, args);
            });

            headerBinding.llDestacadasContainer.addView(ib.getRoot());
        }
        headerBinding.tvDestacadasTitulo.setVisibility(View.VISIBLE);
        headerBinding.hsvDestacadas.setVisibility(View.VISIBLE);
    }

    private void ocultarDestacadas() {
        headerBinding.tvDestacadasTitulo.setVisibility(View.GONE);
        headerBinding.hsvDestacadas.setVisibility(View.GONE);
    }

    private void reiniciarYRecargar() {
        actividades.clear();
        adapter.notifyDataSetChanged();
        paginaActual = 0;
        esUltimaPagina = false;
        footerBinding.btnCargarMas.setVisibility(View.VISIBLE);
        footerBinding.tvFinDeLista.setVisibility(View.GONE);
        cargarPrimeraPagina();
    }

    private void cargarPrimeraPagina() {
        binding.tvStatus.setText("Cargando...");
        binding.tvStatus.setVisibility(View.VISIBLE);
        binding.lvActividades.setVisibility(View.GONE);
        cargarPagina(0);
    }

    private void cargarSiguientePagina() {
        if (cargando || esUltimaPagina) return;
        cargarPagina(paginaActual + 1);
    }

    private void cargarPagina(int pagina) {
        if (cargando) return;
        cargando = true;
        footerBinding.btnCargarMas.setEnabled(false);
        footerBinding.btnCargarMas.setText("Cargando...");

        actividadRepository.listarActividades(pagina, PAGE_SIZE, filtrosActuales).enqueue(
                new Callback<PageResponseDTO<ActividadDTO>>() {
                    @Override
                    public void onResponse(
                            Call<PageResponseDTO<ActividadDTO>> call,
                            Response<PageResponseDTO<ActividadDTO>> response) {
                        cargando = false;
                        if (binding == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            paginaActual = response.body().getNumber();
                            esUltimaPagina = response.body().isLast();
                            agregarActividades(response.body().getContent());
                        } else {
                            mostrarError("Error HTTP " + response.code());
                        }
                        actualizarFooter();
                    }

                    @Override
                    public void onFailure(
                            Call<PageResponseDTO<ActividadDTO>> call,
                            Throwable t) {
                        cargando = false;
                        if (binding == null) return;
                        mostrarError("Error de red: " + t.getMessage());
                        actualizarFooter();
                        Log.e(TAG, "onFailure", t);
                    }
                });
    }

    private void agregarActividades(List<ActividadDTO> recibidas) {
        if (paginaActual == 0) {
            actividades.clear();
        }
        if (recibidas != null) {
            actividades.addAll(recibidas);
        }

        if (actividades.isEmpty()) {
            mostrarError("No hay actividades que coincidan con los filtros");
            return;
        }

        adapter.notifyDataSetChanged();
        binding.tvStatus.setVisibility(View.GONE);
        binding.lvActividades.setVisibility(View.VISIBLE);
    }

    private void actualizarFooter() {
        if (esUltimaPagina) {
            footerBinding.btnCargarMas.setVisibility(View.GONE);
            footerBinding.tvFinDeLista.setVisibility(View.VISIBLE);
        } else {
            footerBinding.btnCargarMas.setVisibility(View.VISIBLE);
            footerBinding.btnCargarMas.setEnabled(true);
            footerBinding.btnCargarMas.setText("Cargar mas");
            footerBinding.tvFinDeLista.setVisibility(View.GONE);
        }
    }

    private void mostrarError(String mensaje) {
        binding.tvStatus.setText(mensaje);
        binding.tvStatus.setVisibility(View.VISIBLE);
        binding.lvActividades.setVisibility(View.GONE);
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        headerBinding = null;
        footerBinding = null;
    }
}
package com.xplorenow.ui.home;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.xplorenow.R;
import com.xplorenow.data.dto.ActividadDTO;
import com.xplorenow.data.dto.FiltrosActividad;
import com.xplorenow.data.dto.PageResponseDTO;
import com.xplorenow.data.repository.ActividadRepository;
import com.xplorenow.data.util.PrecioFormatter;
import com.xplorenow.ui.home.detalle.DetalleFragment;
import com.xplorenow.ui.home.filtros.FiltrosFragment;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.xplorenow.data.dto.NoticiaDTO;
import com.xplorenow.data.dto.FavoritoDTO;
import com.xplorenow.data.repository.NoticiaRepository;
import com.xplorenow.data.repository.FavoritoRepository;
import com.xplorenow.ui.home.NoticiaAdapter;
import android.widget.Button;
import android.widget.Toast;
import java.util.HashSet;
import java.util.Set;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final int PAGE_SIZE = 5;

    private MaterialToolbar toolbar;
    private TextView tvStatus;
    private ListView lvActividades;

    // Header views
    private TextView tvDestacadasTitulo;
    private HorizontalScrollView hsvDestacadas;
    private LinearLayout llDestacadasContainer;
    private TextView tvNoticiasTitulo;
    private ListView lvNoticias;
    private NoticiaAdapter noticiaAdapter;
    private final List<NoticiaDTO> noticias = new ArrayList<>();
    private Button btnVerTodasNoticias;

    // Footer views
    private android.widget.Button btnCargarMas;
    private TextView tvFinDeLista;

    private ActividadAdapter adapter;
    private final List<ActividadDTO> actividades = new ArrayList<>();

    private FiltrosActividad filtrosActuales = null;
    private int paginaActual = 0;
    private boolean esUltimaPagina = false;
    private boolean cargando = false;
    private int requestToken = 0;

    @Inject
    ActividadRepository actividadRepository;
    @Inject
    NoticiaRepository noticiaRepository;
    @Inject
    FavoritoRepository favoritoRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.toolbar);
        tvStatus = view.findViewById(R.id.tvStatus);
        lvActividades = view.findViewById(R.id.lvActividades);

        // Menu de filtrar
        toolbar.inflateMenu(R.menu.menu_home);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_filtrar) {
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_home_to_filtros);
                return true;
            }
            return false;
        });

        // Resultado de los filtros
        getParentFragmentManager().setFragmentResultListener(
                FiltrosFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    Log.d(TAG, "Resultado recibido - bundle keys: " + bundle.keySet()
                            + " - destinoId del bundle: "
                            + bundle.getLong(FiltrosFragment.ARG_DESTINO_ID, -1));
                    filtrosActuales = bundleAFiltros(bundle);
                    Log.d(TAG, "filtrosActuales: " + (filtrosActuales == null ? "null" :
                            "destinoId=" + filtrosActuales.getDestinoId()));
                    reiniciarYRecargar();
                }
        );

        // Header (destacadas + titulo "Catalogo completo")
        View header = LayoutInflater.from(requireContext())
                .inflate(R.layout.header_destacadas, lvActividades, false);
        tvDestacadasTitulo = header.findViewById(R.id.tvDestacadasTitulo);
        hsvDestacadas = header.findViewById(R.id.hsvDestacadas);
        llDestacadasContainer = header.findViewById(R.id.llDestacadasContainer);

        tvNoticiasTitulo = header.findViewById(R.id.tvNoticiasTitulo);
        lvNoticias = header.findViewById(R.id.lvNoticias);
        noticiaAdapter = new NoticiaAdapter(requireContext(), noticias);
        lvNoticias.setAdapter(noticiaAdapter);

        btnVerTodasNoticias = header.findViewById(R.id.btnVerTodasNoticias);
        btnVerTodasNoticias.setOnClickListener(v -> {
            btnVerTodasNoticias.setVisibility(View.GONE);
        noticiaAdapter.setLimitado(false);
        noticiaAdapter.notifyDataSetChanged();
        lvNoticias.post(() -> ajustarAlturaListView(lvNoticias));
        });

        lvNoticias.setOnItemClickListener((parent, v, position, id) -> {
            NoticiaDTO noticia = noticias.get(position);
            Bundle args = new Bundle();
            if (noticia.getActividadRelacionadaId() != null) {
                args.putLong(DetalleFragment.ARG_ACTIVIDAD_ID,
                    noticia.getActividadRelacionadaId());
                Navigation.findNavController(v)
                    .navigate(R.id.action_home_to_detalle, args);
            } else {
                args.putLong("noticiaId", noticia.getId());
                Navigation.findNavController(v)
                    .navigate(R.id.action_home_to_noticiaDetalle, args);
            }
        });

        lvActividades.addHeaderView(header, null, false);

        // Footer (boton "Cargar mas")
        View footer = LayoutInflater.from(requireContext())
                .inflate(R.layout.footer_cargar_mas, lvActividades, false);
        btnCargarMas = footer.findViewById(R.id.btnCargarMas);
        tvFinDeLista = footer.findViewById(R.id.tvFinDeLista);
        lvActividades.addFooterView(footer, null, false);
        btnCargarMas.setOnClickListener(v -> cargarSiguientePagina());

        adapter = new ActividadAdapter(requireContext(), actividades);
        lvActividades.setAdapter(adapter);

        // Toggle de favorito desde la card del catalogo (Punto 7).
        // Optimistic update: cambiamos el icono al toque y revertimos si el
        // backend falla, para que la UI se sienta instantanea.
        adapter.setFavoritoListener((act, nuevoEstado) -> {
            long actividadId = act.getId();
            if (nuevoEstado) {
                adapter.marcarLocal(actividadId);
                favoritoRepository.marcar(actividadId).enqueue(
                        new Callback<FavoritoDTO>() {
                            @Override public void onResponse(
                                    Call<FavoritoDTO> c, Response<FavoritoDTO> r) {
                                if (!r.isSuccessful() && getView() != null) {
                                    adapter.desmarcarLocal(actividadId);
                                    Toast.makeText(requireContext(),
                                            "No se pudo marcar como favorito",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override public void onFailure(
                                    Call<FavoritoDTO> c, Throwable t) {
                                if (getView() == null) return;
                                adapter.desmarcarLocal(actividadId);
                                Toast.makeText(requireContext(),
                                        "Error de conexión",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                adapter.desmarcarLocal(actividadId);
                favoritoRepository.desmarcar(actividadId).enqueue(
                        new Callback<Void>() {
                            @Override public void onResponse(
                                    Call<Void> c, Response<Void> r) {
                                if (!r.isSuccessful() && getView() != null) {
                                    adapter.marcarLocal(actividadId);
                                    Toast.makeText(requireContext(),
                                            "No se pudo quitar de favoritos",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override public void onFailure(
                                    Call<Void> c, Throwable t) {
                                if (getView() == null) return;
                                adapter.marcarLocal(actividadId);
                                Toast.makeText(requireContext(),
                                        "Error de conexión",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        lvActividades.setOnItemClickListener((parent, v, position, id) -> {
            Object item = parent.getItemAtPosition(position);
            if (!(item instanceof ActividadDTO)) return;
            ActividadDTO seleccionada = (ActividadDTO) item;
            Bundle args = new Bundle();
            args.putLong(DetalleFragment.ARG_ACTIVIDAD_ID, seleccionada.getId());
            Navigation.findNavController(v).navigate(
                    R.id.action_home_to_detalle, args);
        });

        cargarDestacadas();
        cargarPrimeraPagina();
        cargarNoticias();
        cargarFavoritos();
    }

    /**
     * Carga el set de actividades favoritas del usuario para que el adapter
     * dibuje el corazon lleno donde corresponda. Se invoca al entrar a Home
     * y tambien en onResume() para refrescar despues de volver del detalle.
     */
    private void cargarFavoritos() {
        favoritoRepository.misFavoritos().enqueue(new Callback<List<FavoritoDTO>>() {
            @Override
            public void onResponse(Call<List<FavoritoDTO>> call,
                                   Response<List<FavoritoDTO>> response) {
                if (getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    Set<Long> ids = new HashSet<>();
                    for (FavoritoDTO f : response.body()) {
                        if (f.getActividadId() != null) ids.add(f.getActividadId());
                    }
                    adapter.setFavoritos(ids);
                }
            }
            @Override
            public void onFailure(Call<List<FavoritoDTO>> call, Throwable t) {
                if (getView() == null) return;
                Log.e(TAG, "favoritos onFailure", t);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Volver del detalle puede haber cambiado el estado de favoritos.
        if (adapter != null) cargarFavoritos();
    }

    // ---------- Destacadas ----------

    private void cargarDestacadas() {
        actividadRepository.obtenerDestacadas().enqueue(
                new Callback<List<ActividadDTO>>() {
                    @Override
                    public void onResponse(Call<List<ActividadDTO>> call,
                                           Response<List<ActividadDTO>> response) {
                        if (getView() == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            mostrarDestacadas(response.body());
                        } else {
                            ocultarDestacadas();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ActividadDTO>> call, Throwable t) {
                        if (getView() == null) return;
                        Log.e(TAG, "destacadas onFailure", t);
                        ocultarDestacadas();
                    }
                });
    }

    private void mostrarDestacadas(List<ActividadDTO> recibidas) {
        llDestacadasContainer.removeAllViews();
        if (recibidas == null || recibidas.isEmpty()) {
            ocultarDestacadas();
            return;
        }
        for (ActividadDTO a : recibidas) {
            View card = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_destacada, llDestacadasContainer, false);
            ImageView ivImg = card.findViewById(R.id.ivDestacadaImagen);
            TextView tvNom = card.findViewById(R.id.tvDestacadaNombre);
            TextView tvPre = card.findViewById(R.id.tvDestacadaPrecio);

            tvNom.setText(a.getNombre());
            tvPre.setText(PrecioFormatter.format(a.getPrecio()));
            Glide.with(this)
                    .load(a.getImagenPrincipal())
                    .placeholder(android.R.color.darker_gray)
                    .into(ivImg);

            card.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putLong(DetalleFragment.ARG_ACTIVIDAD_ID, a.getId());
                Navigation.findNavController(v).navigate(
                        R.id.action_home_to_detalle, args);
            });

            llDestacadasContainer.addView(card);
        }
        tvDestacadasTitulo.setVisibility(View.VISIBLE);
        hsvDestacadas.setVisibility(View.VISIBLE);
    }

    private void ocultarDestacadas() {
        tvDestacadasTitulo.setVisibility(View.GONE);
        hsvDestacadas.setVisibility(View.GONE);
    }

    // ---------- Listado paginado ----------

    private void reiniciarYRecargar() {
        actividades.clear();
        adapter.notifyDataSetChanged();
        paginaActual = 0;
        esUltimaPagina = false;
        btnCargarMas.setVisibility(View.VISIBLE);
        tvFinDeLista.setVisibility(View.GONE);
        cargarPrimeraPagina();
    }

    private void cargarPrimeraPagina() {
        tvStatus.setText("Cargando...");
        tvStatus.setVisibility(View.VISIBLE);
        lvActividades.setVisibility(View.GONE);
        cargarPagina(0);
    }

    private void cargarSiguientePagina() {
        if (cargando || esUltimaPagina) return;
        cargarPagina(paginaActual + 1);
    }

    private void cargarPagina(int pagina) {
        final int miToken = ++requestToken;
        cargando = true;
        btnCargarMas.setEnabled(false);
        btnCargarMas.setText("Cargando...");

        actividadRepository.listarActividades(pagina, PAGE_SIZE, filtrosActuales).enqueue(
                new Callback<PageResponseDTO<ActividadDTO>>() {
                    @Override
                    public void onResponse(
                            Call<PageResponseDTO<ActividadDTO>> call,
                            Response<PageResponseDTO<ActividadDTO>> response) {
                        if (getView() == null) return;
                        if (miToken != requestToken) {
                            // Respuesta vieja, ignorar
                            return;
                        }
                        cargando = false;
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
                        if (getView() == null) return;
                        if (miToken != requestToken) return;
                        cargando = false;
                        mostrarError("Error de red: " + t.getMessage());
                        actualizarFooter();
                        Log.e(TAG, "onFailure", t);
                    }
                });
    }

    private void agregarActividades(List<ActividadDTO> recibidas) {
        if (paginaActual == 0) actividades.clear();
        if (recibidas != null) actividades.addAll(recibidas);

        if (actividades.isEmpty()) {
            mostrarError("No hay actividades que coincidan con los filtros");
            return;
        }

        adapter.notifyDataSetChanged();
        tvStatus.setVisibility(View.GONE);
        lvActividades.setVisibility(View.VISIBLE);
    }

    private void actualizarFooter() {
        if (esUltimaPagina) {
            btnCargarMas.setVisibility(View.GONE);
            tvFinDeLista.setVisibility(View.VISIBLE);
        } else {
            btnCargarMas.setVisibility(View.VISIBLE);
            btnCargarMas.setEnabled(true);
            btnCargarMas.setText("Cargar mas");
            tvFinDeLista.setVisibility(View.GONE);
        }
    }

    private void mostrarError(String mensaje) {
        tvStatus.setText(mensaje);
        tvStatus.setVisibility(View.VISIBLE);
        lvActividades.setVisibility(View.GONE);
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

    private void cargarNoticias() {
        noticiaRepository.listarNoticias().enqueue(new Callback<List<NoticiaDTO>>() {
            @Override
            public void onResponse(Call<List<NoticiaDTO>> call,
                                    Response<List<NoticiaDTO>> response) {
                if (getView() == null) return;
                if (response.isSuccessful() && response.body() != null
                        && !response.body().isEmpty()) {
                    mostrarNoticias(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<NoticiaDTO>> call, Throwable t) {
                if (getView() == null) return;
                Log.e(TAG, "noticias onFailure", t);
            }
        });
    }


    private void mostrarNoticias(List<NoticiaDTO> recibidas) {
        noticias.clear();
        noticias.addAll(recibidas);
        noticiaAdapter.notifyDataSetChanged();
        tvNoticiasTitulo.setVisibility(View.VISIBLE);
        lvNoticias.setVisibility(View.VISIBLE);
        if (recibidas.size() > 2) {
            btnVerTodasNoticias.setVisibility(View.VISIBLE);
        }
        lvNoticias.post(() -> ajustarAlturaListView(lvNoticias));
    }

    private void ajustarAlturaListView(ListView lv) {
        if (lv.getAdapter() == null) return;
        int totalHeight = 0;
        for (int i = 0; i < lv.getAdapter().getCount(); i++) {
            View item = lv.getAdapter().getView(i, null, lv);
            item.measure(
                    View.MeasureSpec.makeMeasureSpec(lv.getWidth(), View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.UNSPECIFIED);
            totalHeight += item.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = lv.getLayoutParams();
        params.height = totalHeight + (lv.getDividerHeight() * (lv.getAdapter().getCount() - 1));
        lv.setLayoutParams(params);
        lv.requestLayout();
    }

}
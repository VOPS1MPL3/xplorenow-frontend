package com.xplorenow.ui.home.favoritos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.xplorenow.R;
import com.xplorenow.data.dto.FavoritoDTO;
import com.xplorenow.data.repository.FavoritoRepository;
import com.xplorenow.ui.home.detalle.DetalleFragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla "Mis favoritos" (Punto 7 del TPO).
 *
 * Funcionalidad:
 *  - Lista las actividades marcadas como favoritas (GET /favoritos).
 *  - Tap en una card -> abre el detalle de la actividad (acceso rapido a reservar).
 *  - Tap en el corazon de la card -> quita de favoritos.
 *  - Si el backend reporta tieneNovedad=true para una actividad, muestra
 *    un badge "Bajo de precio" / "Hay mas cupos".
 *  - Recarga al volver a la pantalla (onResume) para reflejar cambios
 *    hechos desde el catalogo o el detalle.
 */
@AndroidEntryPoint
public class FavoritosFragment extends Fragment {

    private static final String TAG = "FavoritosFragment";

    @Inject
    FavoritoRepository favoritoRepository;

    private TextView tvStatus;
    private ListView lvFavoritos;

    private FavoritoAdapter adapter;
    private final List<FavoritoDTO> favoritos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favoritos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        // Esta pantalla cuelga del bottom nav, no necesita boton atras.
        // Si en algun caso entra desde otra pantalla, mostrara el boton al
        // setearlo aca.
        // toolbar.setNavigationOnClickListener(v -> Navigation...popBackStack());

        tvStatus = view.findViewById(R.id.tvStatus);
        lvFavoritos = view.findViewById(R.id.lvFavoritos);

        adapter = new FavoritoAdapter(requireContext(), favoritos);
        lvFavoritos.setAdapter(adapter);

        // Tap en una card -> ir al detalle de la actividad.
        lvFavoritos.setOnItemClickListener((parent, v, position, id) -> {
            FavoritoDTO f = favoritos.get(position);
            if (f.getActividadId() == null) return;
            Bundle args = new Bundle();
            args.putLong(DetalleFragment.ARG_ACTIVIDAD_ID, f.getActividadId());
            Navigation.findNavController(v)
                    .navigate(R.id.action_favoritos_to_detalle, args);
        });

        // Quitar de favoritos desde el corazon de la card.
        adapter.setOnQuitarListener(this::quitarFavorito);
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarFavoritos();
    }

    private void cargarFavoritos() {
        tvStatus.setText("Cargando...");
        tvStatus.setVisibility(View.VISIBLE);
        lvFavoritos.setVisibility(View.GONE);

        favoritoRepository.misFavoritos().enqueue(new Callback<List<FavoritoDTO>>() {
            @Override
            public void onResponse(Call<List<FavoritoDTO>> call,
                                   Response<List<FavoritoDTO>> response) {
                if (getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    favoritos.clear();
                    favoritos.addAll(response.body());
                    if (favoritos.isEmpty()) {
                        tvStatus.setText("Todavía no tenés favoritos.\n\n\n"
                                + "Selecciona aquellos que mas te interesen para recibir novedades! ;) ");
                        tvStatus.setVisibility(View.VISIBLE);
                        lvFavoritos.setVisibility(View.GONE);
                    } else {
                        tvStatus.setVisibility(View.GONE);
                        lvFavoritos.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    tvStatus.setText("No se pudieron cargar tus favoritos "
                            + "(HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<FavoritoDTO>> call, Throwable t) {
                if (getView() == null) return;
                Log.e(TAG, "favoritos onFailure", t);
                tvStatus.setText("Error de conexión");
            }
        });
    }

    /**
     * Quita el favorito del backend. Optimistic update: lo sacamos de la lista
     * al toque y, si la API falla, lo volvemos a poner.
     */
    private void quitarFavorito(FavoritoDTO f) {
        if (f.getActividadId() == null) return;
        long actId = f.getActividadId();

        // Guardar copia por si tenemos que revertir
        int posicion = favoritos.indexOf(f);
        favoritos.remove(f);
        adapter.notifyDataSetChanged();
        actualizarStatusSiVacio();

        favoritoRepository.desmarcar(actId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> c, Response<Void> r) {
                if (getView() == null) return;
                if (!r.isSuccessful()) {
                    revertir(f, posicion);
                    Toast.makeText(requireContext(),
                            "No se pudo quitar de favoritos",
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> c, Throwable t) {
                if (getView() == null) return;
                revertir(f, posicion);
                Toast.makeText(requireContext(),
                        "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void revertir(FavoritoDTO f, int posicion) {
        if (posicion >= 0 && posicion <= favoritos.size()) {
            favoritos.add(posicion, f);
        } else {
            favoritos.add(f);
        }
        adapter.notifyDataSetChanged();
        actualizarStatusSiVacio();
    }

    private void actualizarStatusSiVacio() {
        if (favoritos.isEmpty()) {
            tvStatus.setText("Todavía no tenés favoritos.\n"
                    + "Marcá actividades con el corazón en el catálogo.");
            tvStatus.setVisibility(View.VISIBLE);
            lvFavoritos.setVisibility(View.GONE);
        } else {
            tvStatus.setVisibility(View.GONE);
            lvFavoritos.setVisibility(View.VISIBLE);
        }
    }
}

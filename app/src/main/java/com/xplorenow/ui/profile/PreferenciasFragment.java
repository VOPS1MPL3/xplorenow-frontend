package com.xplorenow.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.xplorenow.R;
import com.xplorenow.data.dto.CategoriaDTO;
import com.xplorenow.data.dto.PerfilDTO;
import com.xplorenow.data.repository.CatalogoRepository;
import com.xplorenow.data.repository.PerfilRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla para editar las preferencias de viaje (Punto 2.2 del TPO).
 *
 * Muestra un ListView con todas las categorias disponibles y permite marcar
 * varias. Al guardar, llama a PUT /perfil/preferencias y vuelve atras
 * notificando a PerfilFragment via setFragmentResult.
 *
 * Regla del backend (doc 6.2): la lista enviada SOBREESCRIBE la lista actual,
 * no se hace merge. Por eso enviamos siempre la lista completa de seleccionadas.
 */
@AndroidEntryPoint
public class PreferenciasFragment extends Fragment {

    private static final String TAG = "PreferenciasFragment";

    /** Clave usada por PerfilFragment para detectar que se guardaron cambios. */
    public static final String RESULT_KEY = "preferencias_result";

    @Inject
    CatalogoRepository catalogoRepository;

    @Inject
    PerfilRepository perfilRepository;

    private MaterialToolbar toolbar;
    private TextView tvStatus;
    private ListView lvCategorias;
    private Button btnGuardar;

    private final List<CategoriaDTO> categorias = new ArrayList<>();
    /** IDs ya marcadas en el perfil del usuario. Las cargamos del backend al entrar. */
    private final Set<Long> seleccionadasIniciales = new HashSet<>();
    private boolean categoriasListas = false;
    private boolean perfilListo = false;

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

        toolbar = view.findViewById(R.id.toolbarPreferencias);
        tvStatus = view.findViewById(R.id.tvStatus);
        lvCategorias = view.findViewById(R.id.lvCategorias);
        btnGuardar = view.findViewById(R.id.btnGuardarPref);

        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        btnGuardar.setEnabled(false);
        btnGuardar.setOnClickListener(v -> guardar());

        // Cargamos categorias y perfil en paralelo. Cuando ambos terminan,
        // pintamos la lista con las preselecciones del usuario.
        cargarCategorias();
        cargarPreferenciasActuales();
    }

    // ---------- Carga ----------

    private void cargarCategorias() {
        catalogoRepository.listarCategorias().enqueue(new Callback<List<CategoriaDTO>>() {
            @Override
            public void onResponse(Call<List<CategoriaDTO>> call,
                                   Response<List<CategoriaDTO>> response) {
                if (getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    categorias.clear();
                    categorias.addAll(response.body());
                    categoriasListas = true;
                    intentarPintar();
                } else {
                    mostrarError("No se pudieron cargar las categorias (HTTP "
                            + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<CategoriaDTO>> call, Throwable t) {
                if (getView() == null) return;
                Log.e(TAG, "categorias onFailure", t);
                mostrarError("Error de conexion al cargar categorias");
            }
        });
    }

    private void cargarPreferenciasActuales() {
        perfilRepository.obtenerPerfil().enqueue(new Callback<PerfilDTO>() {
            @Override
            public void onResponse(Call<PerfilDTO> call, Response<PerfilDTO> response) {
                if (getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    seleccionadasIniciales.clear();
                    List<CategoriaDTO> prefs = response.body().getPreferencias();
                    if (prefs != null) {
                        for (CategoriaDTO c : prefs) {
                            if (c.getId() != null) {
                                seleccionadasIniciales.add(c.getId());
                            }
                        }
                    }
                    perfilListo = true;
                    intentarPintar();
                } else {
                    // Si no se puede leer el perfil, igual dejamos elegir desde cero.
                    perfilListo = true;
                    intentarPintar();
                }
            }

            @Override
            public void onFailure(Call<PerfilDTO> call, Throwable t) {
                if (getView() == null) return;
                Log.e(TAG, "perfil onFailure", t);
                perfilListo = true;
                intentarPintar();
            }
        });
    }

    private void intentarPintar() {
        if (!categoriasListas || !perfilListo) return;

        if (categorias.isEmpty()) {
            mostrarError("No hay categorias disponibles");
            return;
        }

        List<String> labels = new ArrayList<>();
        for (CategoriaDTO c : categorias) labels.add(c.getNombre());

        // simple_list_item_multiple_choice: cada fila tiene un CheckBox a la derecha,
        // y ListView gestiona el estado con choiceMode=multipleChoice (ya seteado en XML).
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_multiple_choice,
                labels);
        lvCategorias.setAdapter(adapter);

        // Pre-marcamos las categorias que el usuario ya tiene seleccionadas.
        for (int i = 0; i < categorias.size(); i++) {
            Long id = categorias.get(i).getId();
            if (id != null && seleccionadasIniciales.contains(id)) {
                lvCategorias.setItemChecked(i, true);
            }
        }

        tvStatus.setVisibility(View.GONE);
        lvCategorias.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(true);
    }

    // ---------- Guardar ----------

    private void guardar() {
        // Recopilamos los IDs de las categorias marcadas.
        List<Long> idsSeleccionados = new ArrayList<>();
        for (int i = 0; i < categorias.size(); i++) {
            if (lvCategorias.isItemChecked(i)) {
                Long id = categorias.get(i).getId();
                if (id != null) idsSeleccionados.add(id);
            }
        }

        btnGuardar.setEnabled(false);

        perfilRepository.actualizarPreferencias(idsSeleccionados)
                .enqueue(new Callback<PerfilDTO>() {
                    @Override
                    public void onResponse(Call<PerfilDTO> call,
                                           Response<PerfilDTO> response) {
                        if (getView() == null) return;
                        btnGuardar.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "Preferencias guardadas",
                                    Toast.LENGTH_SHORT).show();
                            // Le avisamos a PerfilFragment que recargue.
                            getParentFragmentManager()
                                    .setFragmentResult(RESULT_KEY, new Bundle());
                            Navigation.findNavController(requireView()).popBackStack();
                        } else {
                            Toast.makeText(requireContext(),
                                    "No se pudo guardar (HTTP "
                                            + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PerfilDTO> call, Throwable t) {
                        if (getView() == null) return;
                        btnGuardar.setEnabled(true);
                        Log.e(TAG, "actualizarPreferencias onFailure", t);
                        Toast.makeText(requireContext(),
                                "Error de conexion: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarError(String msg) {
        tvStatus.setText(msg);
        tvStatus.setVisibility(View.VISIBLE);
        lvCategorias.setVisibility(View.GONE);
        btnGuardar.setEnabled(false);
    }
}

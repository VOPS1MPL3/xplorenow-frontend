package com.xplorenow.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.MaterialToolbar;
import com.xplorenow.R;
import com.xplorenow.data.dto.CategoriaDTO;
import com.xplorenow.data.dto.EstadoReserva;
import com.xplorenow.data.dto.PerfilDTO;
import com.xplorenow.data.dto.ReservaDTO;
import com.xplorenow.data.repository.PerfilRepository;
import com.xplorenow.util.ImageStorageUtil;
import com.xplorenow.util.TokenManager;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla de Perfil del Viajero (Punto 2 del TPO).
 *
 * Permite:
 *  - Ver datos personales (nombre, email, telefono, foto).
 *  - Editar nombre, telefono y foto (el email NO se puede modificar, regla del backend).
 *  - Editar las preferencias de viaje (en pantalla aparte: PreferenciasFragment).
 *  - Ver un resumen de actividades reservadas y finalizadas (punto 2.3).
 *  - Cerrar sesion.
 *
 * La pantalla tiene dos modos: VISTA (tv*) y EDICION (et*). Solo se muestran
 * los inputs cuando el usuario toca "Editar datos personales".
 */
@AndroidEntryPoint
public class PerfilFragment extends Fragment {

    private static final String TAG = "PerfilFragment";

    @Inject
    PerfilRepository perfilRepository;

    @Inject
    TokenManager tokenManager;

    private MaterialToolbar toolbar;
    private ImageView ivFoto;
    private android.widget.ImageButton btnCambiarFoto;
    private TextView tvEmail, tvNombre, tvTelefono, tvPreferencias, tvResumenReservas;
    private EditText etNombre, etTelefono;
    private Button btnEditar, btnCancelar, btnGuardar, btnEditarPreferencias, btnVerHistorial, btnCerrarSesion;
    private LinearLayout llBotonesEdicion;

    private PerfilDTO perfilActual;
    private boolean modoEdicion = false;

    // ---------- Launchers (patron del apunte, seccion 10) ----------

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagenSeleccionada);

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    abrirGaleria();
                } else {
                    Toast.makeText(requireContext(),
                            "Necesitamos permiso para acceder a la galeria",
                            Toast.LENGTH_SHORT).show();
                }
            });

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

        toolbar = view.findViewById(R.id.toolbarPerfil);
        ivFoto = view.findViewById(R.id.ivFotoPerfil);
        btnCambiarFoto = view.findViewById(R.id.btnCambiarFoto);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvNombre = view.findViewById(R.id.tvNombre);
        tvTelefono = view.findViewById(R.id.tvTelefono);
        tvPreferencias = view.findViewById(R.id.tvPreferencias);
        tvResumenReservas = view.findViewById(R.id.tvResumenReservas);
        etNombre = view.findViewById(R.id.etNombre);
        etTelefono = view.findViewById(R.id.etTelefono);
        btnEditar = view.findViewById(R.id.btnEditar);
        btnCancelar = view.findViewById(R.id.btnCancelar);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        btnEditarPreferencias = view.findViewById(R.id.btnEditarPreferencias);
        btnVerHistorial = view.findViewById(R.id.btnVerHistorial);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        llBotonesEdicion = view.findViewById(R.id.llBotonesEdicion);

        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        btnEditar.setOnClickListener(v -> entrarEdicion());
        btnCancelar.setOnClickListener(v -> salirEdicion(false));
        btnGuardar.setOnClickListener(v -> guardarCambios());
        btnCambiarFoto.setOnClickListener(v -> seleccionarFoto());
        btnEditarPreferencias.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_perfil_to_preferencias));
        btnVerHistorial.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_perfil_to_historial));
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        // Cuando preferencias guarda y vuelve, recargamos el perfil para
        // reflejar la nueva lista en pantalla.
        getParentFragmentManager().setFragmentResultListener(
                PreferenciasFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> cargarPerfil()
        );

        cargarPerfil();
        cargarResumenReservas();
    }

    // ---------- Carga de datos ----------

    private void cargarPerfil() {
        perfilRepository.obtenerPerfil().enqueue(new Callback<PerfilDTO>() {
            @Override
            public void onResponse(Call<PerfilDTO> call, Response<PerfilDTO> response) {
                if (getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    perfilActual = response.body();
                    pintarPerfil(perfilActual);
                } else if (response.code() == 401) {
                    // Token vencido o invalido: aca SI conviene borrarlo,
                    // porque ya no sirve para nada y dejarlo guardado haria
                    // que el boton de huella en el login simule un acceso
                    // valido pero todas las llamadas siguientes fallarian.
                    tokenManager.clearToken();
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_perfil_to_login);
                } else {
                    Toast.makeText(requireContext(),
                            "No se pudo cargar el perfil (HTTP " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PerfilDTO> call, Throwable t) {
                if (getView() == null) return;
                Log.e(TAG, "perfil onFailure", t);
                Toast.makeText(requireContext(),
                        "Error de conexion: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarResumenReservas() {
        // Punto 2.3: resumen de reservadas (CONFIRMADA) y realizadas (FINALIZADA).
        // Pedimos la lista completa y contamos por estado del lado de la app.
        perfilRepository.misReservas(null).enqueue(new Callback<List<ReservaDTO>>() {
            @Override
            public void onResponse(Call<List<ReservaDTO>> call,
                                   Response<List<ReservaDTO>> response) {
                if (getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    pintarResumen(response.body());
                } else {
                    tvResumenReservas.setText("No se pudo cargar el resumen.");
                }
            }

            @Override
            public void onFailure(Call<List<ReservaDTO>> call, Throwable t) {
                if (getView() == null) return;
                Log.e(TAG, "resumen onFailure", t);
                tvResumenReservas.setText("Error de conexion al cargar el resumen.");
            }
        });
    }

    // ---------- Render ----------

    private void pintarPerfil(@NonNull PerfilDTO p) {
        tvEmail.setText(p.getEmail() != null ? p.getEmail() : "-");
        tvNombre.setText(!TextUtils.isEmpty(p.getNombre()) ? p.getNombre() : "-");
        tvTelefono.setText(!TextUtils.isEmpty(p.getTelefono()) ? p.getTelefono() : "-");

        // Foto de perfil con Glide (apunte, seccion 9.5).
        Object glideSource = ImageStorageUtil.resolverParaGlide(p.getFotoUrl());
        if (glideSource != null) {
            Glide.with(this)
                    .load(glideSource)
                    .placeholder(R.drawable.bg_foto_perfil)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .circleCrop()
                    .into(ivFoto);
        } else {
            // Sin foto: dejamos visible el background circular (bg_foto_perfil).
            // Limpiamos el src para que no quede una foto vieja arriba.
            ivFoto.setImageDrawable(null);
        }

        // Preferencias (lista de categorias).
        List<CategoriaDTO> prefs = p.getPreferencias();
        if (prefs == null || prefs.isEmpty()) {
            tvPreferencias.setText("Sin preferencias seleccionadas");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < prefs.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(prefs.get(i).getNombre());
            }
            tvPreferencias.setText(sb.toString());
        }
    }

    private void pintarResumen(@NonNull List<ReservaDTO> reservas) {
        int confirmadas = 0;
        int finalizadas = 0;
        int canceladas = 0;
        for (ReservaDTO r : reservas) {
            if (r.getEstado() == EstadoReserva.CONFIRMADA) confirmadas++;
            else if (r.getEstado() == EstadoReserva.FINALIZADA) finalizadas++;
            else if (r.getEstado() == EstadoReserva.CANCELADA) canceladas++;
        }
        String resumen = "Reservadas (proximas): " + confirmadas + "\n" +
                "Realizadas: " + finalizadas + "\n" +
                "Canceladas: " + canceladas;
        tvResumenReservas.setText(resumen);
    }

    // ---------- Modo edicion ----------

    private void entrarEdicion() {
        if (perfilActual == null) return;
        modoEdicion = true;

        etNombre.setText(perfilActual.getNombre() != null ? perfilActual.getNombre() : "");
        etTelefono.setText(perfilActual.getTelefono() != null ? perfilActual.getTelefono() : "");

        tvNombre.setVisibility(View.GONE);
        tvTelefono.setVisibility(View.GONE);
        etNombre.setVisibility(View.VISIBLE);
        etTelefono.setVisibility(View.VISIBLE);

        btnEditar.setVisibility(View.GONE);
        llBotonesEdicion.setVisibility(View.VISIBLE);
        btnEditarPreferencias.setEnabled(false);
        // El lapiz de foto sigue visible (es independiente del modo edicion).
    }

    private void salirEdicion(boolean exitoso) {
        modoEdicion = false;

        tvNombre.setVisibility(View.VISIBLE);
        tvTelefono.setVisibility(View.VISIBLE);
        etNombre.setVisibility(View.GONE);
        etTelefono.setVisibility(View.GONE);

        btnEditar.setVisibility(View.VISIBLE);
        llBotonesEdicion.setVisibility(View.GONE);
        btnEditarPreferencias.setEnabled(true);

        if (!exitoso && perfilActual != null) {
            // Si se cancelo la edicion, repintamos para revertir cualquier
            // cambio en los inputs. La foto NO se revierte aca porque ahora
            // se guarda inmediatamente al elegirla, no se asocia al modo edicion.
            pintarPerfil(perfilActual);
        }
    }

    private void guardarCambios() {
        if (perfilActual == null) return;

        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(requireContext(),
                    "El nombre no puede estar vacio",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // La foto se gestiona aparte (con el lapiz). Mandamos la URL actual
        // del perfil para no pisarla.
        String fotoUrl = perfilActual.getFotoUrl();

        btnGuardar.setEnabled(false);

        perfilRepository.actualizarPerfil(nombre, telefono, fotoUrl)
                .enqueue(new Callback<PerfilDTO>() {
                    @Override
                    public void onResponse(Call<PerfilDTO> call, Response<PerfilDTO> response) {
                        if (getView() == null) return;
                        btnGuardar.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            perfilActual = response.body();
                            pintarPerfil(perfilActual);
                            salirEdicion(true);
                            Toast.makeText(requireContext(),
                                    "Perfil actualizado",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(),
                                    "No se pudo guardar (HTTP " + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PerfilDTO> call, Throwable t) {
                        if (getView() == null) return;
                        btnGuardar.setEnabled(true);
                        Log.e(TAG, "actualizarPerfil onFailure", t);
                        Toast.makeText(requireContext(),
                                "Error de conexion: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ---------- Foto de perfil ----------

    private void seleccionarFoto() {
        // En Android 13+ (API 33) se usa READ_MEDIA_IMAGES. En < 13, READ_EXTERNAL_STORAGE.
        // ACTION_PICK / GetContent de la galeria normalmente no requiere permiso explicito
        // a partir de API 19, pero pedimos el permiso para casos de fallback en algunos
        // dispositivos OEM (apunte, seccion 9.2).
        String permiso = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), permiso)
                == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria();
        } else {
            requestPermissionLauncher.launch(permiso);
        }
    }

    private void abrirGaleria() {
        // GetContent recibe el MIME type; "image/*" muestra todas las imagenes.
        pickImageLauncher.launch("image/*");
    }

    private void onImagenSeleccionada(@Nullable Uri uri) {
        if (uri == null) return; // El usuario cancelo
        if (perfilActual == null) {
            Toast.makeText(requireContext(),
                    "Esperando datos del perfil...",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String fotoUrl = ImageStorageUtil.guardarFotoPerfil(requireContext(), uri);
        if (fotoUrl == null) {
            Toast.makeText(requireContext(),
                    "No se pudo procesar la imagen",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Preview inmediato.
        Glide.with(this)
                .load(ImageStorageUtil.resolverParaGlide(fotoUrl))
                .placeholder(R.drawable.bg_foto_perfil)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .circleCrop()
                .into(ivFoto);

        // Persistir al backend de una. PUT /perfil necesita nombre y telefono;
        // mandamos los valores actuales para no pisar nada. Si el usuario esta
        // editando datos personales en este momento, leemos del input para
        // capturar lo que tipeo (todavia sin guardar) y no perder ese trabajo.
        String nombre = modoEdicion
                ? etNombre.getText().toString().trim()
                : (perfilActual.getNombre() != null ? perfilActual.getNombre() : "");
        String telefono = modoEdicion
                ? etTelefono.getText().toString().trim()
                : (perfilActual.getTelefono() != null ? perfilActual.getTelefono() : "");

        if (TextUtils.isEmpty(nombre)) {
            // El backend exige nombre no vacio. Si no lo tenemos, abortamos
            // y avisamos al usuario.
            Toast.makeText(requireContext(),
                    "Completa tu nombre antes de cambiar la foto",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnCambiarFoto.setEnabled(false);
        perfilRepository.actualizarPerfil(nombre, telefono, fotoUrl)
                .enqueue(new Callback<PerfilDTO>() {
                    @Override
                    public void onResponse(Call<PerfilDTO> call, Response<PerfilDTO> response) {
                        if (getView() == null) return;
                        btnCambiarFoto.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            perfilActual = response.body();
                            // Solo repintamos la foto/preferencias, no los inputs,
                            // para no pisar lo que el usuario esta tipeando si esta
                            // en modo edicion.
                            pintarFotoYPreferencias(perfilActual);
                            Toast.makeText(requireContext(),
                                    "Foto actualizada",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(),
                                    "No se pudo guardar la foto (HTTP "
                                            + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PerfilDTO> call, Throwable t) {
                        if (getView() == null) return;
                        btnCambiarFoto.setEnabled(true);
                        Log.e(TAG, "actualizar foto onFailure", t);
                        Toast.makeText(requireContext(),
                                "Error de conexion al guardar la foto",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Repinta solo la foto y las preferencias. Lo usamos despues de guardar
     * la foto cuando el usuario podria estar tipeando en los inputs y no
     * queremos pisar lo que escribio.
     */
    private void pintarFotoYPreferencias(@NonNull PerfilDTO p) {
        Object glideSource = ImageStorageUtil.resolverParaGlide(p.getFotoUrl());
        if (glideSource != null) {
            Glide.with(this)
                    .load(glideSource)
                    .placeholder(R.drawable.bg_foto_perfil)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .circleCrop()
                    .into(ivFoto);
        }
        List<CategoriaDTO> prefs = p.getPreferencias();
        if (prefs == null || prefs.isEmpty()) {
            tvPreferencias.setText("Sin preferencias seleccionadas");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < prefs.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(prefs.get(i).getNombre());
            }
            tvPreferencias.setText(sb.toString());
        }
    }

    // ---------- Cerrar sesion ----------

    private void cerrarSesion() {
        // Decision: NO borramos el token con tokenManager.clearToken().
        // El LoginFragment muestra el boton "Ingresar con huella" solo si
        // tokenManager.hasToken() devuelve true. Si lo borraramos, al volver
        // al login el boton desaparece y el usuario pierde el acceso rapido
        // por biometria que tenia antes.
        //
        // Igualmente la sesion queda "cerrada" desde el punto de vista del
        // usuario porque el nav controller hace pop hasta loginFragment con
        // popUpToInclusive=true (ver action_perfil_to_login en nav_graph.xml),
        // asi que la pantalla de Perfil sale del back stack y no se puede
        // volver con el boton de "atras" del sistema.
        Navigation.findNavController(requireView())
                .navigate(R.id.action_perfil_to_login);
    }
}

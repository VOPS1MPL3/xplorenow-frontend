package com.xplorenow.ui.home.detalle;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.navigation.Navigation;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.xplorenow.R;
import com.xplorenow.data.dto.ActividadDetalleDTO;
import com.xplorenow.data.repository.ActividadRepository;
import com.xplorenow.data.util.PrecioFormatter;
import com.xplorenow.util.MapUtil;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class DetalleFragment extends Fragment {

    private static final String TAG = "DetalleFragment";
    public static final String ARG_ACTIVIDAD_ID = "actividadId";

    private ImageView ivImagenPrincipal;
    private TextView tvNombre, tvDestinoCategoria, tvPrecio, tvCupos;
    private TextView tvDescripcion, tvQueIncluye, tvPuntoEncuentro;
    private TextView tvGuia, tvIdioma, tvPoliticaCancelacion;
    private Button btnVerMapa;
    private HorizontalScrollView hsvGaleria;
    private LinearLayout llGaleriaContainer;

    private MaterialToolbar toolbar;
    private Button btnReservar;
    private ActividadDetalleDTO actividadActual;

    @Inject
    ActividadRepository actividadRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivImagenPrincipal = view.findViewById(R.id.ivImagenPrincipal);
        tvNombre = view.findViewById(R.id.tvNombre);
        tvDestinoCategoria = view.findViewById(R.id.tvDestinoCategoria);
        tvPrecio = view.findViewById(R.id.tvPrecio);
        tvCupos = view.findViewById(R.id.tvCupos);
        tvDescripcion = view.findViewById(R.id.tvDescripcion);
        tvQueIncluye = view.findViewById(R.id.tvQueIncluye);
        tvPuntoEncuentro = view.findViewById(R.id.tvPuntoEncuentro);
        tvGuia = view.findViewById(R.id.tvGuia);
        tvIdioma = view.findViewById(R.id.tvIdioma);
        tvPoliticaCancelacion = view.findViewById(R.id.tvPoliticaCancelacion);
        btnVerMapa = view.findViewById(R.id.btnVerMapa);
        hsvGaleria = view.findViewById(R.id.hsvGaleria);
        llGaleriaContainer = view.findViewById(R.id.llGaleriaContainer);
        btnReservar = view.findViewById(R.id.btnReservar);

        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        btnVerMapa.setOnClickListener(v -> {
            if (actividadActual != null) {
                MapUtil.abrirMapaNavegacion(requireContext(), 
                        actividadActual.getLatitud(), 
                        actividadActual.getLongitud(), 
                        actividadActual.getNombre());
            } else {
                Toast.makeText(requireContext(), "Cargando información del mapa...", Toast.LENGTH_SHORT).show();
            }
        });

        long actividadId = requireArguments().getLong(ARG_ACTIVIDAD_ID, -1L);
        if (actividadId < 0) {
            Toast.makeText(requireContext(),
                    "Actividad invalida", Toast.LENGTH_SHORT).show();
            return;
        }
        cargarDetalle(actividadId);
        btnReservar.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong("actividadId", actividadId);
            Navigation.findNavController(v)
                .navigate(R.id.action_detalle_to_horarios, args);
        });
    }

    private void cargarDetalle(long actividadId) {
        actividadRepository.obtenerActividad(actividadId).enqueue(
                new Callback<ActividadDetalleDTO>() {
                    @Override
                    public void onResponse(Call<ActividadDetalleDTO> call,
                                           Response<ActividadDetalleDTO> response) {
                        if (getView() == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            mostrarDetalle(response.body());
                        } else {
                            Log.e(TAG, "HTTP " + response.code());
                            Toast.makeText(requireContext(),
                                    "No se pudo cargar el detalle",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ActividadDetalleDTO> call, Throwable t) {
                        if (getView() == null) return;
                        Log.e(TAG, "onFailure", t);
                        Toast.makeText(requireContext(),
                                "Error de red: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void mostrarDetalle(ActividadDetalleDTO d) {
        this.actividadActual = d;
        Glide.with(this)
                .load(d.getImagenPrincipal())
                .placeholder(android.R.color.darker_gray)
                .into(ivImagenPrincipal);

        tvNombre.setText(d.getNombre());
        tvDestinoCategoria.setText(
                d.getDestino() + " - " + d.getCategoria()
                        + " - " + formatDuracion(d.getDuracionMinutos()));
        tvPrecio.setText(PrecioFormatter.format(d.getPrecio()));
        tvCupos.setText(d.getCuposDisponibles() + " cupos disponibles");
        tvDescripcion.setText(d.getDescripcion());
        tvQueIncluye.setText(d.getQueIncluye());
        tvPuntoEncuentro.setText(d.getPuntoEncuentro());
        tvGuia.setText(d.getGuiaAsignado());
        tvIdioma.setText(d.getIdioma());
        tvPoliticaCancelacion.setText(d.getPoliticaCancelacion());

        cargarGaleria(d.getGaleriaUrls());
    }

    private void cargarGaleria(List<String> urls) {
        llGaleriaContainer.removeAllViews();

        if (urls == null || urls.isEmpty()) {
            hsvGaleria.setVisibility(View.GONE);
            return;
        }

        for (String url : urls) {
            View foto = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_foto, llGaleriaContainer, false);
            ImageView iv = foto.findViewById(R.id.ivFoto);
            Glide.with(this)
                    .load(url)
                    .placeholder(android.R.color.darker_gray)
                    .into(iv);
            llGaleriaContainer.addView(foto);
        }
    }

    private String formatDuracion(Integer minutos) {
        if (minutos == null) return "";
        if (minutos < 60) return minutos + " min";
        int horas = minutos / 60;
        int mins = minutos % 60;
        if (mins == 0) return horas + " h";
        return horas + "h " + mins + "m";
    }
}
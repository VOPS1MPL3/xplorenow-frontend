package com.xplorenow.ui.home.detalle;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.xplorenow.R;
import com.xplorenow.data.dto.ActividadDetalleDTO;
import com.xplorenow.data.repository.ActividadRepository;
import com.xplorenow.data.util.PrecioFormatter;
import com.xplorenow.databinding.FragmentDetalleBinding;
import com.xplorenow.databinding.ItemFotoBinding;
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

    private FragmentDetalleBinding binding;

    @Inject
    ActividadRepository actividadRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetalleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Boton placeholder de "Ver mapa" (punto 10 del TPO, lo hace otro)
        binding.btnVerMapa.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "El mapa se va a integrar en el punto 10",
                        Toast.LENGTH_SHORT).show());

        long actividadId = requireArguments().getLong(ARG_ACTIVIDAD_ID, -1L);
        if (actividadId < 0) {
            Toast.makeText(requireContext(),
                    "Actividad invalida", Toast.LENGTH_SHORT).show();
            return;
        }

        cargarDetalle(actividadId);
    }

    private void cargarDetalle(long actividadId) {
        actividadRepository.obtenerActividad(actividadId).enqueue(
                new Callback<ActividadDetalleDTO>() {
                    @Override
                    public void onResponse(Call<ActividadDetalleDTO> call,
                                           Response<ActividadDetalleDTO> response) {
                        if (binding == null) return;
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
                        if (binding == null) return;
                        Log.e(TAG, "onFailure", t);
                        Toast.makeText(requireContext(),
                                "Error de red: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void mostrarDetalle(ActividadDetalleDTO d) {
        Glide.with(this)
                .load(d.getImagenPrincipal())
                .placeholder(android.R.color.darker_gray)
                .into(binding.ivImagenPrincipal);

        binding.tvNombre.setText(d.getNombre());
        binding.tvDestinoCategoria.setText(
                d.getDestino() + " - " + d.getCategoria()
                        + " - " + formatDuracion(d.getDuracionMinutos()));
        binding.tvPrecio.setText(PrecioFormatter.format(d.getPrecio()));
        binding.tvCupos.setText(d.getCuposDisponibles() + " cupos disponibles");
        binding.tvDescripcion.setText(d.getDescripcion());
        binding.tvQueIncluye.setText(d.getQueIncluye());
        binding.tvPuntoEncuentro.setText(d.getPuntoEncuentro());
        binding.tvGuia.setText(d.getGuiaAsignado());
        binding.tvIdioma.setText(d.getIdioma());
        binding.tvPoliticaCancelacion.setText(d.getPoliticaCancelacion());

        cargarGaleria(d.getGaleriaUrls());
    }

    private void cargarGaleria(List<String> urls) {
        binding.llGaleriaContainer.removeAllViews();

        if (urls == null || urls.isEmpty()) {
            // Sin galeria, ocultamos la HorizontalScrollView
            binding.hsvGaleria.setVisibility(View.GONE);
            return;
        }

        for (String url : urls) {
            ItemFotoBinding fotoBinding = ItemFotoBinding.inflate(
                    LayoutInflater.from(requireContext()),
                    binding.llGaleriaContainer,
                    false
            );
            ImageView iv = fotoBinding.ivFoto;
            Glide.with(this)
                    .load(url)
                    .placeholder(android.R.color.darker_gray)
                    .into(iv);
            binding.llGaleriaContainer.addView(fotoBinding.getRoot());
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
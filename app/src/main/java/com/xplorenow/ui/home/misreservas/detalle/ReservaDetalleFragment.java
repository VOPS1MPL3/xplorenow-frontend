package com.xplorenow.ui.home.misreservas.detalle;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.xplorenow.data.dto.EstadoReserva;
import com.xplorenow.data.dto.ReservaDetalleDTO;
import com.xplorenow.data.repository.ReservaRepository;
import com.xplorenow.databinding.FragmentReservaDetalleBinding;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ReservaDetalleFragment extends Fragment {

    private static final String TAG = "ReservaDetalleFragment";
    public static final String ARG_RESERVA_ID = "reservaId";

    private FragmentReservaDetalleBinding binding;

    @Inject
    ReservaRepository reservaRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReservaDetalleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        binding.btnVerMapa.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "El mapa se va a integrar en el punto 10",
                        Toast.LENGTH_SHORT).show());

        long reservaId = requireArguments().getLong(ARG_RESERVA_ID, -1L);
        if (reservaId < 0) {
            Toast.makeText(requireContext(), "Reserva invalida", Toast.LENGTH_SHORT).show();
            return;
        }
        cargarDetalle(reservaId);
    }

    private void cargarDetalle(long id) {
        reservaRepository.obtenerReserva(id).enqueue(new Callback<ReservaDetalleDTO>() {
            @Override
            public void onResponse(Call<ReservaDetalleDTO> call,
                                   Response<ReservaDetalleDTO> response) {
                if (binding == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    mostrar(response.body());
                } else {
                    Toast.makeText(requireContext(),
                            "No se pudo cargar la reserva",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReservaDetalleDTO> call, Throwable t) {
                if (binding == null) return;
                Log.e(TAG, "onFailure", t);
                Toast.makeText(requireContext(),
                        "Error de red: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrar(ReservaDetalleDTO d) {
        Glide.with(this)
                .load(d.getActividadImagen())
                .placeholder(android.R.color.darker_gray)
                .into(binding.ivImagen);

        binding.tvVoucher.setText(d.getVoucherCodigo());
        binding.tvEstado.setText(d.getEstado() != null ? d.getEstado().name() : "");
        binding.tvEstado.setBackgroundColor(colorPara(d.getEstado()));

        binding.tvNombre.setText(d.getActividadNombre());
        binding.tvDestinoCategoria.setText(
                d.getDestino() + " - " + d.getCategoria());

        String hora = d.getHora() != null && d.getHora().length() >= 5
                ? d.getHora().substring(0, 5) : d.getHora();
        binding.tvFechaHora.setText(d.getFecha() + " - " + hora);

        binding.tvParticipantes.setText(d.getCantidadParticipantes() + " personas");
        binding.tvPuntoEncuentro.setText(d.getPuntoEncuentro());
        binding.tvGuia.setText(d.getGuiaAsignado());
        binding.tvIdioma.setText(d.getIdioma());
        binding.tvPolitica.setText(d.getPoliticaCancelacion());
    }

    private int colorPara(EstadoReserva e) {
        if (e == null) return Color.GRAY;
        switch (e) {
            case CONFIRMADA: return Color.parseColor("#1B5E20");
            case CANCELADA:  return Color.parseColor("#B71C1C");
            case FINALIZADA: return Color.parseColor("#37474F");
            default: return Color.GRAY;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
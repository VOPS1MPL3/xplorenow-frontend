package com.xplorenow.ui.home.misreservas.detalle;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.xplorenow.R;
import com.xplorenow.data.dto.EstadoReserva;
import com.xplorenow.data.dto.ReservaDetalleDTO;
import com.xplorenow.data.repository.ReservaRepository;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ReservaDetalleFragment extends Fragment {

    private static final String TAG = "ReservaDetalleFragment";
    public static final String ARG_RESERVA_ID = "reservaId";

    private MaterialToolbar toolbar;
    private ImageView ivImagen;
    private TextView tvVoucher, tvEstado, tvNombre, tvDestinoCategoria;
    private TextView tvFechaHora, tvParticipantes, tvPuntoEncuentro;
    private TextView tvGuia, tvIdioma, tvPolitica;
    private Button btnVerMapa, btnCancelar;

    private long reservaIdActual = -1L;

    @Inject
    ReservaRepository reservaRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reserva_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.toolbar);
        ivImagen = view.findViewById(R.id.ivImagen);
        tvVoucher = view.findViewById(R.id.tvVoucher);
        tvEstado = view.findViewById(R.id.tvEstado);
        tvNombre = view.findViewById(R.id.tvNombre);
        tvDestinoCategoria = view.findViewById(R.id.tvDestinoCategoria);
        tvFechaHora = view.findViewById(R.id.tvFechaHora);
        tvParticipantes = view.findViewById(R.id.tvParticipantes);
        tvPuntoEncuentro = view.findViewById(R.id.tvPuntoEncuentro);
        tvGuia = view.findViewById(R.id.tvGuia);
        tvIdioma = view.findViewById(R.id.tvIdioma);
        tvPolitica = view.findViewById(R.id.tvPolitica);
        btnVerMapa = view.findViewById(R.id.btnVerMapa);
        btnCancelar = view.findViewById(R.id.btnCancelar);

        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        btnVerMapa.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "El mapa se va a integrar en el punto 10",
                        Toast.LENGTH_SHORT).show());

        btnCancelar.setOnClickListener(v -> confirmarCancelacion());

        reservaIdActual = requireArguments().getLong(ARG_RESERVA_ID, -1L);
        if (reservaIdActual < 0) {
            Toast.makeText(requireContext(), "Reserva invalida", Toast.LENGTH_SHORT).show();
            return;
        }
        cargarDetalle(reservaIdActual);
    }

    private void cargarDetalle(long id) {
        reservaRepository.obtenerReserva(id).enqueue(new Callback<ReservaDetalleDTO>() {
            @Override
            public void onResponse(Call<ReservaDetalleDTO> call,
                                   Response<ReservaDetalleDTO> response) {
                if (getView() == null) return;
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
                if (getView() == null) return;
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
                .into(ivImagen);

        tvVoucher.setText(d.getVoucherCodigo());
        tvEstado.setText(d.getEstado() != null ? d.getEstado().name() : "");
        tvEstado.setBackgroundColor(colorPara(d.getEstado()));

        tvNombre.setText(d.getActividadNombre());
        tvDestinoCategoria.setText(d.getDestino() + " - " + d.getCategoria());

        String hora = d.getHora() != null && d.getHora().length() >= 5
                ? d.getHora().substring(0, 5) : d.getHora();
        tvFechaHora.setText(d.getFecha() + " - " + hora);

        tvParticipantes.setText(d.getCantidadParticipantes() + " personas");
        tvPuntoEncuentro.setText(d.getPuntoEncuentro());
        tvGuia.setText(d.getGuiaAsignado());
        tvIdioma.setText(d.getIdioma());
        tvPolitica.setText(d.getPoliticaCancelacion());

        if (d.getEstado() == EstadoReserva.CONFIRMADA) {
            btnCancelar.setVisibility(View.VISIBLE);
        } else {
            btnCancelar.setVisibility(View.GONE);
        }
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

    private void confirmarCancelacion() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelar reserva")
                .setMessage("¿Estas seguro de cancelar esta reserva?")
                .setPositiveButton("Si, cancelar", (dialog, which) -> ejecutarCancelacion())
                .setNegativeButton("No", null)
                .show();
    }

    private void ejecutarCancelacion() {
        if (reservaIdActual < 0) return;
        btnCancelar.setEnabled(false);
        btnCancelar.setText("Cancelando...");

        reservaRepository.cancelarReserva(reservaIdActual).enqueue(
                new Callback<ReservaDetalleDTO>() {
                    @Override
                    public void onResponse(Call<ReservaDetalleDTO> call,
                                           Response<ReservaDetalleDTO> response) {
                        if (getView() == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(requireContext(),
                                    "Reserva cancelada",
                                    Toast.LENGTH_SHORT).show();
                            mostrar(response.body());
                        } else {
                            btnCancelar.setEnabled(true);
                            btnCancelar.setText("Cancelar reserva");
                            Toast.makeText(requireContext(),
                                    "No se pudo cancelar (HTTP " + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ReservaDetalleDTO> call, Throwable t) {
                        if (getView() == null) return;
                        btnCancelar.setEnabled(true);
                        btnCancelar.setText("Cancelar reserva");
                        Toast.makeText(requireContext(),
                                "Error de red: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, "cancelar onFailure", t);
                    }
                });
    }
}
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
import com.xplorenow.data.api.XploreNowApi;
import com.xplorenow.data.dto.CalificacionDTO;
import com.xplorenow.data.dto.EstadoReserva;
import com.xplorenow.data.dto.ReservaDetalleDTO;
import com.xplorenow.data.repository.ReservaRepository;
import com.xplorenow.ui.home.calificacion.CalificacionFragment;
import com.xplorenow.util.MapUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
    private Button btnVerMapa, btnCancelar, btnCalificar;

    private View layoutCalificacionExistente;
    private TextView tvRatingActividad, tvRatingGuia, tvComentario;

    private long reservaIdActual = -1L;
    private String actividadNombre = "";
    private String fechaActividad  = "";
    private String horaActividad   = "";
    private ReservaDetalleDTO reservaActual;

    @Inject
    ReservaRepository reservaRepository;

    @Inject
    XploreNowApi api;

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

        toolbar                     = view.findViewById(R.id.toolbar);
        ivImagen                    = view.findViewById(R.id.ivImagen);
        tvVoucher                   = view.findViewById(R.id.tvVoucher);
        tvEstado                    = view.findViewById(R.id.tvEstado);
        tvNombre                    = view.findViewById(R.id.tvNombre);
        tvDestinoCategoria          = view.findViewById(R.id.tvDestinoCategoria);
        tvFechaHora                 = view.findViewById(R.id.tvFechaHora);
        tvParticipantes             = view.findViewById(R.id.tvParticipantes);
        tvPuntoEncuentro            = view.findViewById(R.id.tvPuntoEncuentro);
        tvGuia                      = view.findViewById(R.id.tvGuia);
        tvIdioma                    = view.findViewById(R.id.tvIdioma);
        tvPolitica                  = view.findViewById(R.id.tvPolitica);
        btnVerMapa                  = view.findViewById(R.id.btnVerMapa);
        btnCancelar                 = view.findViewById(R.id.btnCancelar);
        btnCalificar                = view.findViewById(R.id.btnCalificar);
        layoutCalificacionExistente = view.findViewById(R.id.layoutCalificacionExistente);
        tvRatingActividad           = view.findViewById(R.id.tvRatingActividad);
        tvRatingGuia                = view.findViewById(R.id.tvRatingGuia);
        tvComentario                = view.findViewById(R.id.tvComentario);

        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        btnVerMapa.setOnClickListener(v -> {
            if (reservaActual != null) {
                MapUtil.abrirMapaNavegacion(requireContext(),
                        reservaActual.getLatitud(),
                        reservaActual.getLongitud(),
                        reservaActual.getActividadNombre());
            } else {
                Toast.makeText(requireContext(), "Cargando información del mapa...", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelar.setOnClickListener(v -> confirmarCancelacion());

        reservaIdActual = requireArguments().getLong(ARG_RESERVA_ID, -1L);
        if (reservaIdActual < 0) {
            Toast.makeText(requireContext(), "Reserva inválida", Toast.LENGTH_SHORT).show();
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
        this.reservaActual = d;
        actividadNombre = d.getActividadNombre() != null ? d.getActividadNombre() : "";
        fechaActividad  = d.getFecha()           != null ? d.getFecha()           : "";
        horaActividad   = d.getHora()            != null ? d.getHora()            : "";

        Glide.with(this)
                .load(d.getActividadImagen())
                .placeholder(android.R.color.darker_gray)
                .into(ivImagen);

        tvVoucher.setText(d.getVoucherCodigo());
        tvEstado.setText(d.getEstado() != null ? d.getEstado().name() : "");
        tvEstado.setBackgroundColor(colorPara(d.getEstado()));
        tvNombre.setText(actividadNombre);
        tvDestinoCategoria.setText(d.getDestino() + " - " + d.getCategoria());

        String horaCorta = horaActividad.length() >= 5
                ? horaActividad.substring(0, 5) : horaActividad;
        tvFechaHora.setText(fechaActividad + " - " + horaCorta);

        tvParticipantes.setText(d.getCantidadParticipantes() + " personas");
        tvPuntoEncuentro.setText(d.getPuntoEncuentro());
        tvGuia.setText(d.getGuiaAsignado());
        tvIdioma.setText(d.getIdioma());
        tvPolitica.setText(d.getPoliticaCancelacion());

        btnCancelar.setVisibility(
                d.getEstado() == EstadoReserva.CONFIRMADA ? View.VISIBLE : View.GONE);

        btnCalificar.setVisibility(View.GONE);
        layoutCalificacionExistente.setVisibility(View.GONE);

        if (d.getEstado() == EstadoReserva.FINALIZADA) {
            verificarCalificacion();
        }
    }

    private void verificarCalificacion() {
        api.obtenerCalificacion(reservaIdActual)
                .enqueue(new Callback<CalificacionDTO>() {
                    @Override
                    public void onResponse(Call<CalificacionDTO> call,
                                           Response<CalificacionDTO> response) {
                        if (getView() == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            // Ya calificó → mostrar en modo lectura
                            mostrarCalificacionExistente(response.body());
                        } else {
                            // No calificó → verificar si está dentro de las 48hs
                            if (dentroDeVentana48hs()) {
                                btnCalificar.setVisibility(View.VISIBLE);
                                btnCalificar.setOnClickListener(v -> irACalificar(v));
                            } else {
                                mostrarAvisoVencido();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CalificacionDTO> call, Throwable t) {
                        if (getView() == null) return;
                        // Si hay error de red asumimos que puede calificar
                        btnCalificar.setVisibility(View.VISIBLE);
                        btnCalificar.setOnClickListener(v -> irACalificar(v));
                    }
                });
    }

    /**
     * Calcula si la actividad terminó hace menos de 48hs.
     * Usa la fecha y hora de la actividad que ya están en el DTO.
     */
    private boolean dentroDeVentana48hs() {
        try {
            String horaCorta = horaActividad.length() >= 5
                    ? horaActividad.substring(0, 5) : horaActividad;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            Date fechaHoraActividad = sdf.parse(fechaActividad + " " + horaCorta);
            if (fechaHoraActividad == null) return false;
            long diff = System.currentTimeMillis() - fechaHoraActividad.getTime();
            return diff <= 48L * 60 * 60 * 1000;
        } catch (Exception e) {
            return false;
        }
    }

    private void mostrarCalificacionExistente(CalificacionDTO c) {
        layoutCalificacionExistente.setVisibility(View.VISIBLE);
        tvRatingActividad.setText("Actividad: " + estrellas(c.getRatingActividad()));
        tvRatingGuia.setText("Guía: " + estrellas(c.getRatingGuia()));
        tvRatingGuia.setVisibility(View.VISIBLE);
        if (c.getComentario() != null && !c.getComentario().isEmpty()) {
            tvComentario.setText("\"" + c.getComentario() + "\"");
            tvComentario.setVisibility(View.VISIBLE);
        } else {
            tvComentario.setVisibility(View.GONE);
        }
        // Al tocar abre dialog con detalle completo
        layoutCalificacionExistente.setOnClickListener(v ->
                mostrarDialogCalificacion(c));
    }

    private void mostrarDialogCalificacion(CalificacionDTO c) {
        String mensaje =
                "Actividad: " + estrellas(c.getRatingActividad()) + "\n" +
                        "Guía: "      + estrellas(c.getRatingGuia());

        if (c.getComentario() != null && !c.getComentario().isEmpty()) {
            mensaje += "\n\n\"" + c.getComentario() + "\"";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Tu calificación")
                .setMessage(mensaje)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void mostrarAvisoVencido() {
        layoutCalificacionExistente.setVisibility(View.VISIBLE);
        layoutCalificacionExistente.setOnClickListener(null);
        tvRatingActividad.setText("⏰ El plazo para calificar esta actividad venció");
        tvRatingActividad.setTextColor(Color.parseColor("#B71C1C"));
        tvRatingGuia.setVisibility(View.GONE);
        tvComentario.setVisibility(View.GONE);
    }

    private String estrellas(Integer rating) {
        if (rating == null) return "-";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rating; i++) sb.append("★");
        for (int i = rating; i < 5; i++) sb.append("☆");
        return sb.toString();
    }

    private void irACalificar(View view) {
        Bundle args = new Bundle();
        args.putLong(CalificacionFragment.ARG_RESERVA_ID, reservaIdActual);
        args.putString(CalificacionFragment.ARG_ACTIVIDAD_NOMBRE, actividadNombre);
        Navigation.findNavController(view)
                .navigate(R.id.action_reservaDetalle_to_calificacion, args);
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
                .setMessage("¿Estás seguro de cancelar esta reserva?")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> ejecutarCancelacion())
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
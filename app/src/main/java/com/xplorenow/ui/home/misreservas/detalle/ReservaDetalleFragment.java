package com.xplorenow.ui.home.misreservas.detalle;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import com.xplorenow.util.NetworkObserver;

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
    private TextView tvOfflineBanner;
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
    private Double latitud = null;
    private Double longitud = null;
    private boolean isOnline = true;

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
        tvOfflineBanner             = view.findViewById(R.id.tvOfflineBanner);
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

        new NetworkObserver(requireContext()).observe(getViewLifecycleOwner(), connected -> {
            isOnline = connected;
            tvOfflineBanner.setVisibility(connected ? View.GONE : View.VISIBLE);
        });

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

        // Observar la base de datos local para cambios en tiempo real
        reservaRepository.observarReserva(reservaIdActual).observe(getViewLifecycleOwner(), dto -> {
            if (dto != null) {
                mostrar(dto);
            }
        });

        cargarDetalle(reservaIdActual);
    }

    private void cargarDetalle(long id) {
        if (!isOnline) {
            cargarOffline(id);
            return;
        }

        reservaRepository.obtenerReserva(id).enqueue(new Callback<ReservaDetalleDTO>() {
            @Override
            public void onResponse(Call<ReservaDetalleDTO> call,
                                   Response<ReservaDetalleDTO> response) {
                if (getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    ReservaDetalleDTO dto = response.body();
                    mostrar(dto);
                    reservaRepository.guardarReservaLocal(dto);
                } else {
                    Toast.makeText(requireContext(),
                            "No se pudo cargar la reserva",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReservaDetalleDTO> call, Throwable t) {
                if (getView() == null) return;
                Log.e(TAG, "onFailure network, intentando offline", t);
                cargarOffline(id);
            }
        });
    }

    private void cargarOffline(long id) {
        reservaRepository.obtenerReservaOffline(id, result -> {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (result == null) {
                    Toast.makeText(requireContext(),
                            "No hay conexión y no existen datos guardados",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mostrar(result);
                }
            });
        });
    }

    private void mostrar(ReservaDetalleDTO d) {
        this.reservaActual = d;
        actividadNombre = d.getActividadNombre() != null ? d.getActividadNombre() : "";
        fechaActividad  = d.getFecha()           != null ? d.getFecha()           : "";
        horaActividad   = d.getHora()            != null ? d.getHora()            : "";
        latitud         = d.getLatitud();
        longitud        = d.getLongitud();

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
        btnVerMapa.setEnabled(d.getLatitud() != null && d.getLongitud() != null
                && d.getLatitud() != 0.0 && d.getLongitud() != 0.0);

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
                            mostrarCalificacionExistente(response.body());
                        } else {
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
                        btnCalificar.setVisibility(View.VISIBLE);
                        btnCalificar.setOnClickListener(v -> irACalificar(v));
                    }
                });
    }

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
        tvRatingActividad.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), com.xplorenow.R.color.estado_cancelada));
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

    private void abrirMapa(double lat, double lng) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng);
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    private int colorPara(EstadoReserva e) {
        if (e == null) return Color.GRAY;
        switch (e) {
            case CONFIRMADA: return androidx.core.content.ContextCompat.getColor(requireContext(), com.xplorenow.R.color.estado_confirmada);
            case CANCELADA:  return androidx.core.content.ContextCompat.getColor(requireContext(), com.xplorenow.R.color.estado_cancelada);
            case FINALIZADA: return androidx.core.content.ContextCompat.getColor(requireContext(), com.xplorenow.R.color.estado_finalizada);
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

        // FIX: si no hay conexión, encolar directamente sin esperar timeout de Retrofit
        if (!isOnline) {
            reservaRepository.encolarCancelacion(reservaIdActual);
            Toast.makeText(requireContext(),
                    "Sin conexión. La cancelación se sincronizará automáticamente al recuperar red.",
                    Toast.LENGTH_LONG).show();
            return;
        }

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
                            reservaRepository.guardarReservaLocal(response.body());
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
                        reservaRepository.encolarCancelacion(reservaIdActual);
                        Toast.makeText(requireContext(),
                                "Sin conexión. La cancelación se sincronizará automáticamente al recuperar red.",
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, "cancelar onFailure", t);
                    }
                });
    }
}
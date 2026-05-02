package com.xplorenow.ui.home.misreservas;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.xplorenow.R;
import com.xplorenow.data.dto.EstadoReserva;
import com.xplorenow.data.dto.ReservaDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReservaAdapter extends ArrayAdapter<ReservaDTO> {

    /** Plazo para resenar despues de que la reserva queda FINALIZADA. */
    private static final long HORAS_PARA_RESENAR = 48;

    public ReservaAdapter(@NonNull Context context, @NonNull List<ReservaDTO> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_reserva, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ReservaDTO r = getItem(position);
        if (r != null) holder.bind(r);
        return convertView;
    }

    private static class ViewHolder {
        final ImageView ivImagen;
        final TextView tvNombre, tvFechaHora, tvParticipantes, tvEstado, tvBadgeResena;

        ViewHolder(View row) {
            ivImagen = row.findViewById(R.id.ivImagen);
            tvNombre = row.findViewById(R.id.tvNombre);
            tvFechaHora = row.findViewById(R.id.tvFechaHora);
            tvParticipantes = row.findViewById(R.id.tvParticipantes);
            tvEstado = row.findViewById(R.id.tvEstado);
            tvBadgeResena = row.findViewById(R.id.tvBadgeResena);
        }

        void bind(ReservaDTO r) {
            tvNombre.setText(r.getActividadNombre());
            String hora = r.getHora() != null && r.getHora().length() >= 5
                    ? r.getHora().substring(0, 5) : r.getHora();
            tvFechaHora.setText(r.getFecha() + " - " + hora);
            tvParticipantes.setText(r.getCantidadParticipantes() + " participantes");

            EstadoReserva estado = r.getEstado();
            tvEstado.setText(estado != null ? estado.name() : "");
            tvEstado.setBackgroundColor(colorPara(estado));

            actualizarBadgeResena(r);

            Glide.with(ivImagen.getContext())
                    .load(r.getActividadImagen())
                    .placeholder(android.R.color.darker_gray)
                    .into(ivImagen);
        }

        private void actualizarBadgeResena(ReservaDTO r) {
            Context ctx = ivImagen.getContext();

            if (r.getEstado() != EstadoReserva.FINALIZADA) {
                tvBadgeResena.setVisibility(View.GONE);
                return;
            }

            if (r.getCalificacion() != null) {
                tvBadgeResena.setText("RESEÑADO");
                tvBadgeResena.setBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.estado_confirmada));
                tvBadgeResena.setVisibility(View.VISIBLE);
                return;
            }

            if (estaEnPlazoParaResenar(r)) {
                tvBadgeResena.setText("RESEÑAR");
                tvBadgeResena.setBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.estado_pendiente_resenar));
                tvBadgeResena.setVisibility(View.VISIBLE);
            } else {
                tvBadgeResena.setVisibility(View.GONE);
            }
        }

        /**
         * Devuelve true si pasaron menos de 48 horas desde que la reserva
         * termino (fecha + hora del horario reservado).
         */
        private boolean estaEnPlazoParaResenar(ReservaDTO r) {
            try {
                if (r.getFecha() == null || r.getHora() == null) return false;

                String[] f = r.getFecha().split("-");
                LocalDate fecha = LocalDate.of(
                        Integer.parseInt(f[0]),
                        Integer.parseInt(f[1]),
                        Integer.parseInt(f[2]));

                String horaStr = r.getHora().length() >= 5
                        ? r.getHora().substring(0, 5) : r.getHora();
                String[] h = horaStr.split(":");
                LocalTime hora = LocalTime.of(
                        Integer.parseInt(h[0]),
                        Integer.parseInt(h[1]));

                LocalDateTime finReserva = LocalDateTime.of(fecha, hora);
                long horas = ChronoUnit.HOURS.between(finReserva, LocalDateTime.now());

                return horas >= 0 && horas <= HORAS_PARA_RESENAR;
            } catch (Exception e) {
                return false;
            }
        }

        private int colorPara(EstadoReserva estado) {
            if (estado == null) return Color.GRAY;
            Context ctx = ivImagen.getContext();
            switch (estado) {
                case CONFIRMADA: return ContextCompat.getColor(ctx, R.color.estado_confirmada);
                case CANCELADA:  return ContextCompat.getColor(ctx, R.color.estado_cancelada);
                case FINALIZADA: return ContextCompat.getColor(ctx, R.color.estado_finalizada);
                default: return Color.GRAY;
            }
        }
    }
}
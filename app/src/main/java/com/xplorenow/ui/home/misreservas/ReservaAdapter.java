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
import com.bumptech.glide.Glide;
import com.xplorenow.R;
import com.xplorenow.data.dto.EstadoReserva;
import com.xplorenow.data.dto.ReservaDTO;

import java.util.List;

public class ReservaAdapter extends ArrayAdapter<ReservaDTO> {

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
        final TextView tvNombre, tvFechaHora, tvParticipantes, tvEstado;

        ViewHolder(View row) {
            ivImagen = row.findViewById(R.id.ivImagen);
            tvNombre = row.findViewById(R.id.tvNombre);
            tvFechaHora = row.findViewById(R.id.tvFechaHora);
            tvParticipantes = row.findViewById(R.id.tvParticipantes);
            tvEstado = row.findViewById(R.id.tvEstado);
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

            Glide.with(ivImagen.getContext())
                    .load(r.getActividadImagen())
                    .placeholder(android.R.color.darker_gray)
                    .into(ivImagen);
        }

        private int colorPara(EstadoReserva estado) {
            if (estado == null) return Color.GRAY;
            android.content.Context ctx = ivImagen.getContext();
            switch (estado) {
                case CONFIRMADA: return androidx.core.content.ContextCompat.getColor(ctx, com.xplorenow.R.color.estado_confirmada);
                case CANCELADA:  return androidx.core.content.ContextCompat.getColor(ctx, com.xplorenow.R.color.estado_cancelada);
                case FINALIZADA: return androidx.core.content.ContextCompat.getColor(ctx, com.xplorenow.R.color.estado_finalizada);
                default: return Color.GRAY;
            }
        }
    }
}
package com.xplorenow.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.xplorenow.R;
import com.xplorenow.data.dto.ActividadDTO;
import com.xplorenow.data.util.PrecioFormatter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class ActividadAdapter extends ArrayAdapter<ActividadDTO> {

    /** Callback que ejecuta el Fragment cuando el usuario toca el corazon. */
    public interface OnFavoritoToggleListener {
        void onToggle(ActividadDTO actividad, boolean nuevoEstado);
    }

    private final Set<Long> favoritosIds = new HashSet<>();
    private OnFavoritoToggleListener favoritoListener;

    public ActividadAdapter(@NonNull Context context, @NonNull List<ActividadDTO> items) {
        super(context, 0, items);
    }

    public void setFavoritoListener(OnFavoritoToggleListener listener) {
        this.favoritoListener = listener;
    }

    public void setFavoritos(@NonNull Set<Long> ids) {
        favoritosIds.clear();
        favoritosIds.addAll(ids);
        notifyDataSetChanged();
    }

    public void marcarLocal(long actividadId) {
        favoritosIds.add(actividadId);
        notifyDataSetChanged();
    }

    public void desmarcarLocal(long actividadId) {
        favoritosIds.remove(actividadId);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_actividad, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ActividadDTO actividad = getItem(position);
        if (actividad != null) {
            holder.bind(actividad, favoritosIds.contains(actividad.getId()),
                    favoritoListener);
        }

        return convertView;
    }

    private static class ViewHolder {
        final ImageView ivImagen;
        final ImageButton btnFavorito;
        final TextView tvNombre;
        final TextView tvDestinoCategoria;
        final TextView tvDuracion;
        final TextView tvPrecio;
        final TextView tvCupos;

        ViewHolder(View row) {
            ivImagen = row.findViewById(R.id.ivImagen);
            btnFavorito = row.findViewById(R.id.btnFavorito);
            tvNombre = row.findViewById(R.id.tvNombre);
            tvDestinoCategoria = row.findViewById(R.id.tvDestinoCategoria);
            tvDuracion = row.findViewById(R.id.tvDuracion);
            tvPrecio = row.findViewById(R.id.tvPrecio);
            tvCupos = row.findViewById(R.id.tvCupos);
        }

        void bind(ActividadDTO a, boolean esFavorita,
                  OnFavoritoToggleListener listener) {
            tvNombre.setText(a.getNombre());
            tvDestinoCategoria.setText(a.getDestino() + " - " + a.getCategoria());
            tvDuracion.setText(formatDuracion(a.getDuracionMinutos()));
            tvPrecio.setText(PrecioFormatter.format(a.getPrecio()));
            tvCupos.setText(a.getCuposDisponibles() + " cupos");

            Glide.with(ivImagen.getContext())
                    .load(a.getImagenPrincipal())
                    .placeholder(android.R.color.darker_gray)
                    .into(ivImagen);

            // Estado del corazon
            btnFavorito.setImageResource(esFavorita
                    ? R.drawable.ic_corazon_lleno
                    : R.drawable.ic_corazon_vacio);

            btnFavorito.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggle(a, !esFavorita);
                }
            });
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
}

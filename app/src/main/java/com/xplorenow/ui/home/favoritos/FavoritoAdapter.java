package com.xplorenow.ui.home.favoritos;

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
import com.xplorenow.data.dto.FavoritoDTO;
import com.xplorenow.data.util.PrecioFormatter;

import java.util.List;

/**
 * Adapter de la pantalla "Mis favoritos" (Punto 7 del TPO).
 *
 * Muestra cada favorito como una card horizontal con imagen, nombre,
 * destino+categoria, precio y, si corresponde, un badge de novedad
 * ("¡Bajo de precio!" / "¡Hay mas cupos!"). El boton corazon a la
 * derecha permite quitarlo de favoritos sin abrir el detalle.
 */
public class FavoritoAdapter extends ArrayAdapter<FavoritoDTO> {

    public interface OnQuitarListener {
        void onQuitar(FavoritoDTO favorito);
    }

    private OnQuitarListener quitarListener;

    public FavoritoAdapter(@NonNull Context context, @NonNull List<FavoritoDTO> items) {
        super(context, 0, items);
    }

    public void setOnQuitarListener(OnQuitarListener l) {
        this.quitarListener = l;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_favorito, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FavoritoDTO f = getItem(position);
        if (f != null) holder.bind(f, quitarListener);
        return convertView;
    }

    private static class ViewHolder {
        final ImageView ivImagen;
        final TextView tvNombre;
        final TextView tvDestinoCategoria;
        final TextView tvPrecio;
        final TextView tvBadgeNovedad;
        final ImageButton btnQuitar;

        ViewHolder(View row) {
            ivImagen           = row.findViewById(R.id.ivImagen);
            tvNombre           = row.findViewById(R.id.tvNombre);
            tvDestinoCategoria = row.findViewById(R.id.tvDestinoCategoria);
            tvPrecio           = row.findViewById(R.id.tvPrecio);
            tvBadgeNovedad     = row.findViewById(R.id.tvBadgeNovedad);
            btnQuitar          = row.findViewById(R.id.btnQuitar);
        }

        void bind(FavoritoDTO f, OnQuitarListener listener) {
            tvNombre.setText(f.getActividadNombre());
            tvDestinoCategoria.setText(
                    nz(f.getDestino()) + " - " + nz(f.getCategoria()));
            tvPrecio.setText(PrecioFormatter.format(f.getPrecioActual()));

            Glide.with(ivImagen.getContext())
                    .load(f.getActividadImagen())
                    .placeholder(android.R.color.darker_gray)
                    .into(ivImagen);

            // Badge de novedad (Punto 16). El backend ya nos dice el motivo.
            if (f.isTieneNovedad() && f.getMotivoNovedad() != null) {
                String texto;
                if ("BAJO_PRECIO".equals(f.getMotivoNovedad())) {
                    texto = "¡Bajó de precio!";
                } else if ("MAS_CUPOS".equals(f.getMotivoNovedad())) {
                    texto = "¡Hay más cupos!";
                } else {
                    texto = "¡Novedad!";
                }
                tvBadgeNovedad.setText(texto);
                tvBadgeNovedad.setVisibility(View.VISIBLE);
            } else {
                tvBadgeNovedad.setVisibility(View.GONE);
            }

            btnQuitar.setOnClickListener(v -> {
                if (listener != null) listener.onQuitar(f);
            });
        }

        private static String nz(String s) { return s == null ? "" : s; }
    }
}

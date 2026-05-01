package com.xplorenow.ui.home;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.xplorenow.R;
import com.xplorenow.data.dto.ActividadDTO;
import com.xplorenow.data.util.PrecioFormatter;
import java.util.List;


public class ActividadAdapter extends ArrayAdapter<ActividadDTO> {

    public ActividadAdapter(@NonNull Context context, @NonNull List<ActividadDTO> items) {
        // El segundo parametro (resource) no lo usamos porque inflamos el
        // layout manualmente, pero el constructor lo pide. Pasamos 0.
        super(context, 0, items);
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
            holder.bind(actividad);
        }

        return convertView;
    }
    private static class ViewHolder {
        final ImageView ivImagen;
        final TextView tvNombre;
        final TextView tvDestinoCategoria;
        final TextView tvDuracion;
        final TextView tvPrecio;
        final TextView tvCupos;

        ViewHolder(View row) {
            ivImagen = row.findViewById(R.id.ivImagen);
            tvNombre = row.findViewById(R.id.tvNombre);
            tvDestinoCategoria = row.findViewById(R.id.tvDestinoCategoria);
            tvDuracion = row.findViewById(R.id.tvDuracion);
            tvPrecio = row.findViewById(R.id.tvPrecio);
            tvCupos = row.findViewById(R.id.tvCupos);
        }

        void bind(ActividadDTO a) {
            tvNombre.setText(a.getNombre());
            tvDestinoCategoria.setText(a.getDestino() + " - " + a.getCategoria());
            tvDuracion.setText(formatDuracion(a.getDuracionMinutos()));
            tvPrecio.setText(PrecioFormatter.format(a.getPrecio()));
            tvCupos.setText(a.getCuposDisponibles() + " cupos");

            Glide.with(ivImagen.getContext())
                    .load(a.getImagenPrincipal())
                    .placeholder(android.R.color.darker_gray)
                    .into(ivImagen);
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
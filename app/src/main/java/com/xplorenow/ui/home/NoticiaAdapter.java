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
import com.xplorenow.data.dto.NoticiaDTO;
import java.util.List;

public class NoticiaAdapter extends ArrayAdapter<NoticiaDTO> {

    public NoticiaAdapter(@NonNull Context context, @NonNull List<NoticiaDTO> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_noticia, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        NoticiaDTO n = getItem(position);
        if (n != null) holder.bind(n);
        return convertView;
    }

    private static class ViewHolder {
        final ImageView ivImagen;
        final TextView tvTitulo;
        final TextView tvDescripcion;

        ViewHolder(View row) {
            ivImagen = row.findViewById(R.id.ivNoticiaImagen);
            tvTitulo = row.findViewById(R.id.tvNoticiaTitulo);
            tvDescripcion = row.findViewById(R.id.tvNoticiaDescripcion);
        }

        void bind(NoticiaDTO n) {
            tvTitulo.setText(n.getTitulo());
            tvDescripcion.setText(n.getDescripcionBreve());
            Glide.with(ivImagen.getContext())
                    .load(n.getImagenUrl())
                    .placeholder(android.R.color.darker_gray)
                    .into(ivImagen);
        }
    }

    private boolean limitado = true;

    public void setLimitado(boolean limitado) {
        this.limitado = limitado;
    }

    @Override
    public int getCount() {
        if (limitado) return Math.min(super.getCount(), 2);
        return super.getCount();
    }


}
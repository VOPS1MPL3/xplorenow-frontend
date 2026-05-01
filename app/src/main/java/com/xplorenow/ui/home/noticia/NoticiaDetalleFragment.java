package com.xplorenow.ui.home.noticia;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.xplorenow.R;
import com.xplorenow.data.dto.NoticiaDTO;
import com.xplorenow.data.repository.NoticiaRepository;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class NoticiaDetalleFragment extends Fragment {

    private static final String TAG = "NoticiaDetalleFragment";
    public static final String ARG_NOTICIA_ID = "noticiaId";

    private MaterialToolbar toolbar;
    private ImageView ivImagen;
    private TextView tvTitulo;
    private TextView tvDescripcionCompleta;
    private TextView tvStatus;

    @Inject
    NoticiaRepository noticiaRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_noticia_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.toolbar);
        ivImagen = view.findViewById(R.id.ivImagen);
        tvTitulo = view.findViewById(R.id.tvTitulo);
        tvDescripcionCompleta = view.findViewById(R.id.tvDescripcionCompleta);
        tvStatus = view.findViewById(R.id.tvStatus);

        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        long noticiaId = requireArguments().getLong(ARG_NOTICIA_ID, -1L);
        if (noticiaId < 0) {
            Toast.makeText(requireContext(),
                    "Noticia invalida", Toast.LENGTH_SHORT).show();
            return;
        }
        cargarNoticia(noticiaId);
    }

    private void cargarNoticia(long noticiaId) {
        tvStatus.setText("Cargando...");
        tvStatus.setVisibility(View.VISIBLE);

        noticiaRepository.obtenerNoticia(noticiaId).enqueue(new Callback<NoticiaDTO>() {
            @Override
            public void onResponse(Call<NoticiaDTO> call,
                                   Response<NoticiaDTO> response) {
                if (getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    mostrarNoticia(response.body());
                } else {
                    Log.e(TAG, "HTTP " + response.code());
                    tvStatus.setText("No se pudo cargar la noticia");
                }
            }

            @Override
            public void onFailure(Call<NoticiaDTO> call, Throwable t) {
                if (getView() == null) return;
                Log.e(TAG, "onFailure", t);
                tvStatus.setText("Error de red: " + t.getMessage());
            }
        });
    }

    private void mostrarNoticia(NoticiaDTO n) {
        tvStatus.setVisibility(View.GONE);
        tvTitulo.setText(n.getTitulo());
        tvDescripcionCompleta.setText(n.getDescripcionBreve());
        Glide.with(this)
                .load(n.getImagenUrl())
                .placeholder(android.R.color.darker_gray)
                .into(ivImagen);
    }
}
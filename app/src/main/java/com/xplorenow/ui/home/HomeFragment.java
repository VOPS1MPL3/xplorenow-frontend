package com.xplorenow.ui.home;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.xplorenow.data.dto.ActividadDTO;
import com.xplorenow.data.dto.PageResponseDTO;
import com.xplorenow.data.repository.ActividadRepository;
import com.xplorenow.databinding.FragmentHomeBinding;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;

    @Inject
    ActividadRepository actividadRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvStatus.setText("Conectando con el backend...");
        cargarActividades();
    }

    private void cargarActividades() {
        actividadRepository.listarActividades(0, 20).enqueue(
                new Callback<PageResponseDTO<ActividadDTO>>() {

                    @Override
                    public void onResponse(
                            Call<PageResponseDTO<ActividadDTO>> call,
                            Response<PageResponseDTO<ActividadDTO>> response) {

                        // Si el fragment fue destruido mientras esperabamos,
                        // no toquemos la UI
                        if (binding == null) return;

                        if (response.isSuccessful() && response.body() != null) {
                            int total = response.body().getTotalElements();
                            int recibidas = response.body().getContent().size();
                            String msg = "Recibi " + recibidas + " actividades del backend\n"
                                    + "(total en BD: " + total + ")";
                            binding.tvStatus.setText(msg);
                            Log.i(TAG, msg);
                        } else {
                            String err = "Error HTTP " + response.code();
                            binding.tvStatus.setText(err);
                            Log.e(TAG, err);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PageResponseDTO<ActividadDTO>> call,
                            Throwable t) {
                        if (binding == null) return;
                        String err = "Error de red: " + t.getMessage();
                        binding.tvStatus.setText(err);
                        Log.e(TAG, err, t);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // evita memory leaks con ViewBinding
    }
}
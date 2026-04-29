package com.xplorenow.ui.home.misreservas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.xplorenow.data.dto.EstadoReserva;
import com.xplorenow.data.dto.ReservaDTO;
import com.xplorenow.data.repository.ReservaRepository;
import com.xplorenow.databinding.FragmentMisReservasBinding;
import androidx.navigation.Navigation;
import com.xplorenow.R;
import com.xplorenow.ui.home.misreservas.detalle.ReservaDetalleFragment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MisReservasFragment extends Fragment {

    private static final String TAG = "MisReservasFragment";

    private FragmentMisReservasBinding binding;
    private ReservaAdapter adapter;
    private final List<ReservaDTO> reservas = new ArrayList<>();

    private final List<String> opcionesFiltro = Arrays.asList(
            "Todas", "Confirmadas", "Canceladas", "Finalizadas");

    @Inject
    ReservaRepository reservaRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMisReservasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ReservaAdapter(requireContext(), reservas);
        binding.lvReservas.setAdapter(adapter);

        binding.lvReservas.setOnItemClickListener((parent, v, position, id) -> {
            ReservaDTO seleccionada = reservas.get(position);
            Bundle args = new Bundle();
            args.putLong(ReservaDetalleFragment.ARG_RESERVA_ID, seleccionada.getId());
            Navigation.findNavController(v).navigate(
                    R.id.action_misReservas_to_reservaDetalle, args);
        });

        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item, opcionesFiltro);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spEstado.setAdapter(spAdapter);

        binding.spEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                cargarReservas(estadoSegunPosicion(pos));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        cargarReservas(null);
    }

    private EstadoReserva estadoSegunPosicion(int pos) {
        switch (pos) {
            case 1: return EstadoReserva.CONFIRMADA;
            case 2: return EstadoReserva.CANCELADA;
            case 3: return EstadoReserva.FINALIZADA;
            default: return null; // Todas
        }
    }

    private void cargarReservas(EstadoReserva estado) {
        binding.tvStatus.setText("Cargando...");
        binding.tvStatus.setVisibility(View.VISIBLE);
        binding.lvReservas.setVisibility(View.GONE);

        reservaRepository.misReservas(estado).enqueue(new Callback<List<ReservaDTO>>() {
            @Override
            public void onResponse(Call<List<ReservaDTO>> call,
                                   Response<List<ReservaDTO>> response) {
                if (binding == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    mostrar(response.body());
                } else {
                    error("Error HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ReservaDTO>> call, Throwable t) {
                if (binding == null) return;
                error("Error de red: " + t.getMessage());
                Log.e(TAG, "onFailure", t);
            }
        });
    }

    private void mostrar(List<ReservaDTO> recibidas) {
        if (recibidas == null || recibidas.isEmpty()) {
            error("No tenes reservas con ese estado");
            return;
        }
        reservas.clear();
        reservas.addAll(recibidas);
        adapter.notifyDataSetChanged();
        binding.tvStatus.setVisibility(View.GONE);
        binding.lvReservas.setVisibility(View.VISIBLE);
    }

    private void error(String msg) {
        binding.tvStatus.setText(msg);
        binding.tvStatus.setVisibility(View.VISIBLE);
        binding.lvReservas.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
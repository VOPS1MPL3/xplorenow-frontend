package com.xplorenow.ui.home.misreservas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.xplorenow.R;
import com.xplorenow.data.dto.EstadoReserva;
import com.xplorenow.data.dto.ReservaDTO;
import com.xplorenow.data.repository.ReservaRepository;
import com.xplorenow.ui.home.misreservas.detalle.ReservaDetalleFragment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.navigation.Navigation;

@AndroidEntryPoint
public class MisReservasFragment extends Fragment {

    private static final String TAG = "MisReservasFragment";

    private Spinner spEstado;
    private TextView tvStatus;
    private ListView lvReservas;

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
        return inflater.inflate(R.layout.fragment_mis_reservas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spEstado = view.findViewById(R.id.spEstado);
        tvStatus = view.findViewById(R.id.tvStatus);
        lvReservas = view.findViewById(R.id.lvReservas);

        adapter = new ReservaAdapter(requireContext(), reservas);
        lvReservas.setAdapter(adapter);

        lvReservas.setOnItemClickListener((parent, v, position, id) -> {
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
        spEstado.setAdapter(spAdapter);

        spEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
            default: return null;
        }
    }

    private void cargarReservas(EstadoReserva estado) {
        tvStatus.setText("Cargando...");
        tvStatus.setVisibility(View.VISIBLE);
        lvReservas.setVisibility(View.GONE);

        reservaRepository.misReservas(estado).enqueue(new Callback<List<ReservaDTO>>() {
            @Override
            public void onResponse(Call<List<ReservaDTO>> call,
                                   Response<List<ReservaDTO>> response) {
                if (getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    mostrar(response.body());
                } else {
                    error("Error HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ReservaDTO>> call, Throwable t) {
                if (getView() == null) return;
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
        tvStatus.setVisibility(View.GONE);
        lvReservas.setVisibility(View.VISIBLE);
    }

    private void error(String msg) {
        tvStatus.setText(msg);
        tvStatus.setVisibility(View.VISIBLE);
        lvReservas.setVisibility(View.GONE);
    }
}
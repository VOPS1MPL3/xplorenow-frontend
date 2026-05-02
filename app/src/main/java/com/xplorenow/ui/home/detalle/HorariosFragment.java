package com.xplorenow.ui.home.detalle;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.appbar.MaterialToolbar;
import com.xplorenow.R;
import com.xplorenow.data.dto.CrearReservaRequest;
import com.xplorenow.data.dto.HorarioDTO;
import com.xplorenow.data.dto.ReservaDetalleDTO;
import com.xplorenow.data.repository.ReservaRepository;
import com.xplorenow.data.api.XploreNowApi;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class HorariosFragment extends Fragment {

    private static final String TAG = "HorariosFragment";
    public static final String ARG_ACTIVIDAD_ID = "actividadId";

    private MaterialToolbar toolbar;
    private TextView tvStatus;
    private ListView lvHorarios;
    private LinearLayout llConfirmar;
    private EditText etCantidad;
    private Button btnReservar;

    private final List<HorarioDTO> horarios = new ArrayList<>();
    private HorarioDTO horarioSeleccionado = null;
    private long actividadId;

    @Inject
    ReservaRepository reservaRepository;

    @Inject
    XploreNowApi api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_horarios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.toolbar);
        tvStatus = view.findViewById(R.id.tvStatus);
        lvHorarios = view.findViewById(R.id.lvHorarios);
        llConfirmar = view.findViewById(R.id.llConfirmar);
        etCantidad = view.findViewById(R.id.etCantidad);
        btnReservar = view.findViewById(R.id.btnReservar);

        actividadId = requireArguments().getLong(ARG_ACTIVIDAD_ID, -1L);

        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        lvHorarios.setOnItemClickListener((parent, v, position, id) -> {
            horarioSeleccionado = horarios.get(position);
            llConfirmar.setVisibility(View.VISIBLE);
            Toast.makeText(requireContext(),
                    "Horario seleccionado: " + horarioSeleccionado.getFecha()
                            + " " + horarioSeleccionado.getHora().substring(0, 5),
                    Toast.LENGTH_SHORT).show();
        });

        btnReservar.setOnClickListener(v -> confirmarReserva());

        cargarHorarios();
    }

    private void cargarHorarios() {
        tvStatus.setText("Cargando horarios disponibles...");
        tvStatus.setVisibility(View.VISIBLE);
        lvHorarios.setVisibility(View.GONE);
        llConfirmar.setVisibility(View.GONE);

        api.getHorariosDisponibles(actividadId).enqueue(new Callback<List<HorarioDTO>>() {
            @Override
            public void onResponse(Call<List<HorarioDTO>> call,
                                   Response<List<HorarioDTO>> response) {
                if (getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    mostrarHorarios(response.body());
                } else {
                    mostrarError("No hay horarios disponibles para esta actividad");
                }
            }

            @Override
            public void onFailure(Call<List<HorarioDTO>> call, Throwable t) {
                if (getView() == null) return;
                Log.e(TAG, "onFailure", t);
                mostrarError("Error de red: " + t.getMessage());
            }
        });
    }

    private void mostrarHorarios(List<HorarioDTO> recibidos) {
        horarios.clear();
        horarios.addAll(recibidos);

        List<String> items = new ArrayList<>();
        for (HorarioDTO h : horarios) {
            String hora = h.getHora() != null && h.getHora().length() >= 5
                    ? h.getHora().substring(0, 5) : h.getHora();
            items.add(h.getFecha() + "  —  " + hora
                    + "  —  " + h.getCuposRestantes() + " cupos");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                items);
        lvHorarios.setAdapter(adapter);

        tvStatus.setVisibility(View.GONE);
        lvHorarios.setVisibility(View.VISIBLE);
    }

    private void confirmarReserva() {
        if (horarioSeleccionado == null) {
            Toast.makeText(requireContext(),
                    "Seleccioná un horario primero", Toast.LENGTH_SHORT).show();
            return;
        }

        String cantidadStr = etCantidad.getText().toString().trim();
        if (cantidadStr.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Ingresá la cantidad de participantes", Toast.LENGTH_SHORT).show();
            return;
        }

        int cantidad = Integer.parseInt(cantidadStr);
        if (cantidad < 1) {
            Toast.makeText(requireContext(),
                    "La cantidad debe ser al menos 1", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cantidad > horarioSeleccionado.getCuposRestantes()) {
            Toast.makeText(requireContext(),
                    "No hay suficientes cupos disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        btnReservar.setEnabled(false);
        btnReservar.setText("Reservando...");

        CrearReservaRequest request = new CrearReservaRequest(
                horarioSeleccionado.getId(), cantidad);

        reservaRepository.crearReserva(request).enqueue(new Callback<ReservaDetalleDTO>() {
            @Override
            public void onResponse(Call<ReservaDetalleDTO> call,
                                   Response<ReservaDetalleDTO> response) {
                if (getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    ReservaDetalleDTO detalle = response.body();
                    Bundle args = new Bundle();
                    args.putLong("reservaId", detalle.getId());
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_horarios_to_reservaDetalle, args);
                } else {
                    btnReservar.setEnabled(true);
                    btnReservar.setText("Confirmar reserva");
                    mostrarError("Error al reservar: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ReservaDetalleDTO> call, Throwable t) {
                if (getView() == null) return;
                Log.e(TAG, "onFailure", t);
                btnReservar.setEnabled(true);
                btnReservar.setText("Confirmar reserva");
                mostrarError("Error de red: " + t.getMessage());
            }
        });
    }

    private void mostrarError(String msg) {
        tvStatus.setText(msg);
        tvStatus.setVisibility(View.VISIBLE);
        lvHorarios.setVisibility(View.GONE);
    }
}
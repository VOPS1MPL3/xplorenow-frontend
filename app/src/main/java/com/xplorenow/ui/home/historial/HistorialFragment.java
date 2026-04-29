package com.xplorenow.ui.home.historial;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.xplorenow.R;
import com.xplorenow.data.dto.DestinoDTO;
import com.xplorenow.data.dto.ReservaDTO;
import com.xplorenow.data.repository.CatalogoRepository;
import com.xplorenow.data.repository.ReservaRepository;
import com.xplorenow.databinding.FragmentHistorialBinding;
import com.xplorenow.ui.home.misreservas.ReservaAdapter;
import com.xplorenow.ui.home.misreservas.detalle.ReservaDetalleFragment;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class HistorialFragment extends Fragment {

    private static final String TAG = "HistorialFragment";

    private FragmentHistorialBinding binding;
    private ReservaAdapter adapter;
    private final List<ReservaDTO> reservas = new ArrayList<>();
    private final List<DestinoDTO> destinos = new ArrayList<>();

    @Inject
    ReservaRepository reservaRepository;

    @Inject
    CatalogoRepository catalogoRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistorialBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ReservaAdapter(requireContext(), reservas);
        binding.lvHistorial.setAdapter(adapter);

        binding.lvHistorial.setOnItemClickListener((parent, v, position, id) -> {
            ReservaDTO seleccionada = reservas.get(position);
            Bundle args = new Bundle();
            args.putLong(ReservaDetalleFragment.ARG_RESERVA_ID, seleccionada.getId());
            Navigation.findNavController(v).navigate(
                    R.id.action_historial_to_reservaDetalle, args);
        });

        binding.tvFechaDesde.setOnClickListener(v -> mostrarDatePicker(binding.tvFechaDesde));
        binding.tvFechaHasta.setOnClickListener(v -> mostrarDatePicker(binding.tvFechaHasta));

        binding.btnAplicar.setOnClickListener(v -> cargar());
        binding.btnLimpiar.setOnClickListener(v -> {
            binding.spDestino.setSelection(0);
            binding.tvFechaDesde.setText("");
            binding.tvFechaHasta.setText("");
            cargar();
        });

        cargarDestinos();
        cargar();
    }

    private void cargarDestinos() {
        catalogoRepository.listarDestinos().enqueue(new Callback<List<DestinoDTO>>() {
            @Override
            public void onResponse(Call<List<DestinoDTO>> call,
                                   Response<List<DestinoDTO>> response) {
                if (binding == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    destinos.clear();
                    destinos.addAll(response.body());

                    List<String> labels = new ArrayList<>();
                    labels.add("Todos los destinos");
                    for (DestinoDTO d : destinos) labels.add(d.getNombre());

                    ArrayAdapter<String> ad = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item, labels);
                    ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spDestino.setAdapter(ad);
                }
            }

            @Override
            public void onFailure(Call<List<DestinoDTO>> call, Throwable t) {
                Log.e(TAG, "destinos onFailure", t);
            }
        });
    }

    private void cargar() {
        String desdeStr = binding.tvFechaDesde.getText().toString();
        String hastaStr = binding.tvFechaHasta.getText().toString();
        if (!TextUtils.isEmpty(desdeStr) && !TextUtils.isEmpty(hastaStr)) {
            if (hastaStr.compareTo(desdeStr) < 0) {
                error("La fecha 'hasta' no puede ser anterior a 'desde'");
                return;
            }
        }

        binding.tvStatus.setText("Cargando...");
        binding.tvStatus.setVisibility(View.VISIBLE);
        binding.lvHistorial.setVisibility(View.GONE);

        Long destinoId = null;
        int pos = binding.spDestino.getSelectedItemPosition();
        if (pos > 0 && pos - 1 < destinos.size()) {
            destinoId = destinos.get(pos - 1).getId();
        }

        String desde = binding.tvFechaDesde.getText().toString();
        String hasta = binding.tvFechaHasta.getText().toString();
        if (TextUtils.isEmpty(desde)) desde = null;
        if (TextUtils.isEmpty(hasta)) hasta = null;

        reservaRepository.historial(destinoId, desde, hasta).enqueue(
                new Callback<List<ReservaDTO>>() {
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
            error("No hay reservas finalizadas en el historial");
            return;
        }
        reservas.clear();
        reservas.addAll(recibidas);
        adapter.notifyDataSetChanged();
        binding.tvStatus.setVisibility(View.GONE);
        binding.lvHistorial.setVisibility(View.VISIBLE);
    }

    private void error(String msg) {
        binding.tvStatus.setText(msg);
        binding.tvStatus.setVisibility(View.VISIBLE);
        binding.lvHistorial.setVisibility(View.GONE);
    }

    private void mostrarDatePicker(TextView target) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String fecha = String.format(Locale.US,
                            "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    target.setText(fecha);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        if (target.getId() == binding.tvFechaHasta.getId()) {
            String desde = binding.tvFechaDesde.getText().toString();
            if (!TextUtils.isEmpty(desde)) {
                Long ts = parseFechaATimestamp(desde);
                if (ts != null) dialog.getDatePicker().setMinDate(ts);
            }
        }

        dialog.show();
    }

    private Long parseFechaATimestamp(String fecha) {
        try {
            String[] partes = fecha.split("-");
            if (partes.length != 3) return null;
            Calendar c = Calendar.getInstance();
            c.set(
                    Integer.parseInt(partes[0]),
                    Integer.parseInt(partes[1]) - 1,
                    Integer.parseInt(partes[2]),
                    0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c.getTimeInMillis();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
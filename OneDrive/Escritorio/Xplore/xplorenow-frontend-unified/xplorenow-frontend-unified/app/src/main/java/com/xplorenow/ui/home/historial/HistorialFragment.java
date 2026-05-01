package com.xplorenow.ui.home.historial;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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

    private Spinner spDestino;
    private TextView tvFechaDesde, tvFechaHasta, tvStatus;
    private Button btnLimpiar, btnAplicar;
    private ListView lvHistorial;

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
        return inflater.inflate(R.layout.fragment_historial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spDestino = view.findViewById(R.id.spDestino);
        tvFechaDesde = view.findViewById(R.id.tvFechaDesde);
        tvFechaHasta = view.findViewById(R.id.tvFechaHasta);
        tvStatus = view.findViewById(R.id.tvStatus);
        btnLimpiar = view.findViewById(R.id.btnLimpiar);
        btnAplicar = view.findViewById(R.id.btnAplicar);
        lvHistorial = view.findViewById(R.id.lvHistorial);

        adapter = new ReservaAdapter(requireContext(), reservas);
        lvHistorial.setAdapter(adapter);

        lvHistorial.setOnItemClickListener((parent, v, position, id) -> {
            ReservaDTO seleccionada = reservas.get(position);
            Bundle args = new Bundle();
            args.putLong(ReservaDetalleFragment.ARG_RESERVA_ID, seleccionada.getId());
            Navigation.findNavController(v).navigate(
                    R.id.action_historial_to_reservaDetalle, args);
        });

        tvFechaDesde.setOnClickListener(v -> mostrarDatePicker(tvFechaDesde));
        tvFechaHasta.setOnClickListener(v -> mostrarDatePicker(tvFechaHasta));

        btnAplicar.setOnClickListener(v -> cargar());
        btnLimpiar.setOnClickListener(v -> {
            spDestino.setSelection(0);
            tvFechaDesde.setText("");
            tvFechaHasta.setText("");
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
                if (getView() == null) return;
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
                    spDestino.setAdapter(ad);
                }
            }

            @Override
            public void onFailure(Call<List<DestinoDTO>> call, Throwable t) {
                Log.e(TAG, "destinos onFailure", t);
            }
        });
    }

    private void cargar() {
        String desdeStr = tvFechaDesde.getText().toString();
        String hastaStr = tvFechaHasta.getText().toString();
        if (!TextUtils.isEmpty(desdeStr) && !TextUtils.isEmpty(hastaStr)) {
            if (hastaStr.compareTo(desdeStr) < 0) {
                error("La fecha 'hasta' no puede ser anterior a 'desde'");
                return;
            }
        }

        tvStatus.setText("Cargando...");
        tvStatus.setVisibility(View.VISIBLE);
        lvHistorial.setVisibility(View.GONE);

        Long destinoId = null;
        int pos = spDestino.getSelectedItemPosition();
        if (pos > 0 && pos - 1 < destinos.size()) {
            destinoId = destinos.get(pos - 1).getId();
        }

        if (TextUtils.isEmpty(desdeStr)) desdeStr = null;
        if (TextUtils.isEmpty(hastaStr)) hastaStr = null;

        reservaRepository.historial(destinoId, desdeStr, hastaStr).enqueue(
                new Callback<List<ReservaDTO>>() {
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
            error("No hay reservas finalizadas en el historial");
            return;
        }
        reservas.clear();
        reservas.addAll(recibidas);
        adapter.notifyDataSetChanged();
        tvStatus.setVisibility(View.GONE);
        lvHistorial.setVisibility(View.VISIBLE);
    }

    private void error(String msg) {
        tvStatus.setText(msg);
        tvStatus.setVisibility(View.VISIBLE);
        lvHistorial.setVisibility(View.GONE);
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

        if (target.getId() == tvFechaHasta.getId()) {
            String desde = tvFechaDesde.getText().toString();
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
            c.set(Integer.parseInt(partes[0]),
                    Integer.parseInt(partes[1]) - 1,
                    Integer.parseInt(partes[2]),
                    0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c.getTimeInMillis();
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
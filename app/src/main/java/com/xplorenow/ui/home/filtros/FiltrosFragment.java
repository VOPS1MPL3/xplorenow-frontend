package com.xplorenow.ui.home.filtros;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.xplorenow.R;
import com.xplorenow.data.dto.CategoriaDTO;
import com.xplorenow.data.dto.DestinoDTO;
import com.xplorenow.data.repository.CatalogoRepository;
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
public class FiltrosFragment extends Fragment {

    private static final String TAG = "FiltrosFragment";

    public static final String RESULT_KEY = "filtros_result";
    public static final String ARG_DESTINO_ID = "destinoId";
    public static final String ARG_CATEGORIA_ID = "categoriaId";
    public static final String ARG_FECHA_DESDE = "fechaDesde";
    public static final String ARG_FECHA_HASTA = "fechaHasta";
    public static final String ARG_PRECIO_MIN = "precioMin";
    public static final String ARG_PRECIO_MAX = "precioMax";

    private Spinner spDestino, spCategoria;
    private TextView tvFechaDesde, tvFechaHasta;
    private EditText etPrecioMin, etPrecioMax;
    private Button btnLimpiar, btnAplicar;

    @Inject
    CatalogoRepository catalogoRepository;

    private final List<DestinoDTO> destinos = new ArrayList<>();
    private final List<CategoriaDTO> categorias = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filtros, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spDestino = view.findViewById(R.id.spDestino);
        spCategoria = view.findViewById(R.id.spCategoria);
        tvFechaDesde = view.findViewById(R.id.tvFechaDesde);
        tvFechaHasta = view.findViewById(R.id.tvFechaHasta);
        etPrecioMin = view.findViewById(R.id.etPrecioMin);
        etPrecioMax = view.findViewById(R.id.etPrecioMax);
        btnLimpiar = view.findViewById(R.id.btnLimpiar);
        btnAplicar = view.findViewById(R.id.btnAplicar);

        cargarDestinos();
        cargarCategorias();

        tvFechaDesde.setOnClickListener(v -> mostrarDatePicker(tvFechaDesde));
        tvFechaHasta.setOnClickListener(v -> mostrarDatePicker(tvFechaHasta));

        btnLimpiar.setOnClickListener(v -> limpiar());
        btnAplicar.setOnClickListener(v -> aplicar());

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
                    ad.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    spDestino.setAdapter(ad);
                }
            }

            @Override
            public void onFailure(Call<List<DestinoDTO>> call, Throwable t) {
                Log.e(TAG, "destinos onFailure", t);
            }
        });
    }

    private void cargarCategorias() {
        catalogoRepository.listarCategorias().enqueue(new Callback<List<CategoriaDTO>>() {
            @Override
            public void onResponse(Call<List<CategoriaDTO>> call,
                                   Response<List<CategoriaDTO>> response) {
                if (getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    categorias.clear();
                    categorias.addAll(response.body());

                    List<String> labels = new ArrayList<>();
                    labels.add("Todas las categorias");
                    for (CategoriaDTO c : categorias) labels.add(c.getNombre());

                    ArrayAdapter<String> ad = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item, labels);
                    ad.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    spCategoria.setAdapter(ad);
                }
            }

            @Override
            public void onFailure(Call<List<CategoriaDTO>> call, Throwable t) {
                Log.e(TAG, "categorias onFailure", t);
            }
        });
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

        long limiteMin = System.currentTimeMillis();

        if (target.getId() == tvFechaHasta.getId()) {
            String desde = tvFechaDesde.getText().toString();
            if (!TextUtils.isEmpty(desde)) {
                Long timestamp = parseFechaATimestamp(desde);
                if (timestamp != null) limiteMin = timestamp;
            }
        }
        dialog.getDatePicker().setMinDate(limiteMin);
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

    private void limpiar() {
        Bundle result = new Bundle();
        getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
        Navigation.findNavController(requireView()).popBackStack();
    }

    private void aplicar() {
        String desde = tvFechaDesde.getText().toString();
        String hasta = tvFechaHasta.getText().toString();
        if (!TextUtils.isEmpty(desde) && !TextUtils.isEmpty(hasta)) {
            if (hasta.compareTo(desde) < 0) {
                Toast.makeText(requireContext(),
                        "La fecha 'hasta' no puede ser anterior a 'desde'",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Bundle result = new Bundle();

        int posDestino = spDestino.getSelectedItemPosition();
        if (posDestino > 0 && posDestino - 1 < destinos.size()) {
            result.putLong(ARG_DESTINO_ID, destinos.get(posDestino - 1).getId());
        }

        int posCategoria = spCategoria.getSelectedItemPosition();
        if (posCategoria > 0 && posCategoria - 1 < categorias.size()) {
            result.putLong(ARG_CATEGORIA_ID, categorias.get(posCategoria - 1).getId());
        }

        if (!TextUtils.isEmpty(desde)) result.putString(ARG_FECHA_DESDE, desde);
        if (!TextUtils.isEmpty(hasta)) result.putString(ARG_FECHA_HASTA, hasta);

        try {
            String pmin = etPrecioMin.getText().toString();
            if (!TextUtils.isEmpty(pmin)) {
                result.putDouble(ARG_PRECIO_MIN, Double.parseDouble(pmin));
            }
            String pmax = etPrecioMax.getText().toString();
            if (!TextUtils.isEmpty(pmax)) {
                result.putDouble(ARG_PRECIO_MAX, Double.parseDouble(pmax));
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(),
                    "Precio invalido", Toast.LENGTH_SHORT).show();
            return;
        }

        getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
        Navigation.findNavController(requireView()).popBackStack();
    }
}
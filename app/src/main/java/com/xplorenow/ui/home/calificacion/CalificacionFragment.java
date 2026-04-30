package com.xplorenow.ui.home.calificacion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.xplorenow.R;
import com.xplorenow.data.api.XploreNowApi;
import com.xplorenow.data.dto.CalificacionDTO;
import com.xplorenow.data.dto.CalificacionRequest;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class CalificacionFragment extends Fragment {

    public static final String ARG_RESERVA_ID       = "reservaId";
    public static final String ARG_ACTIVIDAD_NOMBRE = "actividadNombre";

    @Inject
    XploreNowApi api;

    private TextView tvTitulo;
    private RatingBar rbActividad;
    private RatingBar rbGuia;
    private EditText etComentario;
    private TextView tvContadorChars;
    private Button btnEnviar;
    private Button btnVolver;

    private long reservaId = -1L;
    private static final int MAX_CHARS = 300;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calificacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitulo        = view.findViewById(R.id.tvTitulo);
        rbActividad     = view.findViewById(R.id.rbActividad);
        rbGuia          = view.findViewById(R.id.rbGuia);
        etComentario    = view.findViewById(R.id.etComentario);
        tvContadorChars = view.findViewById(R.id.tvContadorChars);
        btnEnviar       = view.findViewById(R.id.btnEnviar);
        btnVolver       = view.findViewById(R.id.btnVolver);

        if (getArguments() != null) {
            reservaId = getArguments().getLong(ARG_RESERVA_ID, -1L);
            String nombre = getArguments().getString(ARG_ACTIVIDAD_NOMBRE, "");
            tvTitulo.setText("¿Cómo fue " + nombre + "?");
        }

        // Contador de caracteres en tiempo real
        etComentario.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int restantes = MAX_CHARS - s.length();
                tvContadorChars.setText(restantes + " caracteres restantes");
                if (restantes < 0) {
                    // Truncar si supera el límite
                    etComentario.setText(s.subSequence(0, MAX_CHARS));
                    etComentario.setSelection(MAX_CHARS);
                }
            }
        });

        btnEnviar.setOnClickListener(v -> enviarCalificacion(view));

        btnVolver.setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );
    }

    private void enviarCalificacion(View view) {
        int ratingActividad = (int) rbActividad.getRating();
        int ratingGuia      = (int) rbGuia.getRating();
        String comentario   = etComentario.getText().toString().trim();

        if (ratingActividad == 0) {
            Toast.makeText(requireContext(),
                    "Calificá la actividad con al menos 1 estrella",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (ratingGuia == 0) {
            Toast.makeText(requireContext(),
                    "Calificá al guía con al menos 1 estrella",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnviar.setEnabled(false);
        btnEnviar.setText("Enviando...");

        CalificacionRequest request = new CalificacionRequest(
                ratingActividad,
                ratingGuia,
                comentario.isEmpty() ? null : comentario
        );

        api.calificarReserva(reservaId, request)
                .enqueue(new Callback<CalificacionDTO>() {
                    @Override
                    public void onResponse(Call<CalificacionDTO> call,
                                           Response<CalificacionDTO> response) {
                        if (getView() == null) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "¡Calificación enviada! Gracias por tu opinión",
                                    Toast.LENGTH_LONG).show();
                            Navigation.findNavController(view).popBackStack();
                        } else if (response.code() == 409) {
                            Toast.makeText(requireContext(),
                                    "Ya calificaste esta actividad",
                                    Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(view).popBackStack();
                        } else if (response.code() == 403) {
                            Toast.makeText(requireContext(),
                                    "El plazo de 48hs para calificar ya venció",
                                    Toast.LENGTH_LONG).show();
                            Navigation.findNavController(view).popBackStack();
                        } else {
                            btnEnviar.setEnabled(true);
                            btnEnviar.setText("Enviar calificación");
                            Toast.makeText(requireContext(),
                                    "Error al enviar (HTTP " + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CalificacionDTO> call, Throwable t) {
                        if (getView() == null) return;
                        btnEnviar.setEnabled(true);
                        btnEnviar.setText("Enviar calificación");
                        Toast.makeText(requireContext(),
                                "Error de conexión",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

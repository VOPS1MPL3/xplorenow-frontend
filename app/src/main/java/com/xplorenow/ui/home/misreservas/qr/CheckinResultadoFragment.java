package com.xplorenow.ui.home.misreservas.qr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.xplorenow.R;
import androidx.fragment.app.FragmentResultListener;

public class CheckinResultadoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkin_resultado, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvIcono  = view.findViewById(R.id.tvIcono);
        TextView tvTitulo = view.findViewById(R.id.tvTitulo);
        TextView tvMensaje = view.findViewById(R.id.tvMensaje);
        Button btnVolver  = view.findViewById(R.id.btnVolver);

        boolean exitoso = requireArguments().getBoolean("exitoso", false);

        if (exitoso) {
            view.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"));
            tvIcono.setText("✅");
            tvTitulo.setText("¡Asistencia confirmada!");
            tvTitulo.setTextColor(android.graphics.Color.parseColor("#1B5E20"));
            tvMensaje.setText("Tu check-in fue registrado correctamente. ¡Disfrutá la actividad!");
            tvMensaje.setTextColor(android.graphics.Color.parseColor("#2E7D32"));

            Bundle resultado = new Bundle();
            resultado.putBoolean("checkinExitoso", true);
            getParentFragmentManager().setFragmentResult("checkin_result", resultado);
        
        } else {
            view.setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"));
            tvIcono.setText("❌");
            tvTitulo.setText("QR inválido");
            tvTitulo.setTextColor(android.graphics.Color.parseColor("#B71C1C"));
            tvMensaje.setText("El código QR escaneado no corresponde a esta reserva. Verificá que estés escaneando el QR correcto.");
            tvMensaje.setTextColor(android.graphics.Color.parseColor("#C62828"));
        }
        
        btnVolver.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack(
                        R.id.reservaDetalleFragment, false));
    }
}
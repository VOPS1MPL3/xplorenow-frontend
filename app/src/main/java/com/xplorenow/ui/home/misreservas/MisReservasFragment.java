package com.xplorenow.ui.home.misreservas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.xplorenow.R;
import com.xplorenow.util.NetworkObserver;
import com.xplorenow.util.TokenManager;
import com.xplorenow.data.dto.EstadoReserva;
import com.xplorenow.data.dto.ReservaDTO;
import com.xplorenow.data.repository.ReservaRepository;
import com.xplorenow.ui.home.misreservas.detalle.ReservaDetalleFragment;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MisReservasFragment extends Fragment {

    private static final String TAG = "MisReservasFragment";

    private TextView tvStatus;
    private ListView lvReservas;

    private ReservaAdapter adapter;
    private final List<ReservaDTO> reservas = new ArrayList<>();
    private boolean isOnline = true;

    @Inject
    ReservaRepository reservaRepository;
    @Inject
    TokenManager tokenManager;

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

        if (!tokenManager.isTokenValid()) {
            tokenManager.clearToken();
            Navigation.findNavController(view).navigate(R.id.loginFragment);
            return;
        }

        tvStatus = view.findViewById(R.id.tvStatus);
        lvReservas = view.findViewById(R.id.lvReservas);

        adapter = new ReservaAdapter(requireContext(), reservas);
        lvReservas.setAdapter(adapter);

        new NetworkObserver(requireContext()).observe(getViewLifecycleOwner(), connected -> {
            boolean changed = (isOnline != connected);
            isOnline = connected;
            if (changed) {
                cargarReservas();
            }
        });

        lvReservas.setOnItemClickListener((parent, v, position, id) -> {
            ReservaDTO seleccionada = reservas.get(position);
            Bundle args = new Bundle();
            args.putLong(ReservaDetalleFragment.ARG_RESERVA_ID, seleccionada.getId());
            Navigation.findNavController(v).navigate(
                    R.id.action_misReservas_to_reservaDetalle, args);
        });

        cargarReservas();
    }

    private void cargarReservas() {
        tvStatus.setText("Cargando...");
        tvStatus.setVisibility(View.VISIBLE);
        lvReservas.setVisibility(View.GONE);

        reservas.clear();

        if (!isOnline) {
            reservaRepository.obtenerReservasOffline(results -> {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    reservas.addAll(results);
                    mostrar();
                });
            });
            return;
        }

        reservaRepository.misReservas(EstadoReserva.CONFIRMADA).enqueue(
                new Callback<List<ReservaDTO>>() {
                    @Override
                    public void onResponse(Call<List<ReservaDTO>> call,
                                           Response<List<ReservaDTO>> response) {
                        if (getView() == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            List<ReservaDTO> body = response.body();
                            reservas.addAll(body);
                            reservaRepository.guardarReservasLocal(body);
                        }
                        cargarCanceladas();
                    }
                    @Override
                    public void onFailure(Call<List<ReservaDTO>> call, Throwable t) {
                        if (getView() == null) return;
                        Log.e(TAG, "confirmadas onFailure", t);
                        cargarCanceladas();
                    }
                });
    }

    private void cargarCanceladas() {
        reservaRepository.misReservas(EstadoReserva.CANCELADA).enqueue(
                new Callback<List<ReservaDTO>>() {
                    @Override
                    public void onResponse(Call<List<ReservaDTO>> call,
                                           Response<List<ReservaDTO>> response) {
                        if (getView() == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            List<ReservaDTO> body = response.body();
                            reservas.addAll(body);
                            reservaRepository.guardarReservasLocal(body);
                        }
                        mostrar();
                    }
                    @Override
                    public void onFailure(Call<List<ReservaDTO>> call, Throwable t) {
                        if (getView() == null) return;
                        Log.e(TAG, "canceladas onFailure", t);
                        mostrar();
                    }
                });
    }

    private void mostrar() {
        if (reservas.isEmpty()) {
            error("No tenés reservas activas");
            return;
        }
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

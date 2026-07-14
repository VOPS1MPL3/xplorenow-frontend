package com.xplorenow.data.repository;
import android.util.Log;
import com.xplorenow.data.api.XploreNowApi;
import com.xplorenow.data.dto.EstadoReserva;
import com.xplorenow.data.dto.ReservaDTO;
import com.xplorenow.data.dto.ReservaDetalleDTO;
import com.xplorenow.data.dto.CrearReservaRequest;
import com.xplorenow.data.local.dao.ReservaDao;
import com.xplorenow.data.local.dao.SyncActionDao;
import com.xplorenow.data.local.entity.ReservaEntity;
import com.xplorenow.data.local.entity.SyncActionEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import androidx.annotation.Nullable;
import retrofit2.Call;
import retrofit2.Response;

@Singleton
public class ReservaRepository {
    private static final String TAG = "ReservaRepository";
    private static final String TYPE_CANCELAR = "CANCELAR_RESERVA";

    private final XploreNowApi api;
    private final ReservaDao reservaDao;
    private final SyncActionDao syncActionDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final java.util.concurrent.atomic.AtomicBoolean isSyncing =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    @Inject
    public ReservaRepository(XploreNowApi api, ReservaDao reservaDao, SyncActionDao syncActionDao) {
        this.api = api;
        this.reservaDao = reservaDao;
        this.syncActionDao = syncActionDao;
    }

    public Call<List<ReservaDTO>> misReservas(EstadoReserva estado) {
        String filtro = (estado == null) ? null : estado.name();
        return api.misReservas(filtro);
    }

    public void guardarReservaLocal(ReservaDetalleDTO dto) {
        if (dto == null) return;
        executor.execute(() -> guardarReservaLocalSync(dto));
    }

    private void guardarReservaLocalSync(ReservaDetalleDTO dto) {
        reservaDao.insertarReserva(mappingDtoToEntity(dto));
    }

    public void guardarReservasLocal(List<ReservaDTO> dtos) {
        if (dtos == null) return;
        executor.execute(() -> guardarReservasLocalSync(dtos));
    }

    private void guardarReservasLocalSync(List<ReservaDTO> dtos) {
        List<ReservaEntity> entities = new ArrayList<>();
        for (ReservaDTO dto : dtos) {
            ReservaEntity existente = reservaDao.obtenerReservaPorId(dto.getId());
            ReservaEntity entity;
            if (existente != null) {
                entity = existente;
                entity.setEstado(dto.getEstado() != null ? dto.getEstado().name() : entity.getEstado());
                entity.setFecha(dto.getFecha());
                entity.setHora(dto.getHora());
                if (dto.getVoucherCodigo() != null) {
                    entity.setVoucherCodigo(dto.getVoucherCodigo());
                }
                if (dto.getActividadNombre() != null) {
                    entity.setActividadNombre(dto.getActividadNombre());
                }
                if (dto.getActividadImagen() != null) {
                    entity.setActividadImagen(dto.getActividadImagen());
                }
                if (dto.getDestino() != null) {
                    entity.setDestino(dto.getDestino());
                }
                if (dto.getCantidadParticipantes() != null) {
                    entity.setCantidadParticipantes(dto.getCantidadParticipantes());
                }
            } else {
                entity = new ReservaEntity(
                        dto.getId(),
                        dto.getVoucherCodigo(),
                        dto.getEstado() != null ? dto.getEstado().name() : "CONFIRMADA",
                        dto.getCantidadParticipantes() != null ? dto.getCantidadParticipantes() : 1,
                        dto.getActividadNombre(),
                        dto.getActividadImagen(),
                        dto.getDestino(),
                        "",
                        "",
                        0.0, 0.0,
                        "",
                        dto.getFecha(),
                        dto.getHora(),
                        "",
                        ""
                );
            }
            entities.add(entity);
        }
        reservaDao.insertarReservas(entities);
    }

    private ReservaEntity mappingDtoToEntity(ReservaDetalleDTO dto) {
        return new ReservaEntity(
                dto.getId(),
                dto.getVoucherCodigo(),
                dto.getEstado() != null ? dto.getEstado().name() : "CONFIRMADA",
                dto.getCantidadParticipantes() != null ? dto.getCantidadParticipantes() : 1,
                dto.getActividadNombre(),
                dto.getActividadImagen(),
                dto.getDestino(),
                dto.getCategoria(),
                dto.getPuntoEncuentro(),
                dto.getLatitud() != null ? dto.getLatitud() : 0.0,
                dto.getLongitud() != null ? dto.getLongitud() : 0.0,
                dto.getPoliticaCancelacion(),
                dto.getFecha(),
                dto.getHora(),
                dto.getGuiaAsignado(),
                dto.getIdioma()
        );
    }

    public void obtenerReservasOffline(OnOfflineResult<List<ReservaDTO>> callback) {
        executor.execute(() -> {
            List<ReservaEntity> entities = reservaDao.obtenerReservasOffline();
            List<ReservaDTO> dtos = new ArrayList<>();
            for (ReservaEntity e : entities) {
                dtos.add(mappingEntityToSimpleDto(e));
            }
            callback.onResult(dtos);
        });
    }

    private ReservaDTO mappingEntityToSimpleDto(ReservaEntity e) {
        ReservaDTO dto = new ReservaDTO();
        dto.setId(e.getId());
        dto.setVoucherCodigo(e.getVoucherCodigo());
        try {
            dto.setEstado(EstadoReserva.valueOf(e.getEstado()));
        } catch (Exception ex) {
            dto.setEstado(EstadoReserva.CONFIRMADA);
        }
        dto.setCantidadParticipantes(e.getCantidadParticipantes());
        dto.setActividadNombre(e.getActividadNombre());
        dto.setActividadImagen(e.getActividadImagen());
        dto.setDestino(e.getDestino());
        dto.setFecha(e.getFecha());
        dto.setHora(e.getHora());
        return dto;
    }

    public androidx.lifecycle.LiveData<ReservaDetalleDTO> observarReserva(long id) {
        return androidx.lifecycle.Transformations.map(reservaDao.observarReservaPorId(id), entity -> {
            if (entity != null) return mappingEntityToDto(entity);
            return null;
        });
    }

    public Call<ReservaDetalleDTO> obtenerReserva(long id) {
        return api.obtenerReserva(id);
    }

    public void obtenerReservaOffline(long id, OnOfflineResult<ReservaDetalleDTO> callback) {
        executor.execute(() -> {
            ReservaEntity e = reservaDao.obtenerReservaPorId(id);
            if (e != null) {
                callback.onResult(mappingEntityToDto(e));
            } else {
                callback.onResult(null);
            }
        });
    }

    public interface OnOfflineResult<T> {
        void onResult(T result);
    }

    private ReservaDetalleDTO mappingEntityToDto(ReservaEntity e) {
        ReservaDetalleDTO dto = new ReservaDetalleDTO();
        dto.setId(e.getId());
        dto.setVoucherCodigo(e.getVoucherCodigo());
        try {
            dto.setEstado(EstadoReserva.valueOf(e.getEstado()));
        } catch (Exception ex) {
            dto.setEstado(EstadoReserva.CONFIRMADA);
        }
        dto.setCantidadParticipantes(e.getCantidadParticipantes());
        dto.setActividadNombre(e.getActividadNombre());
        dto.setActividadImagen(e.getActividadImagen());
        dto.setDestino(e.getDestino());
        dto.setCategoria(e.getCategoria());
        dto.setPuntoEncuentro(e.getPuntoEncuentro());
        dto.setLatitud(e.getLatitud());
        dto.setLongitud(e.getLongitud());
        dto.setPoliticaCancelacion(e.getPoliticaCancelacion());
        dto.setFecha(e.getFecha());
        dto.setHora(e.getHora());
        dto.setGuiaAsignado(e.getGuiaAsignado());
        dto.setIdioma(e.getIdioma());
        return dto;
    }

    public Call<ReservaDetalleDTO> cancelarReserva(long id) {
        return api.cancelarReserva(id);
    }

    public void encolarCancelacion(long id) {
        executor.execute(() -> {
            if (syncActionDao.countByTypeAndTarget(TYPE_CANCELAR, id) == 0) {
                syncActionDao.insert(new SyncActionEntity(TYPE_CANCELAR, id));
            }

            ReservaEntity entity = reservaDao.obtenerReservaPorId(id);
            if (entity != null) {
                entity.setEstado(EstadoReserva.CANCELADA.name());
                reservaDao.insertarReserva(entity);
            }
        });
    }

    /**
     * Al recuperar red: reintenta cancelaciones pendientes y refresca el
     * cache local (listado + detalle completo) para cumplir el sync offline.
     */
    public void sincronizarAlReconectar() {
        sincronizarAlReconectar(null);
    }

    public void sincronizarAlReconectar(@Nullable final SyncResultListener listener) {
        if (!isSyncing.compareAndSet(false, true)) return;

        new Thread(() -> {
            try {
                SyncCancelResult cancelResult = sincronizarCancelacionesPendientesSync();
                refrescarCacheReservasSync();
                if (listener != null && cancelResult.rechazadas > 0) {
                    listener.onCancelacionesRechazadas(
                            cancelResult.rechazadas, cancelResult.nombresRechazados);
                }
            } finally {
                isSyncing.set(false);
            }
        }).start();
    }

    public void sincronizarAccionesPendientes() {
        sincronizarAlReconectar();
    }

    public interface SyncResultListener {
        void onCancelacionesRechazadas(int cantidad, List<String> nombresActividades);
    }

    private static final class SyncCancelResult {
        int rechazadas;
        final List<String> nombresRechazados = new ArrayList<>();
    }

    private SyncCancelResult sincronizarCancelacionesPendientesSync() {
        SyncCancelResult result = new SyncCancelResult();
        List<SyncActionEntity> pending = syncActionDao.getAllPending();
        for (SyncActionEntity action : pending) {
            if (!TYPE_CANCELAR.equals(action.getType())) continue;

            try {
                Response<ReservaDetalleDTO> response =
                        api.cancelarReserva(action.getTargetId()).execute();

                if (response.isSuccessful()) {
                    syncActionDao.delete(action);
                    if (response.body() != null) {
                        guardarReservaLocalSync(response.body());
                    } else {
                        marcarEstadoLocal(action.getTargetId(), EstadoReserva.CANCELADA);
                    }
                } else if (esCancelacionYaAplicada(response.code())) {
                    // Ya estaba cancelada / no existe: limpia cola y deja CANCELADA
                    syncActionDao.delete(action);
                    marcarEstadoLocal(action.getTargetId(), EstadoReserva.CANCELADA);
                } else if (response.code() >= 400 && response.code() < 500) {
                    // Rechazo definitivo del servidor: rollback optimista
                    syncActionDao.delete(action);
                    String nombre = nombreActividadLocal(action.getTargetId());
                    marcarEstadoLocal(action.getTargetId(), EstadoReserva.CONFIRMADA);
                    result.rechazadas++;
                    result.nombresRechazados.add(nombre);
                    Log.w(TAG, "Cancelación rechazada (" + response.code()
                            + ") para reserva " + action.getTargetId() + "; se revirtió localmente");
                }
                // 5xx u otros: se deja en cola para reintentar
            } catch (Exception e) {
                Log.e(TAG, "Error sincronizando cancelación " + action.getTargetId(), e);
            }
        }
        return result;
    }

    private boolean esCancelacionYaAplicada(int code) {
        return code == 404 || code == 409 || code == 410;
    }

    private String nombreActividadLocal(long id) {
        ReservaEntity entity = reservaDao.obtenerReservaPorId(id);
        if (entity != null && entity.getActividadNombre() != null
                && !entity.getActividadNombre().trim().isEmpty()) {
            return entity.getActividadNombre();
        }
        return "una reserva";
    }

    private void marcarEstadoLocal(long id, EstadoReserva estado) {
        ReservaEntity entity = reservaDao.obtenerReservaPorId(id);
        if (entity != null) {
            entity.setEstado(estado.name());
            reservaDao.insertarReserva(entity);
        }
    }

    private void refrescarCacheReservasSync() {
        try {
            Response<List<ReservaDTO>> confirmadas =
                    api.misReservas(EstadoReserva.CONFIRMADA.name()).execute();
            if (confirmadas.isSuccessful() && confirmadas.body() != null) {
                guardarReservasLocalSync(confirmadas.body());
                for (ReservaDTO dto : confirmadas.body()) {
                    refrescarDetalleSync(dto.getId());
                }
            }

            Response<List<ReservaDTO>> canceladas =
                    api.misReservas(EstadoReserva.CANCELADA.name()).execute();
            if (canceladas.isSuccessful() && canceladas.body() != null) {
                guardarReservasLocalSync(canceladas.body());
                for (ReservaDTO dto : canceladas.body()) {
                    refrescarDetalleSync(dto.getId());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error refrescando cache de reservas", e);
        }
    }

    private void refrescarDetalleSync(long id) {
        try {
            Response<ReservaDetalleDTO> response = api.obtenerReserva(id).execute();
            if (response.isSuccessful() && response.body() != null) {
                guardarReservaLocalSync(response.body());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error refrescando detalle de reserva " + id, e);
        }
    }

    public Call<List<ReservaDTO>> historial(Long destinoId, String fechaDesde, String fechaHasta) {
        return api.historial(destinoId, fechaDesde, fechaHasta);
    }

    public Call<ReservaDetalleDTO> crearReserva(CrearReservaRequest request) {
        return api.crearReserva(request);
    }
}

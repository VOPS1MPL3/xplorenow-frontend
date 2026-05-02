package com.xplorenow.data.repository;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ReservaRepository {
    private final XploreNowApi api;
    private final ReservaDao reservaDao;
    private final SyncActionDao syncActionDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final java.util.concurrent.atomic.AtomicBoolean isSyncing = new java.util.concurrent.atomic.AtomicBoolean(false);

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
        executor.execute(() -> {
            ReservaEntity entity = mappingDtoToEntity(dto);
            reservaDao.insertarReserva(entity);
        });
    }

    public void guardarReservasLocal(List<ReservaDTO> dtos) {
        executor.execute(() -> {
            List<ReservaEntity> entities = new ArrayList<>();
            for (ReservaDTO dto : dtos) {
                ReservaEntity existente = reservaDao.obtenerReservaPorId(dto.getId());
                ReservaEntity entity;
                if (existente != null) {
                    entity = existente;
                    entity.setEstado(dto.getEstado() != null ? dto.getEstado().name() : entity.getEstado());
                    entity.setFecha(dto.getFecha());
                    entity.setHora(dto.getHora());
                    // guiaAsignado e idioma no vienen en el DTO simple, se conservan los existentes
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
                            "", // guiaAsignado no viene en DTO simple
                            ""  // idioma no viene en DTO simple
                    );
                }
                entities.add(entity);
            }
            reservaDao.insertarReservas(entities);
        });
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
                dto.getGuiaAsignado(),   // ahora se guarda
                dto.getIdioma()          // ahora se guarda
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
        dto.setGuiaAsignado(e.getGuiaAsignado());   // ahora se lee
        dto.setIdioma(e.getIdioma());               // ahora se lee
        return dto;
    }

    public Call<ReservaDetalleDTO> cancelarReserva(long id) {
        return api.cancelarReserva(id);
    }

    public void encolarCancelacion(long id) {
        executor.execute(() -> {
            SyncActionEntity action = new SyncActionEntity("CANCELAR_RESERVA", id);
            syncActionDao.insert(action);

            ReservaEntity entity = reservaDao.obtenerReservaPorId(id);
            if (entity != null) {
                entity.setEstado(EstadoReserva.CANCELADA.name());
                reservaDao.insertarReserva(entity);
            }
        });
    }

    public void sincronizarAccionesPendientes() {
        if (!isSyncing.compareAndSet(false, true)) return;

        new Thread(() -> {
            try {
                List<SyncActionEntity> pending = syncActionDao.getAllPending();
                for (SyncActionEntity action : pending) {
                    if ("CANCELAR_RESERVA".equals(action.getType())) {
                        try {
                            Response<ReservaDetalleDTO> response = api.cancelarReserva(action.getTargetId()).execute();
                            if (response.isSuccessful()) {
                                syncActionDao.delete(action);
                                executor.execute(() -> {
                                    ReservaEntity entity = reservaDao.obtenerReservaPorId(action.getTargetId());
                                    if (entity != null) {
                                        entity.setEstado(EstadoReserva.CANCELADA.name());
                                        reservaDao.insertarReserva(entity);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } finally {
                isSyncing.set(false);
            }
        }).start();
    }

    public Call<List<ReservaDTO>> historial(Long destinoId, String fechaDesde, String fechaHasta) {
        return api.historial(destinoId, fechaDesde, fechaHasta);
    }

    public Call<ReservaDetalleDTO> crearReserva(CrearReservaRequest request) {
        return api.crearReserva(request);
    }
}
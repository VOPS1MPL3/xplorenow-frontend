package com.xplorenow.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.xplorenow.data.local.entity.ReservaEntity;
import java.util.List;

@Dao
public interface ReservaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarReserva(ReservaEntity reserva);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarReservas(List<ReservaEntity> reservas);

    @Query("SELECT * FROM reservas WHERE estado = 'CONFIRMADA' OR estado = 'CANCELADA' ORDER BY fecha ASC")
    List<ReservaEntity> obtenerReservasOffline();

    @Query("SELECT * FROM reservas WHERE id = :id")
    ReservaEntity obtenerReservaPorId(long id);

    @Query("SELECT * FROM reservas WHERE id = :id")
    androidx.lifecycle.LiveData<ReservaEntity> observarReservaPorId(long id);

    @Query("DELETE FROM reservas")
    void eliminarTodas();
}

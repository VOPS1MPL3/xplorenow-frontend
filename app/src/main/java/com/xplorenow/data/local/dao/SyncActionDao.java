package com.xplorenow.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.xplorenow.data.local.entity.SyncActionEntity;
import java.util.List;

@Dao
public interface SyncActionDao {
    @Insert
    void insert(SyncActionEntity action);

    @Query("SELECT * FROM sync_actions ORDER BY timestamp ASC")
    List<SyncActionEntity> getAllPending();

    @Delete
    void delete(SyncActionEntity action);
}

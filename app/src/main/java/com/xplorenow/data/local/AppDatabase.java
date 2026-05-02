package com.xplorenow.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.xplorenow.data.local.dao.ReservaDao;
import com.xplorenow.data.local.dao.SyncActionDao;
import com.xplorenow.data.local.entity.ReservaEntity;
import com.xplorenow.data.local.entity.SyncActionEntity;

@Database(entities = {ReservaEntity.class, SyncActionEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ReservaDao reservaDao();
    public abstract SyncActionDao syncActionDao();
}
package com.xplorenow.di;

import android.content.Context;
import androidx.room.Room;
import com.xplorenow.data.local.AppDatabase;
import com.xplorenow.data.local.dao.ReservaDao;
import com.xplorenow.data.local.dao.SyncActionDao;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "xplorenow_db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    public ReservaDao provideReservaDao(AppDatabase database) {
        return database.reservaDao();
    }

    @Provides
    public SyncActionDao provideSyncActionDao(AppDatabase database) {
        return database.syncActionDao();
    }
}

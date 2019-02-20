package com.example.yaramobile.downloadmanagerexample.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {DownloadModel.class}, version = 1)
public abstract class DownloadDatabase extends RoomDatabase {

    private static DownloadDatabase INSTANCE;

    public static DownloadDatabase getDownloadDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(
                            context,
                            DownloadDatabase.class,
                            "download_db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public abstract DownloadDao downloadDao();
}

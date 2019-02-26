package com.example.yaramobile.downloadmanagerexample.database;

import android.app.Application;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

@Database(entities = {DownloadModel.class}, version = 1)
public abstract class DownloadDatabase extends RoomDatabase {

    private static DownloadDatabase INSTANCE;
    private static final String TAG = "DownloadDatabase";

    public static DownloadDatabase getDownloadDatabase(Application application) {

        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(
                            application,
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

package com.example.yaramobile.downloadmanagerexample.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface DownloadDao {

    @Insert(onConflict = REPLACE)
    void saveDownloadModel(DownloadModel downloadModel);

    @Insert(onConflict = REPLACE)
    void saveListDownloadModel(List<DownloadModel> downloadModelList);

    @Query("select * from DownloadModel where id = :id")
    LiveData<DownloadModel> getDownloadModel(int id);

    @Query("select * from DownloadModel where status = 'completed'")
    LiveData<List<DownloadModel>> getAllCompletedDownloadModel();

    @Query("select * from DownloadModel")
    List<DownloadModel> getAllDownloadModels();

}

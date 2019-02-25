package com.example.yaramobile.downloadmanagerexample.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface DownloadDao {

    @Insert(onConflict = REPLACE)
    void saveDownloadModel(DownloadModel downloadModel);

    @Insert(onConflict = REPLACE)
    void saveListDownloadModel(List<DownloadModel> downloadModelList);

    @Update
    void updateDownloadModel(DownloadModel downloadModel);

    @Query("select * from DownloadModel where id = :id")
    LiveData<DownloadModel> getDownloadModelLiveData(int id);

    @Query("select * from DownloadModel where url =:url and path =:path")
    DownloadModel getDownloadModelByUrlAndPath(String url, String path);

    @Query("select * from DownloadModel")
    LiveData<List<DownloadModel>> getListDownloadModel();

    @Query("select * from DownloadModel where id =:id")
    DownloadModel getDownloadModelById(int id);

    @Query("select * from DownloadModel where status = 'completed'")
    LiveData<List<DownloadModel>> getAllCompletedDownloadModel();

    @Query("select * from DownloadModel")
    List<DownloadModel> getAllDownloadModels();

    @Query("select * from DownloadModel where status = :status")
    List<DownloadModel> getDownloadModelListByStatus(int status);

}

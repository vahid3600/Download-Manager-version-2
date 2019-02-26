package com.example.yaramobile.downloadmanagerexample.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "downloadModel")
public class DownloadModel {
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    public Integer downloadManagerId;
    public String url;
    public String path;
    public String name;
    public Integer  status;
    public Integer  soFarBytes;
    public Integer  totalBytes;
    public String downloadError;

    public DownloadModel(Integer downloadManagerId, String url, String path, String name, Integer status, Integer soFarBytes, Integer totalBytes, String downloadError) {
        this.downloadManagerId = downloadManagerId;
        this.url = url;
        this.path = path;
        this.name = name;
        this.status = status;
        this.soFarBytes = soFarBytes;
        this.totalBytes = totalBytes;
        this.downloadError = downloadError;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setDownloadManagerId(Integer downloadManagerId) {
        this.downloadManagerId = downloadManagerId;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setSoFarBytes(Integer soFarBytes) {
        this.soFarBytes = soFarBytes;
    }

    public void setTotalBytes(Integer totalBytes) {
        this.totalBytes = totalBytes;
    }

    public void setDownloadError(String downloadError) {
        this.downloadError = downloadError;
    }
}

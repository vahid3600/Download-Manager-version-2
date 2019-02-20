package com.example.yaramobile.downloadmanagerexample.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "downloadModel")
public class DownloadModel {

    @PrimaryKey()
    private int id;
    private String url;
    private String path;
    private String name;
    private String status;
    private int soFarBytes;
    private int totalBytes;
    private int downloadSpeed;
    private String downloadError;

    public DownloadModel(int id,
                         String url,
                         String path,
                         String name,
                         String status,
                         int soFarBytes,
                         int totalBytes,
                         int downloadSpeed,
                         String downloadError) {
        this.id = id;
        this.url = url;
        this.path = path;
        this.name = name;
        this.status = status;
        this.soFarBytes = soFarBytes;
        this.totalBytes = totalBytes;
        this.downloadSpeed = downloadSpeed;
        this.downloadError = downloadError;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public int getSoFarBytes() {
        return soFarBytes;
    }

    public int getTotalBytes() {
        return totalBytes;
    }

    public int getDownloadSpeed() {
        return downloadSpeed;
    }

    public String getUrl() {
        return url;
    }

    public String getPath() {
        return path;
    }

    public String getDownloadError() {
        return downloadError;
    }

    public void setDownloadError(String downloadError) {
        this.downloadError = downloadError;
    }

    public void setId(int id) {
        this.id = id;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSoFarBytes(int soFarBytes) {
        this.soFarBytes = soFarBytes;
    }

    public void setTotalBytes(int totalBytes) {
        this.totalBytes = totalBytes;
    }

    public void setDownloadSpeed(int downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }
}

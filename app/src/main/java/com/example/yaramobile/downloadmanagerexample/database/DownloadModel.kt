package com.example.yaramobile.downloadmanagerexample.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "downloadModel")
data class DownloadModel(
    var downloadId: Int?,
    var url: String?,
    var path: String?,
    var name: String?,
    var status: Int?,
    var soFarBytes: Int?,
    var totalBytes: Int?,
    var downloadSpeed: Int?,
    var downloadError: String?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = 0
}

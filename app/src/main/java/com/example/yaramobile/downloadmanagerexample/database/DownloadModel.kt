package com.example.yaramobile.downloadmanagerexample.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "downloadModel")
class DownloadModel(
    @field:PrimaryKey
    var id: Int?,
    var url: String?,
    var path: String?,
    var name: String?,
    var status: Int?,
    var soFarBytes: Int?,
    var totalBytes: Int?,
    var downloadSpeed: Int?,
    var downloadError: String?
)

package com.example.yaramobile.downloadmanagerexample

import com.example.yaramobile.downloadmanagerexample.database.DownloadModel

interface DownloadActionListener {

    fun stopDownload()

    fun getDownloadModel(): DownloadModel

    fun getAllDownloadModel():List<DownloadModel>

}
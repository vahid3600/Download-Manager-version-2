package com.example.yaramobile.downloadmanagerexample

import com.example.yaramobile.downloadmanagerexample.database.DownloadModel

interface DownloadManagerListener {

    fun downloadFailed(downloadModel: DownloadModel?)

    fun downloadPaused(downloadModel: DownloadModel?)

    fun downloadPending(downloadModel: DownloadModel?)

    fun downloadRunning(downloadModel: DownloadModel?)

    fun downloadSuccessful(downloadModel: DownloadModel?)

    fun downloadStopped(downloadModel: DownloadModel?)
}
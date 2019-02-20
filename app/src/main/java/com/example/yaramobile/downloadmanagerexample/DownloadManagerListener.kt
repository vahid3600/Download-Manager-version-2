package com.example.yaramobile.downloadmanagerexample

interface DownloadManagerListener {

    fun downloadFailed(errorMessage: String, bytesDownloaded: Int?, bytesTotal: Int?)

    fun downloadPaused(pauseMessage: String, bytesDownloaded: Int?, bytesTotal: Int?)

    fun downloadPending(bytesDownloaded: Int?, bytesTotal: Int?)

    fun downloadRunning(bytesDownloaded: Int?, bytesTotal: Int?)

    fun downloadSuccessful(bytesDownloaded: Int?, bytesTotal: Int?)

    fun downloadStopped()
}
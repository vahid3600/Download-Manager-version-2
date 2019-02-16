package com.example.yaramobile.downloadmanagerexample

import android.app.DownloadManager
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

class DownloadService : IntentService("DownloadService") {

    companion object {

        private val DOWNLOAD_PATH = "com.spartons.androiddownloadmanager_DownloadSongService_Download_path"
        private val DESTINATION_PATH = "com.spartons.androiddownloadmanager_DownloadSongService_Destination_path"
        var mThread: Thread? = null
        var downloadManager: DownloadManager? = null
        var downloadManagerListener: DownloadManagerListener? = null
        var downloadId: Long? = null

        fun getDownloadService(callingClassContext: Context, downloadPath: String, destinationPath: String): Intent {
            return Intent(callingClassContext, DownloadService::class.java)
                .putExtra(DOWNLOAD_PATH, downloadPath)
                .putExtra(DESTINATION_PATH, destinationPath)
        }

        fun setDownloadListener(downloadManagerListener: DownloadManagerListener) {
            this.downloadManagerListener = downloadManagerListener
        }

        fun stopDownload() {
            downloadId?.let { downloadManager?.remove(it) }
            mThread?.isInterrupted
            mThread?.join()
            downloadManagerListener?.downloadStopped()
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        val downloadPath = intent!!.getStringExtra(DOWNLOAD_PATH)
        val destinationPath = intent.getStringExtra(DESTINATION_PATH)
        startDownload(downloadPath, destinationPath)
    }

    private fun startDownload(downloadPath: String, destinationPath: String) {
        val uri = Uri.parse(downloadPath) // Path where you want to download file.
        downloadManager = (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
        val request = DownloadManager.Request(uri)

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)  // Tell on which network you want to download file.
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)  // This will show notification on top when downloading the file.
        request.setTitle("Downloading a file") // Title for notification.
        request.setVisibleInDownloadsUi(true)
        request.setDestinationInExternalPublicDir(destinationPath, uri.getLastPathSegment())  // Storage directory path
        downloadId = downloadManager?.enqueue(request) // This will start downloading

        downloadManagerListener?.let { getDownloadStatus(it, downloadId) }
    }

    private fun getDownloadStatus(downloadManagerListener: DownloadManagerListener?, downloadId: Long?) {

        mThread = Thread(Runnable {

            var downloading = true

            while (downloading) {

                val q = DownloadManager.Query()
                downloadId?.let { q.setFilterById(it) }
                val cursor = downloadManager?.query(downloadId?.let { DownloadManager.Query().setFilterById(it) })

                cursor?.moveToFirst()

                Log.e("DownloadSongService", "getDownloadStatus " + cursor?.columnCount + " " + cursor?.count)
                if (cursor?.count != 0) {
                    val columnIndex = cursor?.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = columnIndex?.let { cursor.getInt(it) }
                    val columnReason = cursor?.getColumnIndex(DownloadManager.COLUMN_REASON)
                    val reason = columnReason?.let { cursor.getInt(it) }
                    var reasonText = ""


                    val bytesDownloaded = cursor?.getInt(
                        cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    )
                    val bytesTotal = cursor?.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                    when (status) {
                        DownloadManager.STATUS_FAILED -> {
                            when (reason) {
                                DownloadManager.ERROR_CANNOT_RESUME -> reasonText = "ERROR_CANNOT_RESUME"
                                DownloadManager.ERROR_DEVICE_NOT_FOUND -> reasonText = "ERROR_DEVICE_NOT_FOUND"
                                DownloadManager.ERROR_FILE_ALREADY_EXISTS -> reasonText = "ERROR_FILE_ALREADY_EXISTS"
                                DownloadManager.ERROR_FILE_ERROR -> reasonText = "ERROR_FILE_ERROR"
                                DownloadManager.ERROR_HTTP_DATA_ERROR -> reasonText = "ERROR_HTTP_DATA_ERROR"
                                DownloadManager.ERROR_INSUFFICIENT_SPACE -> reasonText = "ERROR_INSUFFICIENT_SPACE"
                                DownloadManager.ERROR_TOO_MANY_REDIRECTS -> reasonText = "ERROR_TOO_MANY_REDIRECTS"
                                DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> reasonText = "ERROR_UNHANDLED_HTTP_CODE"
                                DownloadManager.ERROR_UNKNOWN -> reasonText = "ERROR_UNKNOWN"
                            }
                            downloadManagerListener?.downloadFailed(reasonText, bytesDownloaded, bytesTotal)
                            downloading = false
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            when (reason) {
                                DownloadManager.PAUSED_QUEUED_FOR_WIFI -> reasonText = "PAUSED_QUEUED_FOR_WIFI"
                                DownloadManager.PAUSED_UNKNOWN -> reasonText = "PAUSED_UNKNOWN"
                                DownloadManager.PAUSED_WAITING_FOR_NETWORK -> reasonText = "PAUSED_WAITING_FOR_NETWORK"
                                DownloadManager.PAUSED_WAITING_TO_RETRY -> reasonText = "PAUSED_WAITING_TO_RETRY"
                            }
                            downloadManagerListener?.downloadPaused(reasonText, bytesDownloaded, bytesTotal)
                            downloading = false
                        }
                        DownloadManager.STATUS_PENDING -> downloadManagerListener?.downloadPending(
                            bytesDownloaded,
                            bytesTotal
                        )
                        DownloadManager.STATUS_RUNNING -> downloadManagerListener?.downloadRunning(
                            bytesDownloaded,
                            bytesTotal
                        )
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downloadManagerListener?.downloadSuccessful(
                                bytesDownloaded,
                                bytesTotal
                            )
                            downloading = false
                        }
                    }

                } else {
                    downloading = false
                }
                cursor?.close()

                Thread.sleep(100)
            }
        })

        mThread?.start()
    }

    interface DownloadManagerListener {

        fun downloadFailed(errorMessage: String, bytesDownloaded: Int?, bytesTotal: Int?)

        fun downloadPaused(pauseMessage: String, bytesDownloaded: Int?, bytesTotal: Int?)

        fun downloadPending(bytesDownloaded: Int?, bytesTotal: Int?)

        fun downloadRunning(bytesDownloaded: Int?, bytesTotal: Int?)

        fun downloadSuccessful(bytesDownloaded: Int?, bytesTotal: Int?)

        fun downloadStopped()
    }
}
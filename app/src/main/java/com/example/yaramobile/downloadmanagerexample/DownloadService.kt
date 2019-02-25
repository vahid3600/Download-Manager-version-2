package com.example.yaramobile.downloadmanagerexample

import android.app.DownloadManager
import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.yaramobile.downloadmanagerexample.database.DownloadDao
import com.example.yaramobile.downloadmanagerexample.database.DownloadDatabase
import com.example.yaramobile.downloadmanagerexample.database.DownloadModel

class DownloadService : IntentService("DownloadService") {

    private var downloadDao: DownloadDao? = null

    companion object {

        private val CALLBACK_LIMIT_TIME: Long = 100
        private val DOWNLOAD_TASK_ID = "com.spartons.androiddownloadmanager_DownloadSongService_Download_Task_ID"
        private val DOWNLOAD_PATH = "com.spartons.androiddownloadmanager_DownloadSongService_Download_path"
        private val DESTINATION_PATH = "com.spartons.androiddownloadmanager_DownloadSongService_Destination_path"
        var mThread: Thread? = null
        var downloadManager: DownloadManager? = null
        var downloadManagerListener: DownloadManagerListener? = null
        var downloadId: Long? = null
        var instance: DownloadService? = null

        fun getDownloadService(
            context: Context?,
            downloadId: Int?,
            downloadPath: String?,
            destinationPath: String?,
            downloadManagerListener: DownloadManagerListener
        ): Intent {

            Log.e("DownloadService", "getDownloadService " + downloadId)

            this.downloadManagerListener = downloadManagerListener
            return Intent(context, DownloadService::class.java)
                .putExtra(DOWNLOAD_PATH, downloadPath)
                .putExtra(DESTINATION_PATH, destinationPath)
                .putExtra(DOWNLOAD_TASK_ID, downloadId)
        }

        fun stopDownload(downloadId: Long?) {
            downloadId?.let { downloadManager?.remove(it) }
            mThread?.isInterrupted
            mThread?.join()
        }
    }

    override fun onCreate() {
        super.onCreate()
        initDataBase()
    }

    override fun onHandleIntent(intent: Intent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val downloadId = intent?.getIntExtra(DOWNLOAD_TASK_ID, 0)
        val downloadPath = intent?.getStringExtra(DOWNLOAD_PATH)
        val destinationPath = intent?.getStringExtra(DESTINATION_PATH)

        Log.e("DownloadService", "onStartCommand " + downloadId)

        startDownload(downloadId, downloadPath, destinationPath)
        return Service.START_STICKY
    }

    private fun initDataBase() {
        downloadDao = DownloadDatabase.getDownloadDatabase(applicationContext).downloadDao()
    }


    private fun startDownload(downloadTaskId: Int?, downloadPath: String?, destinationPath: String?) {

        val fileName: CharSequence? =
            downloadPath?.lastIndexOf('.')?.let { downloadPath.substring(downloadPath.lastIndexOf('/') + 1, it) }

        val uri = Uri.parse(downloadPath) // Path where you want to download file.
        downloadManager = (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
        val request = DownloadManager.Request(uri)

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)  // Tell on which network you want to download file.
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)  // This will show notification on top when downloading the file.
        request.setTitle(fileName) // Title for notification.
        request.setVisibleInDownloadsUi(true)
        request.setDestinationInExternalPublicDir(destinationPath, uri.getLastPathSegment())  // Storage directory path
        downloadId = downloadManager?.enqueue(request) // This will start downloading
        getDownloadStatus(downloadId, downloadTaskId)
    }

    private fun getDownloadStatus(downloadId: Long?, downloadTaskId: Int?) {

        mThread = Thread(Runnable {

            var downloading = true

            while (downloading) {

                val q = DownloadManager.Query()
                downloadId?.let { q.setFilterById(it) }
                val cursor = downloadManager?.query(downloadId?.let { DownloadManager.Query().setFilterById(it) })

                cursor?.moveToFirst()

                if (cursor?.count != 0) {
                    val id = cursor?.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID))
                    val uri = cursor?.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))
                    val path = cursor?.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    val title = cursor?.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
                    val status = cursor?.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val reason = cursor?.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                    var reasonText = ""
                    var statusText = ""

                    val bytesDownloaded = cursor?.getInt(
                        cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    )
                    val bytesTotal = cursor?.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                    when (status) {
                        DownloadManager.STATUS_FAILED -> {
                            statusText = "STATUS_FAILED"
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
                            downloadManagerListener?.downloadFailed(
                                makeDownloadModel(
                                    downloadTaskId,
                                    id,
                                    status,
                                    bytesDownloaded,
                                    bytesTotal,
                                    0,
                                    reasonText
                                )
                            )
                            downloading = false
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            statusText = "STATUS_PAUSED"
                            when (reason) {
                                DownloadManager.PAUSED_QUEUED_FOR_WIFI -> reasonText = "PAUSED_QUEUED_FOR_WIFI"
                                DownloadManager.PAUSED_UNKNOWN -> reasonText = "PAUSED_UNKNOWN"
                                DownloadManager.PAUSED_WAITING_FOR_NETWORK -> reasonText = "PAUSED_WAITING_FOR_NETWORK"
                                DownloadManager.PAUSED_WAITING_TO_RETRY -> reasonText = "PAUSED_WAITING_TO_RETRY"
                            }
                            downloadManagerListener?.downloadPaused(
                                makeDownloadModel(
                                    downloadTaskId,
                                    id,
                                    status,
                                    bytesDownloaded,
                                    bytesTotal,
                                    0,
                                    reasonText
                                )
                            )
                            downloading = false
                        }
                        DownloadManager.STATUS_PENDING -> {
                            statusText = "STATUS_PENDING"
                            downloadManagerListener?.downloadPending(
                                makeDownloadModel(
                                    downloadTaskId,
                                    id,
                                    status,
                                    bytesDownloaded,
                                    bytesTotal,
                                    0,
                                    reasonText
                                )
                            )
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            statusText = "STATUS_RUNNING"
                            downloadManagerListener?.downloadRunning(
                                makeDownloadModel(
                                    downloadTaskId,
                                    id,
                                    status,
                                    bytesDownloaded,
                                    bytesTotal,
                                    0,
                                    reasonText
                                )
                            )
                        }

                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downloading = false
                            statusText = "STATUS_SUCCESSFUL"
                            stopSelf()
                            downloadManagerListener?.downloadSuccessful(
                                makeDownloadModel(
                                    downloadTaskId,
                                    id,
                                    status,
                                    bytesDownloaded,
                                    bytesTotal,
                                    0,
                                    reasonText
                                )
                            )
                        }
                    }

                    Log.e(
                        "DownloadSongService",
                        "getDownloadStatus " + downloadTaskId + " " + status + " " + statusText
                    )
                } else {
                    downloading = false
                }

                cursor?.close()

                Thread.sleep(CALLBACK_LIMIT_TIME)
            }
        })

        mThread?.start()
    }

    fun makeDownloadModel(
        downloadTaskId: Int?,
        downloadId: Int?,
        status: Int?,
        bytesDownloaded: Int?,
        totalBytes: Int?,
        downloadSpeed: Int?,
        downloadError: String?
    ): DownloadModel? {
        val downloadModel =
            downloadTaskId?.let {
                DownloadDatabase.getDownloadDatabase(applicationContext).downloadDao().getDownloadModelById(
                    it
                )
            }

        Log.e(
            "DownloadService",
            "makeDownloadModel " + downloadModel?.downloadId + " " + downloadModel?.id + " " + downloadModel?.status
        )

        downloadModel?.downloadId = downloadId
        downloadModel?.status = status
        downloadModel?.soFarBytes = bytesDownloaded
        downloadModel?.totalBytes = totalBytes
        downloadModel?.downloadSpeed = downloadSpeed
        downloadModel?.downloadError = downloadError

        return downloadModel
    }
}
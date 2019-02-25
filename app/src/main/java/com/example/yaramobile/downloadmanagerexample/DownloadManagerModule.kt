package com.example.yaramobile.downloadmanagerexample

import android.app.DownloadManager
import android.arch.lifecycle.LiveData
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.yaramobile.downloadmanagerexample.database.DownloadDatabase
import com.example.yaramobile.downloadmanagerexample.database.DownloadModel

class DownloadManagerModule {

    var intent: Intent? = null
    val STATUS_QUEUE = 5
    private val CALLBACK_LIMIT_TIME: Long = 100
    private var downloading = true


    companion object {

        var instance: DownloadManagerModule? = null
        var context: Context? = null
        var mThread: Thread? = null

        fun initDownloadmanager(context: Context?): DownloadManagerModule? {
            this.context = context

            if (instance == null) {
                instance = DownloadManagerModule()
            }
            return instance
        }
    }

    fun addDownloadTask(downloadUri: String, downloadPath: String) {

        saveDownloadModel(
            DownloadModel(
                0,
                downloadUri,
                downloadPath,
                "",
                STATUS_QUEUE,
                0,
                0,
                0,
                ""
            )
        )

        checkDownloadsDatabase(StartDownload())
    }

    fun stopDownload(downloadId: Long?) {
        context?.stopService(intent)
        DownloadService.stopDownload(downloadId)
    }

    fun getDownloadModelLiveData(downloadId: Int): LiveData<DownloadModel> {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao().getDownloadModelLiveData(downloadId)
    }

    fun getListDownloadModelLiveData(): LiveData<List<DownloadModel>> {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao().listDownloadModel
    }

    private fun checkDownloadsDatabase(downloadDatabaseListener: DownloadDatabaseListener) {
        Log.e(
            "DownloadManagerModule",
            "checkDownloadsDatabase " + runningDownloadsListSize() + " " + pendingDownloadsListSize() + " " + inQueueDownloadsListSize() + " " + errorDownloadsListSize() + " " + pausedDownloadsListSize()
        )
        if (runningDownloadsListSize() <= 0 && pausedDownloadsListSize() <= 0 && pendingDownloadsListSize() <= 0 && errorDownloadsListSize() <= 0 && inQueueDownloadsListSize() != 0) {
            downloadDatabaseListener.getDownloadModel(getInQueueDownloadsList()?.get(0))
        } else
            downloadDatabaseListener.dataNotAvailable()
    }

    private fun saveDownloadModel(downloadModel: DownloadModel?) {
        Log.e("DownloadManagerModule", "saveDownloadModel " + downloadModel?.id + " " + downloadModel?.downloadId)
        DownloadDatabase.getDownloadDatabase(context).downloadDao()?.saveDownloadModel(downloadModel)
    }

    private fun updateDownloadModel(downloadModel: DownloadModel?) {
        Log.e("DownloadManagerModule", "updateDownloadModel " + downloadModel?.id + " " + downloadModel?.downloadId)
        DownloadDatabase.getDownloadDatabase(context).downloadDao()?.updateDownloadModel(downloadModel)
    }

    inner class StartDownload : DownloadDatabaseListener {

        override fun getDownloadModel(downloadModel: DownloadModel?) {
            Log.e(
                "DownloadManagerModule",
                "getDownloadModelLiveData " + downloadModel + " " + runningDownloadsListSize() + " " + pendingDownloadsListSize() + " " + inQueueDownloadsListSize()
            )

            context?.startService(
                DownloadService.getDownloadService(
                    context,
                    downloadModel?.id,
                    downloadModel?.url,
                    downloadModel?.path,
                    object : DownloadManagerListener {
                        override fun downloadFailed(downloadModel: DownloadModel?) {
                            updateDownloadModel(downloadModel)
                            Log.e("DownloadManagerModule", "downloadFailed")
                            checkDownloadsDatabase(StartDownload())
                        }

                        override fun downloadPaused(downloadModel: DownloadModel?) {
                            updateDownloadModel(downloadModel)
                            Log.e("DownloadManagerModule", "downloadPaused")
                            checkDownloadsDatabase(StartDownload())
                        }

                        override fun downloadPending(downloadModel: DownloadModel?) {
                            updateDownloadModel(downloadModel)
                            Log.e("DownloadManagerModule", "downloadPending")
                            checkDownloadsDatabase(StartDownload())
                        }

                        override fun downloadRunning(downloadModel: DownloadModel?) {
                            updateDownloadModel(downloadModel)
                            Log.e(
                                "DownloadManagerModule",
                                "downloadRunning " + downloadModel?.status + " " + downloadModel?.id + " " + downloadModel?.downloadId
                            )
                            checkDownloadsDatabase(StartDownload())
                        }

                        override fun downloadSuccessful(downloadModel: DownloadModel?) {
                            updateDownloadModel(downloadModel)
                            Log.e("DownloadManagerModule", "downloadSuccessful")
                            checkDownloadsDatabase(StartDownload())
                        }

                        override fun downloadStopped(downloadModel: DownloadModel?) {
                            updateDownloadModel(downloadModel)
                            Log.e("DownloadManagerModule", "downloadStopped")
                            checkDownloadsDatabase(StartDownload())
                        }

                    }
                )
            )
        }

        override fun dataNotAvailable() {
            Log.e("DownloadManagerModule", "dataNotAvailable")
        }

    }

    fun checkDownloadListStatus() {
        var downloadModels: List<DownloadModel> =
            DownloadDatabase.getDownloadDatabase(context).downloadDao().allDownloadModels
        for (i in 0 until downloadModels.size) {
            if (downloadModels[i].status != android.app.DownloadManager.STATUS_SUCCESSFUL) {

                Log.e(
                    "DownloadManagerModule",
                    "checkDownloadListStatus " + downloadModels[i].status + " " + downloadModels[i].id + " " + downloadModels[i].downloadId
                )

                setDownloadStatusListener(downloadModels[i].id.toLong(), object : DownloadManagerListener {
                    override fun downloadFailed(downloadModel: DownloadModel?) {
                        Log.e(
                            "DownloadManagerModule",
                            "checkDownloadListStatus downloadFailed"
                        )
                    }

                    override fun downloadPaused(downloadModel: DownloadModel?) {
                        Log.e(
                            "DownloadManagerModule",
                            "checkDownloadListStatus downloadPaused"
                        )
                    }

                    override fun downloadPending(downloadModel: DownloadModel?) {
                        Log.e(
                            "DownloadManagerModule",
                            "checkDownloadListStatus downloadPending"
                        )
                    }

                    override fun downloadRunning(downloadModel: DownloadModel?) {
                        Log.e(
                            "DownloadManagerModule",
                            "checkDownloadListStatus downloadRunning"
                        )
                    }

                    override fun downloadSuccessful(downloadModel: DownloadModel?) {
                        Log.e(
                            "DownloadManagerModule",
                            "checkDownloadListStatus downloadSuccessful " + downloadModel?.id + " " + downloadModel?.downloadId
                        )
                    }

                    override fun downloadStopped(downloadModel: DownloadModel?) {
                        Log.e(
                            "DownloadManagerModule",
                            "checkDownloadListStatus downloadSuccessful"
                        )
                    }

                })
            }
        }
    }

    fun getDownloadModel(url: String, path: String): DownloadModel {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao().getDownloadModelByUrlAndPath(url, path)
    }

    fun setDownloadStatusListener(
        downloadId: Long?,
        downloadManagerListener: DownloadManagerListener?
    ) {

        mThread = Thread(Runnable {

            var downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager

            while (downloading) {

                var downloadModel =
                    downloadId?.toInt()?.let {
                        DownloadDatabase.getDownloadDatabase(context).downloadDao().getDownloadModelById(
                            it
                        )
                    }

                Log.e(
                    "DownloadManagerModule",
                    "setDownloadStatusListener " + downloading + " " + downloadId + " " + downloadModel?.downloadId + " " + downloadModel?.status
                )

                val q = DownloadManager.Query()
                downloadModel?.downloadId?.let { q.setFilterById(it.toLong()) }

                val cursor =
                    downloadManager.query(downloadId?.let { android.app.DownloadManager.Query().setFilterById(it) })

                cursor?.moveToFirst()

                Log.e("DownloadManagerModule", "cursor " + cursor?.count + " " + downloadModel?.downloadId + " " + q)
                if (cursor?.count != 0) {

                    val id = cursor?.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID))
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
                            downloadModel?.setDownloadId(id)
                            downloadModel?.setStatus(status)
                            downloadModel?.setSoFarBytes(bytesDownloaded)
                            downloadModel?.setTotalBytes(bytesTotal)
                            downloadModel?.setDownloadSpeed(0)
                            downloadModel?.setDownloadError(reasonText)

                            downloadManagerListener?.downloadFailed(
                                downloadModel
                            )
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            when (reason) {
                                DownloadManager.PAUSED_QUEUED_FOR_WIFI -> reasonText = "PAUSED_QUEUED_FOR_WIFI"
                                DownloadManager.PAUSED_UNKNOWN -> reasonText = "PAUSED_UNKNOWN"
                                DownloadManager.PAUSED_WAITING_FOR_NETWORK -> reasonText = "PAUSED_WAITING_FOR_NETWORK"
                                DownloadManager.PAUSED_WAITING_TO_RETRY -> reasonText = "PAUSED_WAITING_TO_RETRY"
                            }
                            downloadModel?.setDownloadId(id)
                            downloadModel?.setStatus(status)
                            downloadModel?.setSoFarBytes(bytesDownloaded)
                            downloadModel?.setTotalBytes(bytesTotal)
                            downloadModel?.setDownloadSpeed(0)
                            downloadModel?.setDownloadError(reasonText)

                            downloadManagerListener?.downloadPaused(
                                downloadModel
                            )
                        }
                        DownloadManager.STATUS_PENDING -> {
                            statusText = "STATUS_PENDING"
                            downloadModel?.setDownloadId(id)
                            downloadModel?.setStatus(status)
                            downloadModel?.setSoFarBytes(bytesDownloaded)
                            downloadModel?.setTotalBytes(bytesTotal)
                            downloadModel?.setDownloadSpeed(0)
                            downloadModel?.setDownloadError(reasonText)

                            downloadManagerListener?.downloadPending(
                                downloadModel
                            )
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            statusText = "STATUS_RUNNING"
                            downloadModel?.setDownloadId(id)
                            downloadModel?.setStatus(status)
                            downloadModel?.setSoFarBytes(bytesDownloaded)
                            downloadModel?.setTotalBytes(bytesTotal)
                            downloadModel?.setDownloadSpeed(0)
                            downloadModel?.setDownloadError(reasonText)

                            downloadManagerListener?.downloadRunning(
                                downloadModel
                            )
                        }

                        DownloadManager.STATUS_SUCCESSFUL -> {
                            statusText = "STATUS_SUCCESSFUL"
                            downloadModel?.setDownloadId(id)
                            downloadModel?.setStatus(status)
                            downloadModel?.setSoFarBytes(bytesDownloaded)
                            downloadModel?.setTotalBytes(bytesTotal)
                            downloadModel?.setDownloadSpeed(0)
                            downloadModel?.setDownloadError(reasonText)

                            downloadManagerListener?.downloadSuccessful(
                                downloadModel
                            )
                        }
                    }

                    Log.e("DownloadSongService", "getDownloadStatus " + status + " " + statusText)
                } else {
                    downloading = false
                }

                cursor?.close()

                Thread.sleep(CALLBACK_LIMIT_TIME)
            }
        })

        mThread?.start()
    }

    private fun runningDownloadsListSize(): Int {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao()
            .getDownloadModelListByStatus(android.app.DownloadManager.STATUS_RUNNING).size
    }

    private fun pausedDownloadsListSize(): Int {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao()
            .getDownloadModelListByStatus(android.app.DownloadManager.STATUS_PAUSED).size
    }

    private fun pendingDownloadsListSize(): Int {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao()
            .getDownloadModelListByStatus(android.app.DownloadManager.STATUS_PENDING).size
    }

    private fun inQueueDownloadsListSize(): Int {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao()
            .getDownloadModelListByStatus(STATUS_QUEUE).size
    }

    private fun errorDownloadsListSize(): Int {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao()
            .getDownloadModelListByStatus(DownloadManager.STATUS_FAILED).size
    }

    private fun getInQueueDownloadsList(): List<DownloadModel>? {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao()?.getDownloadModelListByStatus(STATUS_QUEUE)
    }


    interface DownloadDatabaseListener {

        fun getDownloadModel(downloadModel: DownloadModel?)

        fun dataNotAvailable()
    }
}
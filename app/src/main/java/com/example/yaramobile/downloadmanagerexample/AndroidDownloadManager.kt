package com.example.yaramobile.downloadmanagerexample

import android.app.Activity
import android.app.Application
import android.app.DownloadManager
import android.arch.lifecycle.LiveData
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.yaramobile.downloadmanagerexample.database.DownloadDatabase
import com.example.yaramobile.downloadmanagerexample.database.DownloadModel

class AndroidDownloadManager {

    var intent: Intent? = null
    val STATUS_QUEUE = 5
    private var downloading = true
    var downloadManagerListener: DownloadManagerListener? = null

    companion object {

        var instance: AndroidDownloadManager? = null
        var application: Application? = null

    }


    fun initDownloadManager(application: Application?): AndroidDownloadManager? {
        Companion.application = application

        application?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
                Log.e("AndroidDownloadManager", "onActivityPaused")
            }

            override fun onActivityResumed(activity: Activity?) {
                Log.e("AndroidDownloadManager", "onActivityResumed")
            }

            override fun onActivityStarted(activity: Activity?) {
                Log.e("AndroidDownloadManager", "onActivityStarted")
                updateDownloadDatabase()
            }

            override fun onActivityDestroyed(activity: Activity?) {
                Log.e("AndroidDownloadManager", "onActivityDestroyed")
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
                Log.e("AndroidDownloadManager", "onActivitySaveInstanceState")
            }

            override fun onActivityStopped(activity: Activity?) {
                Log.e("AndroidDownloadManager", "onActivityStopped")
            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                Log.e("AndroidDownloadManager", "onActivityCreated")
            }
        })

        if (instance == null) {
            instance = AndroidDownloadManager()
        }
        return instance
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
                ""
            )
        )

        checkDownloadsDatabase(StartDownload())
    }

    fun stopDownload(downloadId: Long?) {
        DownloadService.stopDownload(downloadId)
    }

    fun stopService() {
        application?.stopService(intent)
    }

    fun getDownloadModelLiveData(downloadId: Int): LiveData<DownloadModel> {
        return DownloadDatabase.getDownloadDatabase(application).downloadDao().getDownloadModelLiveData(downloadId)
    }

    fun getListDownloadModelLiveData(): LiveData<List<DownloadModel>> {
        return DownloadDatabase.getDownloadDatabase(application).downloadDao().listDownloadModel
    }

    private fun checkDownloadsDatabase(downloadDatabaseListener: DownloadDatabaseListener) {

        Log.e(
            "AndroidDownloadManager",
            "checkDownloadsDatabase " + " running " + runningDownloadsList() + " pending " + pendingDownloadsListSize() + " inQueue " + inQueueDownloadsListSize() + " " + pausedDownloadsListSize() + " error " + errorDownloadsListSize()
        )

        if (runningDownloadsList().size <= 0 && pendingDownloadsListSize().size <= 0 && errorDownloadsListSize().size <= 0 && inQueueDownloadsListSize().size != 0) {
            downloadDatabaseListener.getDownloadModel(getInQueueDownloadsList()?.get(0))
        } else
            downloadDatabaseListener.dataNotAvailable()
    }

    private fun saveDownloadModel(downloadModel: DownloadModel?) {
        Log.e(
            "AndroidDownloadManager",
            "saveDownloadModel " + downloadModel?.id + " " + downloadModel?.downloadManagerId
        )
        DownloadDatabase.getDownloadDatabase(application).downloadDao()?.saveDownloadModel(downloadModel)
    }

    private fun updateDownloadModel(downloadModel: DownloadModel?) {
        Log.e(
            "AndroidDownloadManager",
            "updateDownloadModel " + downloadModel?.id + " " + downloadModel?.downloadManagerId
        )
        DownloadDatabase.getDownloadDatabase(application).downloadDao()?.updateDownloadModel(downloadModel)
    }

    inner class StartDownload : DownloadDatabaseListener {

        override fun getDownloadModel(downloadModel: DownloadModel?) {

            Log.e(
                "AndroidDownloadManager",
                "getDownloadModel " + application + " " + downloadModel?.url + " " + downloadModel?.path + " " + downloadModel?.status + " running " + runningDownloadsList() + " pending " + pendingDownloadsListSize() + " inQueue " + inQueueDownloadsListSize() + " " + pausedDownloadsListSize() + " error " + errorDownloadsListSize()
            )

            application?.startService(
                DownloadService.getDownloadService(
                    DownloadService.ServiceMode.Download,
                    application,
                    downloadModel?.id,
                    downloadModel?.downloadManagerId,
                    downloadModel?.url,
                    downloadModel?.path
                )
            )

            DownloadService.setListener(object : DownloadManagerListener {
                override fun downloadFailed(downloadModel: DownloadModel?) {
                    updateDownloadModel(downloadModel)
                    Log.e("AndroidDownloadManager", "downloadFailed")
                    checkDownloadsDatabase(StartDownload())
                    downloadManagerListener?.downloadFailed(downloadModel)

                }

                override fun downloadPaused(downloadModel: DownloadModel?) {
                    updateDownloadModel(downloadModel)
                    Log.e("AndroidDownloadManager", "downloadPaused")
                    checkDownloadsDatabase(StartDownload())
                    downloadManagerListener?.downloadPaused(downloadModel)
                }

                override fun downloadPending(downloadModel: DownloadModel?) {
                    updateDownloadModel(downloadModel)
                    Log.e("AndroidDownloadManager", "downloadPending")
                    checkDownloadsDatabase(StartDownload())
                    downloadManagerListener?.downloadPending(downloadModel)
                }

                override fun downloadRunning(downloadModel: DownloadModel?) {
                    updateDownloadModel(downloadModel)
                    Log.e(
                        "AndroidDownloadManager",
                        "downloadRunning " + downloadModel?.status + " " + downloadModel?.id + " " + downloadModel?.downloadManagerId
                    )
                    checkDownloadsDatabase(StartDownload())
                    downloadManagerListener?.downloadRunning(downloadModel)
                }

                override fun downloadSuccessful(downloadModel: DownloadModel?) {
                    updateDownloadModel(downloadModel)
                    Log.e("AndroidDownloadManager", "downloadSuccessful")
                    checkDownloadsDatabase(StartDownload())
                    downloadManagerListener?.downloadSuccessful(downloadModel)
                }

                override fun downloadStopped(downloadModel: DownloadModel?) {
                    updateDownloadModel(downloadModel)
                    Log.e("AndroidDownloadManager", "downloadStopped")
                    checkDownloadsDatabase(StartDownload())
                    downloadManagerListener?.downloadStopped(downloadModel)
                }

            })
        }

        override fun dataNotAvailable() {
            Log.e("AndroidDownloadManager", "dataNotAvailable")
        }

    }

    fun setDownloadManagerCallback(downloadManagerListener: DownloadManagerListener?) {
        this.downloadManagerListener = downloadManagerListener
        showRunningDownloadStatus()
    }

    private fun showRunningDownloadStatus() {
        if (runningDownloadsList().size == 1)
            application?.startService(
                DownloadService.getDownloadService(
                    DownloadService.ServiceMode.Callback,
                    application,
                    runningDownloadsList()[0].id,
                    runningDownloadsList()[0].downloadManagerId,
                    runningDownloadsList()[0].url,
                    runningDownloadsList()[0].path
                )
            )
    }

    private fun updateDownloadDatabase() {
        var downloadModels: List<DownloadModel> =
            DownloadDatabase.getDownloadDatabase(application).downloadDao().allDownloadModels
        for (i in 0 until downloadModels.size) {

            setDownloadStatusListener(downloadModels[i].id.toLong(), object : DownloadManagerListener {
                override fun downloadFailed(downloadModel: DownloadModel?) {
                    Log.e(
                        "AndroidDownloadManager",
                        "checkDownloadListStatus downloadFailed"
                    )
                    updateDownloadModel(downloadModel)
                }

                override fun downloadPaused(downloadModel: DownloadModel?) {
                    Log.e(
                        "AndroidDownloadManager",
                        "checkDownloadListStatus downloadPaused"
                    )
                    updateDownloadModel(downloadModel)
                }

                override fun downloadPending(downloadModel: DownloadModel?) {
                    Log.e(
                        "AndroidDownloadManager",
                        "checkDownloadListStatus downloadPending"
                    )
                    updateDownloadModel(downloadModel)
                }

                override fun downloadRunning(downloadModel: DownloadModel?) {
                    Log.e(
                        "AndroidDownloadManager",
                        "checkDownloadListStatus downloadRunning"
                    )
                    updateDownloadModel(downloadModel)
                }

                override fun downloadSuccessful(downloadModel: DownloadModel?) {
                    Log.e(
                        "AndroidDownloadManager",
                        "checkDownloadListStatus downloadSuccessful " + downloadModel?.id + " " + downloadModel?.downloadManagerId
                    )
                    updateDownloadModel(downloadModel)
                }

                override fun downloadStopped(downloadModel: DownloadModel?) {
                    Log.e(
                        "AndroidDownloadManager",
                        "checkDownloadListStatus downloadSuccessful"
                    )
                    updateDownloadModel(downloadModel)
                }

            })
        }
    }

    fun getDownloadModel(url: String, path: String): DownloadModel {
        return DownloadDatabase.getDownloadDatabase(application).downloadDao().getDownloadModelByUrlAndPath(url, path)
    }

    private fun setDownloadStatusListener(
        downloadId: Long?,
        downloadManagerListener: DownloadManagerListener?
    ) {


        val downloadManager = application?.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        val downloadModel =
            downloadId?.toInt()?.let {
                DownloadDatabase.getDownloadDatabase(application).downloadDao().getDownloadModelById(
                    it
                )
            }

        Log.e(
            "AndroidDownloadManager",
            "setDownloadStatusListener " + downloadId + " " + downloadModel?.status + " " + downloadModel?.downloadManagerId + " " + downloadModel?.id
        )
        val cursor = downloadManager.query(downloadModel?.downloadManagerId?.let {
            DownloadManager.Query().setFilterById(it.toLong())
        })

        cursor?.moveToFirst()

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
                    downloadModel?.setDownloadManagerId(id)
                    downloadModel?.setStatus(status)
                    downloadModel?.setSoFarBytes(bytesDownloaded)
                    downloadModel?.setTotalBytes(bytesTotal)
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
                    downloadModel?.setDownloadManagerId(id)
                    downloadModel?.setStatus(status)
                    downloadModel?.setSoFarBytes(bytesDownloaded)
                    downloadModel?.setTotalBytes(bytesTotal)
                    downloadModel?.setDownloadError(reasonText)

                    downloadManagerListener?.downloadPaused(
                        downloadModel
                    )
                }
                DownloadManager.STATUS_PENDING -> {
                    statusText = "STATUS_PENDING"
                    downloadModel?.setDownloadManagerId(id)
                    downloadModel?.setStatus(status)
                    downloadModel?.setSoFarBytes(bytesDownloaded)
                    downloadModel?.setTotalBytes(bytesTotal)
                    downloadModel?.setDownloadError(reasonText)

                    downloadManagerListener?.downloadPending(
                        downloadModel
                    )
                }
                DownloadManager.STATUS_RUNNING -> {
                    statusText = "STATUS_RUNNING"
                    downloadModel?.setDownloadManagerId(id)
                    downloadModel?.setStatus(status)
                    downloadModel?.setSoFarBytes(bytesDownloaded)
                    downloadModel?.setTotalBytes(bytesTotal)
                    downloadModel?.setDownloadError(reasonText)

                    downloadManagerListener?.downloadRunning(
                        downloadModel
                    )
                }

                DownloadManager.STATUS_SUCCESSFUL -> {
                    statusText = "STATUS_SUCCESSFUL"
                    downloadModel?.setDownloadManagerId(id)
                    downloadModel?.setStatus(status)
                    downloadModel?.setSoFarBytes(bytesDownloaded)
                    downloadModel?.setTotalBytes(bytesTotal)
                    downloadModel?.setDownloadError(reasonText)

                    downloadManagerListener?.downloadSuccessful(
                        downloadModel
                    )
                }
            }
        } else {
            downloading = false
        }

        cursor?.close()
    }

    private fun runningDownloadsList(): List<DownloadModel> {
        return DownloadDatabase.getDownloadDatabase(application).downloadDao()
            .getDownloadModelListByStatus(android.app.DownloadManager.STATUS_RUNNING)
    }

    private fun pausedDownloadsListSize(): List<DownloadModel> {
        return DownloadDatabase.getDownloadDatabase(application).downloadDao()
            .getDownloadModelListByStatus(android.app.DownloadManager.STATUS_PAUSED)
    }

    private fun pendingDownloadsListSize(): List<DownloadModel> {
        return DownloadDatabase.getDownloadDatabase(application).downloadDao()
            .getDownloadModelListByStatus(android.app.DownloadManager.STATUS_PENDING)
    }

    private fun inQueueDownloadsListSize(): List<DownloadModel> {
        return DownloadDatabase.getDownloadDatabase(application).downloadDao()
            .getDownloadModelListByStatus(STATUS_QUEUE)
    }

    private fun errorDownloadsListSize(): List<DownloadModel> {
        return DownloadDatabase.getDownloadDatabase(application).downloadDao()
            .getDownloadModelListByStatus(DownloadManager.STATUS_FAILED)
    }

    private fun getInQueueDownloadsList(): List<DownloadModel>? {
        return DownloadDatabase.getDownloadDatabase(application).downloadDao()
            ?.getDownloadModelListByStatus(STATUS_QUEUE)
    }


    interface DownloadDatabaseListener {

        fun getDownloadModel(downloadModel: DownloadModel?)

        fun dataNotAvailable()
    }
}
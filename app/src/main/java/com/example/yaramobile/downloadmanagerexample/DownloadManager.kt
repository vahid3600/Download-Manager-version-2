package com.example.yaramobile.downloadmanagerexample

import android.arch.lifecycle.LiveData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.yaramobile.downloadmanagerexample.database.DownloadDatabase
import com.example.yaramobile.downloadmanagerexample.database.DownloadModel

class DownloadManager {

    var intent: Intent? = null
    val STATUS_QUEUE = 5

    companion object {

        var instance: DownloadManager? = null
        var context: Context? = null

        fun initDownloadmanager(context: Context?): DownloadManager? {
            this.context = context

            if (instance == null) {
                instance = DownloadManager()
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

    public fun getDownloadModelLiveData(downloadId: Int): LiveData<DownloadModel> {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao().getDownloadModelLiveData(downloadId)
    }

    fun getListDownloadModelLiveData(): LiveData<List<DownloadModel>> {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao().listDownloadModel
    }

    protected fun getPath(url: String?): String {
        return if (url != null)
            getSaveDir() + Uri.parse(url).lastPathSegment
        else
            ""
    }

    protected fun getSaveDir(): String {
        return context?.filesDir?.absolutePath.toString()
    }

    protected fun checkDownloadsDatabase(downloadDatabaseListener: DownloadDatabaseListener) {
        Log.e(
            "DownloadManager",
            "checkDownloadsDatabase " + runningDownloadsListSize() + " " + pendingDownloadsListSize() + " " + inQueueDownloadsListSize()
        )
        if (runningDownloadsListSize() <= 0 && pendingDownloadsListSize() <= 0) {
            downloadDatabaseListener.getDownloadModel(getInQueueDownloadsList()?.get(0))
        } else
            downloadDatabaseListener.dataNotAvailable()
    }

    private fun saveDownloadModel(downloadModel: DownloadModel?) {
        Log.e("DownloadManager", "saveDownloadModel " + downloadModel?.id + " " + downloadModel?.downloadId)
        DownloadDatabase.getDownloadDatabase(context).downloadDao()?.saveDownloadModel(downloadModel)
    }

    inner class StartDownload : DownloadDatabaseListener {

        override fun getDownloadModel(downloadModel: DownloadModel?) {
            Log.e(
                "DownloadManager",
                "getDownloadModelLiveData " + downloadModel + " " + runningDownloadsListSize() + " " + pendingDownloadsListSize() + " " + inQueueDownloadsListSize()
            )

            context?.startService(
                DownloadService.getDownloadService(
                    context,
                    downloadModel?.downloadId,
                    downloadModel?.url,
                    downloadModel?.path,
                    object : DownloadManagerListener {
                        override fun downloadFailed(downloadModel: DownloadModel?) {
                            saveDownloadModel(downloadModel)
                            Log.e("DownloadManager", "downloadFailed")
                            checkDownloadsDatabase(StartDownload())
                        }

                        override fun downloadPaused(downloadModel: DownloadModel?) {
                            saveDownloadModel(downloadModel)
                            Log.e("DownloadManager", "downloadPaused")
                            checkDownloadsDatabase(StartDownload())
                        }

                        override fun downloadPending(downloadModel: DownloadModel?) {
                            saveDownloadModel(downloadModel)
                            Log.e("DownloadManager", "downloadPending")
                            checkDownloadsDatabase(StartDownload())
                        }

                        override fun downloadRunning(downloadModel: DownloadModel?) {
                            saveDownloadModel(downloadModel)
                            Log.e("DownloadManager", "downloadRunning")
                            checkDownloadsDatabase(StartDownload())
                        }

                        override fun downloadSuccessful(downloadModel: DownloadModel?) {
                            saveDownloadModel(downloadModel)
                            Log.e("DownloadManager", "downloadSuccessful")
                            checkDownloadsDatabase(StartDownload())
                        }

                        override fun downloadStopped(downloadModel: DownloadModel?) {
                            saveDownloadModel(downloadModel)
                            Log.e("DownloadManager", "downloadStopped")
                            checkDownloadsDatabase(StartDownload())
                        }

                    }
                )
            )
        }

        override fun dataNotAvailable() {
            Log.e("DownloadManager", "dataNotAvailable")
        }

    }

    protected fun runningDownloadsListSize(): Int {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao()
            .getDownloadModelListByStatus(android.app.DownloadManager.STATUS_RUNNING).size
    }

    protected fun pendingDownloadsListSize(): Int {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao()
            .getDownloadModelListByStatus(android.app.DownloadManager.STATUS_PENDING).size
    }

    protected fun inQueueDownloadsListSize(): Int {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao()
            .getDownloadModelListByStatus(STATUS_QUEUE).size
    }

    protected fun getInQueueDownloadsList(): List<DownloadModel>? {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao()?.getDownloadModelListByStatus(STATUS_QUEUE)
    }

    interface DownloadDatabaseListener {

        fun getDownloadModel(downloadModel: DownloadModel?)

        fun dataNotAvailable()
    }
}
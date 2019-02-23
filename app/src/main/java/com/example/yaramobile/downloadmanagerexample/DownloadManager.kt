package com.example.yaramobile.downloadmanagerexample

import android.app.Application
import android.arch.lifecycle.LiveData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.yaramobile.downloadmanagerexample.database.DownloadDatabase
import com.example.yaramobile.downloadmanagerexample.database.DownloadModel

class DownloadManager {

    var intent: Intent? = null

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

    fun startDownload(downloadUri: String, downloadPath: String) {
        intent = DownloadService.getDownloadService(
            context,
            downloadUri,
            downloadPath,
            object : DownloadManagerListener {
                override fun downloadFailed(downloadModel: DownloadModel) {
                    Log.e("DownloadManager", "downloadFailed")
                    checkDownloadDatabase()

                }

                override fun downloadPaused(downloadModel: DownloadModel) {
                    Log.e("DownloadManager", "downloadPaused")
                    checkDownloadDatabase()

                }

                override fun downloadPending(downloadModel: DownloadModel) {
                    Log.e("DownloadManager", "downloadPending")
                    checkDownloadDatabase()

                }

                override fun downloadRunning(downloadModel: DownloadModel) {
                    Log.e("DownloadManager", "downloadRunning")
                    checkDownloadDatabase()

                }

                override fun downloadSuccessful(downloadModel: DownloadModel) {
                    Log.e("DownloadManager", "downloadSuccessful")
                    checkDownloadDatabase()

                }

                override fun downloadStopped(downloadModel: DownloadModel) {
                    Log.e("DownloadManager", "downloadStopped")
                    checkDownloadDatabase()

                }

            }
        )

        context?.startService(
            intent
        )
    }

    fun stopDownload(downloadId: Long?) {
        context?.stopService(intent)
        DownloadService.stopDownload(downloadId)
    }

    public fun getDownloadModelLiveData(downloadId: Int): LiveData<DownloadModel> {
        return DownloadDatabase.getDownloadDatabase(context).downloadDao().getDownloadModel(downloadId)
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

    protected fun checkDownloadDatabase() {
        Log.e("DownloadManager",
            "checkDownloadDatabase " + DownloadDatabase.getDownloadDatabase(context).downloadDao()
                .getDownloadModelListByStatus(android.app.DownloadManager.STATUS_RUNNING).size
        )
    }
}
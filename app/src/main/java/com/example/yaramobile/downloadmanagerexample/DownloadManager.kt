package com.example.yaramobile.downloadmanagerexample

import android.content.Context
import android.net.Uri

class DownloadManager {

    companion object {

        private var context: Context? = null
        var instance: DownloadManager? = null

        fun initDownloadmanager(context: Context): DownloadManager? {

            this.context = context

            if (instance == null) {
                instance = DownloadManager()
            }
            return instance
        }

    }

    private fun startDownload() {
        context?.startService(
            DownloadService.getDownloadService(
                context!!,
                "http://5.239.244.167:1337/lyrics/api/files/bjCbe1h7B3DrLpZmCOwj1RYpgOPL4z8g/33cb19f604cbc1b4a4de95b8c946bac5_hWOMf19fFuc.mp4",
                getPath("http://5.239.244.167:1337/lyrics/api/files/bjCbe1h7B3DrLpZmCOwj1RYpgOPL4z8g/33cb19f604cbc1b4a4de95b8c946bac5_hWOMf19fFuc.mp4")
            )
        )
    }

    private fun setDownloadListener(downloadManagerListener: DownloadManagerListener){
        DownloadService.setDownloadListener(downloadManagerListener)
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
}
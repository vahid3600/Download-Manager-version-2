package com.example.yaramobile.downloadmanagerexample

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.yaramobile.downloadmanagerexample.database.DownloadModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var downloadManager = AndroidDownloadManager().initDownloadManager(application)

        start.setOnClickListener {
            downloadManager?.addDownloadTask(
                "http://5.239.244.167:1337/lyrics/api/files/bjCbe1h7B3DrLpZmCOwj1RYpgOPL4z8g/33cb19f604cbc1b4a4de95b8c946bac5_hWOMf19fFuc.mp4",
                getPath("http://5.239.244.167:1337/lyrics/api/files/bjCbe1h7B3DrLpZmCOwj1RYpgOPL4z8g/33cb19f604cbc1b4a4de95b8c946bac5_hWOMf19fFuc.mp4")
            )
        }

        downloadManager?.setDownloadManagerCallback(object : DownloadManagerListener {
            override fun downloadFailed(downloadModel: DownloadModel?) {
                Log.e(
                    "MainActivity",
                    "downloadFailed " + downloadModel?.status
                )
            }

            override fun downloadPaused(downloadModel: DownloadModel?) {
                Log.e(
                    "MainActivity",
                    "downloadPaused " + downloadModel?.status
                )
            }

            override fun downloadPending(downloadModel: DownloadModel?) {
                Log.e(
                    "MainActivity",
                    "downloadPending " + downloadModel?.status
                )
            }

            override fun downloadRunning(downloadModel: DownloadModel?) {
                Log.e(
                    "MainActivity",
                    "downloadRunning " + downloadModel?.status + " " + downloadModel?.soFarBytes + " " + downloadModel?.totalBytes
                )
                if (downloadModel?.soFarBytes != null && downloadModel.totalBytes != null) {
                    progressBar?.progress = downloadModel.soFarBytes
                    progressBar?.max = downloadModel.totalBytes
                }
            }

            override fun downloadSuccessful(downloadModel: DownloadModel?) {
                Log.e(
                    "MainActivity",
                    "downloadSuccessful " + downloadModel?.status
                )
            }

            override fun downloadStopped(downloadModel: DownloadModel?) {
                Log.e(
                    "MainActivity",
                    "downloadStopped " + downloadModel?.status
                )
            }
        })

//        downloadManager?.getListDownloadModelLiveData()?.observe(this,
//            Observer<List<DownloadModel>> { t -> Log.e("MainActivity", "onChanged " + t?.size) })

//        downloadManager.setDownloadStatusListener()

        fun checkDownloadQueue(downloadModel: DownloadModel) {

        }

//        stop.setOnClickListener { downloadManager?.stopDownload() }

//        DownloadService.setDownloadListener(object : DownloadService.DownloadManagerListener {
//
//            override fun downloadFailed(errorMessage: String, bytesDownloaded: Int?, bytesTotal: Int?) {
//                Log.e(
//                    "DownloadSongService",
//                    "downloadFailed " + errorMessage + " " + bytesDownloaded + " " + bytesTotal
//                )
//                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
//
//            }
//
//            override fun downloadPaused(pauseMessage: String, bytesDownloaded: Int?, bytesTotal: Int?) {
//                Log.e(
//                    "DownloadSongService",
//                    "downloadPaused " + pauseMessage + " " + bytesDownloaded + " " + bytesTotal
//                )
//            }
//
//            override fun downloadPending(bytesDownloaded: Int?, bytesTotal: Int?) {
//                Log.e(
//                    "DownloadSongService",
//                    "downloadPending " + bytesDownloaded + " " + bytesTotal
//                )
//            }
//
//            override fun downloadRunning(bytesDownloaded: Int?, bytesTotal: Int?) {
//                Log.e(
//                    "DownloadSongService",
//                    "downloadRunning " + bytesDownloaded + " " + bytesTotal
//                )
//                if (bytesDownloaded != null && bytesTotal != null) {
//                    progressBar?.progress = bytesDownloaded
//                    progressBar?.max = bytesTotal
//                }
//            }
//
//            override fun downloadSuccessful(bytesDownloaded: Int?, bytesTotal: Int?) {
//                Log.e(
//                    "DownloadSongService",
//                    "downloadSuccessful " + bytesDownloaded + " " + bytesTotal
//                )
//            }
//
//            override fun downloadStopped() {
//                progressBar?.progress = 0
//            }
//
//        })
    }

    private fun getPath(url: String?): String {
        return if (url != null)
            getSaveDir() + Uri.parse(url).lastPathSegment
        else
            ""
    }

    private fun getSaveDir(): String {
        return applicationContext?.filesDir?.absolutePath.toString()
    }

}

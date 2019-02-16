package com.example.yaramobile.downloadmanagerexample

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                startService(
                    DownloadService.getDownloadService(
                        applicationContext,
                        "http://5.239.244.167:1337/lyrics/api/files/bjCbe1h7B3DrLpZmCOwj1RYpgOPL4z8g/33cb19f604cbc1b4a4de95b8c946bac5_hWOMf19fFuc.mp4",
                        getPath("http://5.239.244.167:1337/lyrics/api/files/bjCbe1h7B3DrLpZmCOwj1RYpgOPL4z8g/33cb19f604cbc1b4a4de95b8c946bac5_hWOMf19fFuc.mp4")
                    )
                )
            }
        })

        stop.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                DownloadService.stopDownload()
            }
        })

        DownloadService.setDownloadListener(object : DownloadService.DownloadManagerListener {

            override fun downloadFailed(errorMessage: String, bytesDownloaded: Int?, bytesTotal: Int?) {
                Log.e(
                    "DownloadSongService",
                    "downloadFailed " + errorMessage + " " + bytesDownloaded + " " + bytesTotal
                )
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()

            }

            override fun downloadPaused(pauseMessage: String, bytesDownloaded: Int?, bytesTotal: Int?) {
                Log.e(
                    "DownloadSongService",
                    "downloadPaused " + pauseMessage + " " + bytesDownloaded + " " + bytesTotal
                )
            }

            override fun downloadPending(bytesDownloaded: Int?, bytesTotal: Int?) {
                Log.e(
                    "DownloadSongService",
                    "downloadPending " + bytesDownloaded + " " + bytesTotal
                )
            }

            override fun downloadRunning(bytesDownloaded: Int?, bytesTotal: Int?) {
                Log.e(
                    "DownloadSongService",
                    "downloadRunning " + bytesDownloaded + " " + bytesTotal
                )
                if (bytesDownloaded != null && bytesTotal != null) {
                    progressBar?.progress = bytesDownloaded
                    progressBar?.max = bytesTotal
                }
            }

            override fun downloadSuccessful(bytesDownloaded: Int?, bytesTotal: Int?) {
                Log.e(
                    "DownloadSongService",
                    "downloadSuccessful " + bytesDownloaded + " " + bytesTotal
                )
            }

            override fun downloadStopped() {
                progressBar?.progress = 0
            }

        })
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

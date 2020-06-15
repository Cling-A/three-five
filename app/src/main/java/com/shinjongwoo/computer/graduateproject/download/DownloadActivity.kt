package com.shinjongwoo.computer.graduateproject.download

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.shinjongwoo.computer.graduateproject.R
import kotlinx.android.synthetic.main.download_activity.*
import java.io.File
import java.net.URL

class DownloadActivity : AppCompatActivity() {
    var imgUri : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.download_activity)

        Toast.makeText(applicationContext, "넘어온 값", Toast.LENGTH_LONG).show();

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            val uri = intent.data

            if (uri != null) {
                imgUri = uri.getQueryParameter("imgUri")
                Log.d("abcd", "imgUri 값은 ${imgUri}")

                object : Thread() {
                    override fun run() {
                        val url = URL(imgUri)
                        val bitmap = BitmapFactory.decodeStream(url.openStream())
                        runOnUiThread{
                            downloadImageView.setImageBitmap(bitmap)
                        }
                    }
                }.start()
            }
        }
        else
            Log.d("abcd", "intent 값이 비어있음")

        downloadBtn.setOnClickListener { downloadImage() }
    }

    private fun downloadImage(){
        val direct = File(Environment.getExternalStorageDirectory().toString() + "/download")
        if (!direct.exists()) {
            direct.mkdir()
        } // end of if

        val downloadUri: Uri = Uri.parse(imgUri)
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(downloadUri)

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setTitle("camAIDownloadImage")
            .setVisibleInDownloadsUi(true)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir("/download", "camAIdownload.jpg")

        val reference = downloadManager.enqueue(request)
        Log.d("abcd", reference.toString())
    }
}



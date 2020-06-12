package com.shinjongwoo.computer.graduateproject.download

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.shinjongwoo.computer.graduateproject.R
import com.shinjongwoo.computer.graduateproject.tflite.HttpConnectionUtil
import com.wonderkiln.camerakit.CameraKitImage
import kotlinx.android.synthetic.main.download_activity.*
import org.json.JSONException
import java.net.URL

class DownloadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.download_activity)

        Toast.makeText(applicationContext, "넘어온 값", Toast.LENGTH_LONG).show();

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            val uri = intent.data

            if (uri != null) {
                val imgUri = uri.getQueryParameter("imgUri")
                Log.d("abcd", "imgUri 값은 ${imgUri}")
            }
        }
        else
            Log.d("abcd", "intent 값이 비어있음")
        ViewDownloadImage(downloadImageView).run()
    }
}



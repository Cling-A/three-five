package com.shinjongwoo.computer.graduateproject.download

import android.graphics.BitmapFactory
import android.widget.ImageView
import java.net.URL

class ViewDownloadImage(downloadImageView: ImageView) : Thread() {
    var downloadImageView = downloadImageView
    override fun run() {
        val url =
            URL("https://k.kakaocdn.net/dn/OPNqV/bl2Ej2hDJTd/2Kezsu8BDYIzAGFew6M7e0/kakaolink40_original.jpg")
        val bitmap = BitmapFactory.decodeStream(url.openStream())
        downloadImageView.setImageBitmap(bitmap)
    }
}
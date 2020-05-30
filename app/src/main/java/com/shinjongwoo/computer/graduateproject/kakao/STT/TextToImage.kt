package com.shinjongwoo.computer.graduateproject.kakao.STT

import android.graphics.*
import android.graphics.Shader.TileMode
import android.graphics.LinearGradient
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.shinjongwoo.computer.graduateproject.R
import kotlinx.android.synthetic.main.activity_text_to_image2.*


class TextToImage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_to_image2)
        var baseBitmap = intent.getParcelableExtra<Bitmap>("image")
        //resultImage2.setImageBitmap(textInBitmap(exampleText,baseBitmap));
        resultImage2.setImageBitmap(mark(baseBitmap, exampleText.text as String))

    }

    //텍스트뷰를 사진에 박는 메소드
    fun textInBitmap(overlayView: View, baseBitmap: Bitmap): Bitmap {

        val source = Bitmap.createBitmap(baseBitmap.width, baseBitmap.height, Bitmap.Config.ARGB_8888) as Bitmap
        var canvas = Canvas(source)
        overlayView.draw(canvas)
        val width: Int = source.width
        val height: Int = source.height
        val copy = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        copy.setPixels(pixels, 0, width, 0, 0, width, height)
        val resultBitmap = Bitmap.createBitmap(baseBitmap.width,baseBitmap.height,baseBitmap.config)
        canvas = Canvas(resultBitmap)
        canvas.drawBitmap(baseBitmap,0.toFloat(),0.toFloat(),null)
        canvas.drawBitmap(copy,0.toFloat(), 0.toFloat(), null)


        return resultBitmap



    }
    fun mark(src: Bitmap, watermark: String?): Bitmap? {
        val w = src.width
        val h = src.height
        val shader: Shader = LinearGradient(
            0F,
            0F,
            100F,
            100F,
            Color.TRANSPARENT,
            Color.BLACK,
            TileMode.CLAMP
        )
        val result = Bitmap.createBitmap(w, h, src.config)
        val canvas = Canvas(result)
        canvas.drawBitmap(src, 0f, 0f, null)
        val paint = Paint()
        paint.setColor(Color.WHITE)
        paint.setTextSize(10F)
        paint.setAntiAlias(true)
        paint.setShader(shader)
        paint.setUnderlineText(false)
        canvas.drawText(watermark, 10f, h - 15.toFloat(), paint)
        return result
    }
}

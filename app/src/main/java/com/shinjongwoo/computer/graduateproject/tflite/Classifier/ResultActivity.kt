package com.shinjongwoo.computer.graduateproject.tflite.Classifier

import android.graphics.Bitmap
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.shinjongwoo.computer.graduateproject.R
import com.shinjongwoo.computer.graduateproject.tflite.Classifier.Classifier.Recognition
import kotlinx.android.synthetic.main.result_activity.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class ResultActivity : AppCompatActivity() {
    private var classifier: Classifier? = null
    private val executor: Executor =
        Executors.newSingleThreadExecutor()

    private val MODEL_PATH = "mobilenet_quant_v1_224.tflite"
    private val QUANT = true
    private val LABEL_PATH = "labels.txt"
    private val INPUT_SIZE = 224

    private var bitmap : Bitmap? = null;
    val adapter : ListViewAdapter = ListViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("abcd", "Launch Start")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_activity)
        detectList.adapter = adapter

        initTensorFlowAndLoadModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.execute(Runnable { classifier!!.close() })
    }


    private fun initTensorFlowAndLoadModel() {
        executor.execute(Runnable {
            try {
                bitmap = intent.getParcelableExtra<Bitmap>("bitmap")
                classifier = TensorFlowImageClassifier.create(
                    assets,
                    MODEL_PATH,
                    LABEL_PATH,
                    INPUT_SIZE,
                    QUANT
                )
                distinguishObject();
            } catch (e: Exception) {
                throw RuntimeException("Error initializing TensorFlow!", e)
            }
        })
    }

    fun distinguishObject(){
        val results: List<Recognition> = classifier!!.recognizeImage(bitmap)
        adapter.addItem(bitmap, results.toString())
        resultImage.setImageBitmap(bitmap)  
    }


}

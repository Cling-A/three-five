/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shinjongwoo.computer.graduateproject

import android.graphics.*
import android.media.ImageReader.OnImageAvailableListener
import android.os.Bundle
import android.os.SystemClock
import android.util.Size
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import com.shinjongwoo.computer.graduateproject.customview.OverlayView
import com.shinjongwoo.computer.graduateproject.tflite.Classifier
import com.shinjongwoo.computer.graduateproject.tflite.TFLiteObjectDetectionAPIModel
import java.io.IOException
import java.util.*
import com.shinjongwoo.computer.graduateproject.tflite.Classifier.Recognition
import java.lang.StringBuilder

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
class DetectorActivity(override val desiredPreviewFrameSize: Size?) : MainActivity(), OnImageAvailableListener {
    var trackingOverlay: OverlayView? = null
    private var sensorOrientation: Int? = null
    private var detector: Classifier? = null
    private var lastProcessingTimeMs: Long = 0
    private var rgbFrameBitmap: Bitmap? = null
    private var croppedBitmap: Bitmap? = null
    private var cropCopyBitmap: Bitmap? = null
    private var computingDetection = false
    private var timestamp: Long = 0
    private var frameToCropTransform: Matrix? = null
    private var cropToFrameTransform: Matrix? = null
    private var tracker: MultiBoxTracker? = null

    public override fun onPreviewSizeChosen(size: Size?, rotation: Int) {
        val textSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            TEXT_SIZE_DIP,
            resources.displayMetrics
        )
        tracker = MultiBoxTracker(this)
        var cropSize = TF_OD_API_INPUT_SIZE
        try {
            detector = TFLiteObjectDetectionAPIModel.create(
                assets,
                TF_OD_API_MODEL_FILE,
                TF_OD_API_LABELS_FILE,
                TF_OD_API_INPUT_SIZE,
                TF_OD_API_IS_QUANTIZED
            )
            cropSize = TF_OD_API_INPUT_SIZE
        } catch (e: IOException) {
            e.printStackTrace()
            val toast = Toast.makeText(
                applicationContext, "Classifier could not be initialized", Toast.LENGTH_SHORT
            )
            toast.show()
            finish()
        }
        previewWidth = size!!.width
        previewHeight = size.height
        sensorOrientation = rotation - screenOrientation

        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888)

        cropToFrameTransform = Matrix()
        frameToCropTransform!!.invert(cropToFrameTransform)
        trackingOverlay = findViewById<View>(R.id.tracking_overlay) as OverlayView
        trackingOverlay?.addCallback { canvas ->
            tracker?.draw(canvas)
            if (isDebug) {
                tracker?.drawDebug(canvas)
            }
        }
        tracker?.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation!!)
    }

    override fun processImage() {
        ++timestamp
        val currTimestamp = timestamp
        trackingOverlay?.postInvalidate()

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage()
            return
        }
        computingDetection = true
        rgbFrameBitmap!!.setPixels(
            getRgbBytes(),
            0,
            previewWidth,
            0,
            0,
            previewWidth,
            previewHeight
        )
        readyForNextImage()
        val canvas = Canvas(croppedBitmap)
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null)
        // For examining the actual TF input.

        runInBackground(
            Runnable {
                val startTime = SystemClock.uptimeMillis()
                val results: List<Recognition> =
                    detector?.recognizeImage(croppedBitmap) as List<Recognition>
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime
                cropCopyBitmap = Bitmap.createBitmap(croppedBitmap)
                val canvas = Canvas(cropCopyBitmap)
                val paint = Paint()
                paint.color = Color.RED
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.0f
                var minimumConfidence =
                    MINIMUM_CONFIDENCE_TF_OD_API
                minimumConfidence = when (MODE) {
                    DetectorMode.TF_OD_API -> MINIMUM_CONFIDENCE_TF_OD_API
                }
                val mappedRecognitions: MutableList<Recognition> =
                    LinkedList<Recognition>()
                for (result in results) {
                    val location: RectF = result.getLocation()
                    if (location != null && result.getConfidence() >= minimumConfidence) {
                        canvas.drawRect(location, paint)
                        cropToFrameTransform!!.mapRect(location)
                        result.setLocation(location)
                        mappedRecognitions.add(result)
                    }
                }
                tracker?.trackResults(mappedRecognitions, currTimestamp)
                trackingOverlay?.postInvalidate()
                computingDetection = false
                runOnUiThread {
                    val sb = StringBuilder()
                    showCropInfo(
                        cropCopyBitmap?.getWidth()
                            .toString() + "x" + cropCopyBitmap?.getHeight()
                    )
                    showInference(lastProcessingTimeMs.toString() + "ms")
                }
            })
    }

    override val layoutId: Int
        protected get() = R.layout.main_fragment

    override fun onClick(v: View) {}

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum class DetectorMode {
        TF_OD_API
    }

    override fun setUseNNAPI(isChecked: Boolean) {
        runInBackground(Runnable { detector?.setUseNNAPI(isChecked) })
    }

    override fun setNumThreads(numThreads: Int) {
        runInBackground(Runnable { detector?.setNumThreads(numThreads) })
    }

    companion object {

        // Configuration values for the prepackaged SSD model.
        private const val TF_OD_API_INPUT_SIZE = 300
        private const val TF_OD_API_IS_QUANTIZED = true
        private const val TF_OD_API_MODEL_FILE = "detect.tflite"
        private const val TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt"
        private val MODE = DetectorMode.TF_OD_API

        // Minimum detection confidence to track a detection.
        private const val MINIMUM_CONFIDENCE_TF_OD_API = 0.5f
        private const val MAINTAIN_ASPECT = false
        protected val desiredPreviewFrameSize = Size(640, 480)
        private const val SAVE_PREVIEW_BITMAP = false
        private const val TEXT_SIZE_DIP = 10f
    }
}
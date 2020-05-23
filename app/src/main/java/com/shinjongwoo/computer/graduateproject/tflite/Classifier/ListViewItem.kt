package com.shinjongwoo.computer.graduateproject.tflite.Classifier

import android.graphics.Bitmap

class ListViewItem  {
    var image: Bitmap? = null
    var text: String? = null

    override fun toString(): String {
        if(image == null)
            return "image is null"
        else
            return "text : $text / image is not null";
    }
}
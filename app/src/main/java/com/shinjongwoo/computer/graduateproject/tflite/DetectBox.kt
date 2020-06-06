package com.shinjongwoo.computer.graduateproject.tflite

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.shinjongwoo.computer.graduateproject.R
import com.shinjongwoo.computer.graduateproject.tflite.ResultActivity
import org.w3c.dom.Text

class DetectBox(var name: String?,
                var uuid: String,
                context: Context,
                x: Float,
                y: Float,
                width: Int,
                height: Int) {
    var box = Button(context)
    var state : String = "green"
    var text = TextView(context)
    init {
        box.minWidth = 10
        box.minHeight = 10
        Log.d("abcd", "DetectBox name : 인자 값으로 $name 이 들어왔고, Text 안에는 ${text.text.toString()}이 들어왔다.")
        Log.d("abcd", "DetectBox x : 인자 값으로 $x 이 들어왔다.")
        Log.d("abcd", "DetectBox y : 인자 값으로 $y 이 들어왔다.")
        Log.d("abcd", "DetectBox width : 인자 값으로 $width 이 들어왔다.")
        Log.d("abcd", "DetectBox height : 인자 값으로 $height 이 들어왔다.")
        text.text = name
        text.setTextColor(0xFFFFFFFF.toInt())
        setSize(width,height)
        Log.d("abcd", "minWidth : " + box.minWidth)
        Log.d("abcd", "minHeight : " + box.minHeight)
        Log.d("abcd", "Width : " + width)
        Log.d("abcd", "Height : " + height)
        move(x,y)
        setGreen()
        box.setOnClickListener {
            if(state == "green"){
                setRed()
                Log.d("abcd","green to red")
            }
            else {
                setGreen()
                Log.d("abcd", "red to green")
            }

            Log.d("abcd","리스너 작동")
        }
    }

    fun move(x: Float, y:Float){
        box.x = x
        box.y = y
        text.x = x + 8
        text.y = y - box.height
    }
    fun setSize(width: Int, height: Int){
        box.width = width
        box.height = height
    }
    fun setGreen(){
        box.setBackgroundResource(R.drawable.green_border)
        text.setBackgroundColor(0xff99cc00.toInt())
        state = "green"
    }
    fun setRed(){
        box.setBackgroundResource(R.drawable.red_border)
        text.setBackgroundColor(0xffCC0000.toInt())
        state = "red"
    }
    fun addView(param : ViewGroup.LayoutParams,
                resultActivity: ResultActivity){
        resultActivity.addContentView(box,param)
        resultActivity.addContentView(text,param)
    }

    fun getText() : String? {
        return text.text.toString();
    }
}
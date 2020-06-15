package com.shinjongwoo.computer.graduateproject.tflite

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.shinjongwoo.computer.graduateproject.R

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
        text.text = name
        text.setTextColor(0xFFFFFFFF.toInt())
        setSize(width,height)
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
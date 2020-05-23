package com.shinjongwoo.computer.graduateproject.tflite.Classifier

import android.app.Dialog
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.shinjongwoo.computer.graduateproject.R

class CustomDialog(context: Context) {
    private val context: Context

    // 호출할 다이얼로그 함수를 정의한다.
    fun callFunction(STTtext : String) {
        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        val dlg = Dialog(context)

        // 액티비티의 타이틀바를 숨긴다.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.stt_dialog)

        // 커스텀 다이얼로그를 노출한다.
        dlg.show()



        // record Btn

        /*
        // cancel Btn
        cancelButton.setOnClickListener(object : OnClickListener() {
            fun onClick(view: View?) {
                Toast.makeText(context, "취소 했습니다.", Toast.LENGTH_SHORT).show()
                // 커스텀 다이얼로그를 종료한다.
                dlg.dismiss()
            }
        })

         */
    }

    init {
        this.context = context
    }

}
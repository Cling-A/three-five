package com.shinjongwoo.computer.graduateproject

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.shinjongwoo.computer.graduateproject.kakao.STT.STTActivity
import com.shinjongwoo.computer.graduateproject.kakao.friends.FriendsActivity
import kotlinx.android.synthetic.main.choose_activity.*


class ChooseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("abcd","Main Start")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_activity)
        friends_btn.setOnClickListener{ changedView1() }
        stt_btn.setOnClickListener{ changedView2() }

    }

    fun changedView1(){
        Log.d("abcd","Change Start")
        val mainIntent = Intent(this@ChooseActivity, FriendsActivity::class.java)
        startActivity(mainIntent)
        Log.d("abcd","Change End")
    }

    fun changedView2(){
        Log.d("abcd","Change Start")
        val mainIntent = Intent(this@ChooseActivity, STTActivity::class.java)
        startActivity(mainIntent)
        Log.d("abcd","Change End")
    }

}

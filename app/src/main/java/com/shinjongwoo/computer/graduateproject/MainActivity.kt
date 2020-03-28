package com.shinjongwoo.computer.graduateproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.shinjongwoo.computer.graduateproject.kakao.LoginActivity
import com.shinjongwoo.computer.graduateproject.ui.main.MainFragment


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("abcd","Main Start")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }

    fun changedView(){
        Log.d("abcd","Change Start")
        val mainIntent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(mainIntent)
        Log.d("abcd","Change End")
    }

}

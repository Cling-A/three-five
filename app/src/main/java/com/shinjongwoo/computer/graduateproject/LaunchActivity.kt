package com.shinjongwoo.computer.graduateproject

import android.Manifest
import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


class LaunchActivity : AppCompatActivity() {

    val PERMISSIONS_REQUEST_CODE = 1000
    var PERMISSIONS = arrayOf<String>(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE  
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launch_activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) { //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE)
            } else {
                val mainIntent = Intent(this@LaunchActivity, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            }
        }
    }


    private fun hasPermissions(permissions: Array<String>): Boolean {
        var result: Int
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (perms in permissions) {
            result = ContextCompat.checkSelfPermission(this, perms)
            if (result == PackageManager.PERMISSION_DENIED) { //허가 안된 퍼미션 발견
                return false
            }
        }
        //모든 퍼미션이 허가되었음
        return true
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> if (grantResults.size > 0) {
                val cameraPermissionAccepted = (grantResults[0]
                        == PackageManager.PERMISSION_GRANTED)
                val diskPermissionAccepted = (grantResults[1]
                        == PackageManager.PERMISSION_GRANTED)
                if (!cameraPermissionAccepted || !diskPermissionAccepted) showDialogForPermission("앱을 실행하려면 카메라 권한이 필요합니다.") else {
                    val mainIntent = Intent(this@LaunchActivity, MainActivity::class.java)
                    startActivity(mainIntent)
                    finish()
                }
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private fun showDialogForPermission(msg: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@LaunchActivity)
        builder.setTitle("알림")
        builder.setMessage(msg)
        builder.setCancelable(false)
        builder.setPositiveButton("예",
            DialogInterface.OnClickListener { dialog, id -> requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE) })
        builder.setNegativeButton("아니오",
            DialogInterface.OnClickListener { dialog, id -> finish() })
        builder.create().show()
    }

}

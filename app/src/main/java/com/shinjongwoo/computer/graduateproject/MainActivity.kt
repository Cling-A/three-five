package com.shinjongwoo.computer.graduateproject
import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_activity.*


class MainActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {
    private var REQUIRED_PERMISSIONS = arrayOf( 
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private var mCameraPreview: CameraPreview? = null

    // (참고로 Toast에서는 Context가 필요했습니다.)
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 상태바를 안보이도록 합니다.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // 화면 켜진 상태를 유지합니다.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setContentView(R.layout.main_activity)


        // 런타임 퍼미션 완료될때 까지 화면에서 보이지 않게 해야합니다.
        surfaceView?.visibility = View.GONE
        shotBtn.setOnClickListener { mCameraPreview?.takePicture() }
        changeButton.setOnClickListener {
            if(CAMERA_FACING != Camera.CameraInfo.CAMERA_FACING_BACK)
                CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_FRONT
            else
                CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK
        }
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            val cameraPermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            val writeExternalStoragePermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (cameraPermission == PackageManager.PERMISSION_GRANTED
                && writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED
            ) {
                startCamera()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        REQUIRED_PERMISSIONS[0]
                    )
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        REQUIRED_PERMISSIONS[1]
                    )
                ) {
                    Snackbar.make(
                        container, "이 앱을 실행하려면 카메라와 외부 저장소 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("확인") {
                        ActivityCompat.requestPermissions(
                            this@MainActivity, REQUIRED_PERMISSIONS,
                            PERMISSIONS_REQUEST_CODE
                        )
                    }.show()
                } else {
                    // 2. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                    // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                    ActivityCompat.requestPermissions(
                        this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE
                    )
                }
            }
        } else {
            val snackbar = Snackbar.make(
                container, "디바이스가 카메라를 지원하지 않습니다.",
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setAction("확인") { snackbar.dismiss() }
            snackbar.show()
        }
    }

    fun startCamera() {

        // Create the Preview view and set it as the content of this Activity.
        mCameraPreview =
            surfaceView?.let { CameraPreview(this, this, CAMERA_FACING, it) }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grandResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_CODE && grandResults.size == REQUIRED_PERMISSIONS.size) {
            var check_result = true
            for (result in grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false
                    break
                }
            }
            if (check_result) {
                startCamera()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        REQUIRED_PERMISSIONS[0]
                    )
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        REQUIRED_PERMISSIONS[1]
                    )
                ) {
                    Snackbar.make(
                        container, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("확인") { finish() }.show()
                } else {
                    Snackbar.make(
                        container, "설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("확인") { finish() }.show()
                }
            }
        }
    }

    companion object {
        private const val TAG = "android_camera_example"
        private const val PERMISSIONS_REQUEST_CODE = 100
        private var CAMERA_FACING =
            Camera.CameraInfo.CAMERA_FACING_BACK // Camera.CameraInfo.CAMERA_FACING_FRONT
    }
}

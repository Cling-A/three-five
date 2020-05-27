package com.shinjongwoo.computer.graduateproject.tflite.classifier

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kakao.friends.AppFriendContext
import com.kakao.friends.AppFriendOrder
import com.kakao.friends.response.AppFriendsResponse
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.kakaotalk.callback.TalkResponseCallback
import com.kakao.kakaotalk.response.MessageSendResponse
import com.kakao.kakaotalk.v2.KakaoTalkService
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import com.kakao.network.storage.ImageUploadResponse
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import com.shinjongwoo.computer.graduateproject.R
import kotlinx.android.synthetic.main.result_activity.*
import java.io.File

class ResultActivity : AppCompatActivity() {
    // permission
    val RECORD_REQUEST_CODE = 1000
    val STORAGE_REQUEST_CODE = 1000

    var capturedImage : Bitmap ?= null
    var STTtext : String  = "제목 부분"
    var imageUrl : String ?= null
    var uuids =  mutableListOf<String>();
    var convertedImageUrl : String ?= null
    val templateId = "25313"
    val templateArgs: MutableMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("abcd", "Launch Start")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_activity)
//        capturedImage = intent.getParcelableExtra<Bitmap>("bitmap")
//        STTtext = intent.getStringExtra("text")
        imageUrl = intent.getStringExtra("imageUrl")

//        resultText.text = STTtext
//        resultImage.setImageBitmap(capturedImage)

        // init Part
        initUUIDs()
        initImage()
        setupPermissions()

        // Listener Part
        sendBtn.setOnClickListener{sendMyTemplate()}
        sttBtn.setOnClickListener{onClickHandler()}
        recommendBtn.setOnClickListener{sendSelected()}
    }

    override fun onDestroy() {
        super.onDestroy()
        SpeechRecognizerManager.getInstance().finalizeLibrary()
    }

    private fun onClickHandler() {
        val dlg = CustomDialog(this)
        dlg.callFunction(STTtext!!);
    }

    // KakaoTalk Message function
    private fun sendSelected() {
        var templateArgs: MutableMap<String, String> = HashMap()
        KakaoLinkService.getInstance()
            .sendCustom(
                this,
                "25314",
                templateArgs,
                null,
                object : ResponseCallback<KakaoLinkResponse>() {
                    override fun onFailure(errorResult: ErrorResult) {
                        Log.e("KAKAO_API", "카카오링크 보내기 실패: $errorResult")
                    }

                    override fun onSuccess(result: KakaoLinkResponse) {
                        Log.i("KAKAO_API", "카카오링크 보내기 성공")

                        // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                        Log.w(
                            "KAKAO_API",
                            "warning messages: " + result.warningMsg
                        )
                        Log.w(
                            "KAKAO_API",
                            "argument messages: " + result.argumentMsg
                        )
                    }
                })
    }

    private fun sendMyTemplate(){
        // 커스텀 템플릿으로 친구에게 보내기
        KakaoTalkService.getInstance()
            .sendMessageToFriends(
                uuids,
                templateId,
                templateArgs,
                object : TalkResponseCallback<MessageSendResponse?>() {
                    override fun onNotKakaoTalkUser() {
                        Log.e("KAKAO_API", "카카오톡 사용자가 아님")
                    }

                    override fun onSessionClosed(errorResult: ErrorResult) {
                        Log.e("KAKAO_API", "세션이 닫혀 있음: $errorResult")
                    }

                    override fun onFailure(errorResult: ErrorResult) {
                        Log.e("KAKAO_API", "친구에게 보내기 실패: $errorResult")
                    }

                    override fun onSuccess(result : MessageSendResponse?) {
                        var successCount = 0
                        var failureCount = 0
                        if (result!!.successfulReceiverUuids() != null) {
                            Log.i("KAKAO_API", "친구에게 보내기 성공")
                            Log.d(
                                "KAKAO_API",
                                "전송에 성공한 대상: " + result.successfulReceiverUuids()
                            )
                            successCount += result.successfulReceiverUuids()!!.size
                        }
                        if (result.failureInfo() != null) {
                            Log.e("KAKAO_API", "일부 사용자에게 메시 보내기 실패")
                            for (failureInfo in result.failureInfo()!!) {
                                Log.d("KAKAO_API", "code: " + failureInfo.code())
                                Log.d("KAKAO_API", "msg: " + failureInfo.msg())
                                Log.d(
                                    "KAKAO_API",
                                    "failure_uuids: " + failureInfo.receiverUuids()
                                )
                            }
                            failureCount += result.successfulReceiverUuids()!!.size
                        }

                        Toast.makeText(getApplicationContext(), "사진 전송이 완료되었습니다.\n전송에 성공한 대상 : " + successCount + "\n전송에 실패한 대상 : " + failureCount, Toast.LENGTH_LONG).show();

                    }
                })
    }

    // KakaoTalk Init function
    private fun initImage(){
        val imageFile = File(imageUrl)

        KakaoLinkService.getInstance()
            .uploadImage(this, true, imageFile, object : ResponseCallback<ImageUploadResponse>() {
                override fun onFailure(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "이미지 업로드 실패: $errorResult")
                }

                override fun onSuccess(result: ImageUploadResponse) {
                    Log.i("KAKAO_API", "이미지 업로드 성공")
                    Log.d("KAKAO_API", "URL: " + result.original.url)
                    convertedImageUrl = result.original.url
                    templateArgs["THU"] = result.original.url
                    templateArgs["des_main"] = "제목부분"
                }
            })
    }

    private fun initUUIDs(){
        var context = AppFriendContext(AppFriendOrder.NICKNAME, 0, 100, "asc")
        uuids =  mutableListOf<String>();
        Log.d("abcd", "thread proceed 2")
        KakaoTalkService.getInstance()
            .requestAppFriends(context, object : TalkResponseCallback<AppFriendsResponse?>() {
                override fun onNotKakaoTalkUser() {
                    Log.e("KAKAO_API", "카카오톡 사용자가 아님")
                }

                override fun onSessionClosed(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "세션이 닫혀 있음: $errorResult")
                }

                override fun onFailure(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "친구 조회 실패: $errorResult")
                }

                override fun onSuccess(result: AppFriendsResponse?) {
                    Log.i("KAKAO_API", "친구 조회 성공")
                    for (friend in result!!.friends)
                        uuids.add(friend.uuid)
                }
            })
        }


    // STT Sdk
    private fun setupPermissions(){
        var permission_audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        var permission_storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(permission_audio != PackageManager.PERMISSION_GRANTED) {
            Log.d("STT", "Permission to recode denied in setupPermissions 1")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_REQUEST_CODE)
        } else if(permission_storage != PackageManager.PERMISSION_GRANTED) {
            Log.d("STT", "Permission to recode denied in setupPermissions 2")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE)
        } else {
            //본문실행
            //startUsingSpeechSDK()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }

            STORAGE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
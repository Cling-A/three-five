package com.shinjongwoo.computer.graduateproject.tflite

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
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
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.usermgmt.response.model.Profile
import com.kakao.usermgmt.response.model.UserAccount
import com.kakao.util.OptionalBoolean
import com.shinjongwoo.computer.graduateproject.R
import kotlinx.android.synthetic.main.result_activity.*
import org.json.JSONArray
import java.io.File

class ResultActivity : AppCompatActivity(), View.OnClickListener {
    // permission
    val RECORD_REQUEST_CODE = 1000
    val STORAGE_REQUEST_CODE = 1000

    var capturedImage : Bitmap ?= null
    var imageUrl : String ?= null
    var uuids =  mutableListOf<String>()
    var convertedImageUrl : String ?= null
    val templateArgs: MutableMap<String, String> = HashMap()

    val idUuid : MutableMap<String, String> = HashMap()
    val idNickname : MutableMap<String, String> = HashMap()

    val boxs = mutableListOf<DetectBox>()

    var inflater : LayoutInflater? = null
    var frameLayout : FrameLayout ? = null
    var param = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_activity)

        imageUrl = intent.getStringExtra("imageUrl")
        capturedImage = BitmapFactory.decodeFile(imageUrl)
        resultImage.setImageBitmap(capturedImage)

        // init Part
        myInformation()
        initImage()
        setupPermissions()

        // Listener Part
        sendBtn.setOnClickListener{
            uuids = mutableListOf<String>()
            for(i in boxs.iterator()){
                if(i.state == "green"){
                    if(!i.uuid.equals("empty"))
                        uuids.add(i.uuid)
                    Log.d("KAKAO_API",i.uuid)
                }
            }
            sendMyTemplate()
        }
        sttBtn.setOnClickListener(this)
        recommendBtn.setOnClickListener{sendSelected()}
    }

    override fun onDestroy() {
        super.onDestroy()
        SpeechRecognizerManager.getInstance().finalizeLibrary()
    }

    override fun onStart() {
        super.onStart()
        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        frameLayout = inflater!!.inflate(R.layout.result_activity,null) as FrameLayout
        frameLayout!!.setBackgroundColor(Color.parseColor("#99000000"))
    }

    private fun initDetectBox(){
        val faces = JSONArray(intent.getStringExtra("faces"))

        for(i in 0 until faces.length()){
            val facesIterator = faces.getJSONObject(i)

            var ratio = 1.0f
            val xRatio = resultImage.width.toFloat() / capturedImage!!.width
            val yRatio = resultImage.height.toFloat() / capturedImage!!.height

            if(xRatio < yRatio)
                ratio = xRatio
            else
                ratio = yRatio

            val convertedWidth = capturedImage!!.width * ratio
            val convertedHeight = capturedImage!!.height * ratio

            Log.d("abcd", "idNickname : " + idNickname.toString())
            Log.d("abcd", "idUuid: " + idUuid.toString())

            Log.d("abcd", "faceIterator : " + facesIterator.getString("name"))

            val nickname = idNickname.get(facesIterator.getString("name"))
            var uuid = idUuid.get(facesIterator.getString("name"))

            if(uuid == null)
                uuid = "empty"

            var textView = findViewById<View>(R.id.photoRe) as TextView

            var detectBox = DetectBox(nickname,
                uuid,
                baseContext,
                facesIterator.getInt("x") * ratio + (resultImage.width.toFloat() - convertedWidth ) / 2,
                facesIterator.getInt("y") * ratio + (resultImage.height.toFloat() - convertedHeight )  / 2 + textView.height,
                (facesIterator.getInt("w") * ratio).toInt(),
                (facesIterator.getInt("h") * ratio).toInt()
            )
            detectBox.addView(param, this)
            boxs.add(detectBox)
        }

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
                "25313",
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
                        if(errorResult.errorCode == -10)
                            Toast.makeText(getApplicationContext(), "하루 사용량을 초과하였습니다. 다음 날 시도해주시길 바랍니다.", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(), "에러가 발생했습니다. 관리자에게 문의해주세요.", Toast.LENGTH_LONG).show();
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
        sendBtn.isEnabled = false
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
                    templateArgs["imgUri"] = result.original.url
                    sendBtn.isEnabled = true
                }
            })
    }

    private fun myInformation(){
        UserManagement.getInstance()
            .me(object : MeV2ResponseCallback() {
                override fun onSessionClosed(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "세션이 닫혀 있음: $errorResult")
                }

                override fun onFailure(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "사용자 정보 요청 실패: $errorResult")
                }

                override fun onSuccess(result: MeV2Response) {
                    Log.i("KAKAO_API", "사용자 아이디: " + result.id)
                    setIdNickname(result.id.toString(), result.kakaoAccount.profile.nickname)
                    runOnUiThread{
                        initUUIDs()
                    }
                }
            })
    }

    private fun initUUIDs(){
        var context = AppFriendContext(AppFriendOrder.NICKNAME, 0, 100, "asc")
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
                    for (friend in result!!.friends) {
                        setIdNickname(friend.id.toString(), friend.profileNickname)
                        idUuid[friend.id.toString()] = friend.uuid
                    }
                    runOnUiThread{
                        initDetectBox()
                    }
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

    override fun onClick(v: View?) {
        when(v?.id) {
            sttBtn.id -> {
                Log.d("abcd", "sttBtn clicked")
                val dlg = CustomDialog(this)
                dlg.setOnOKClickedListener{ content ->
                    resultImage.setImageBitmap(content)
                    initImage()
                }
                dlg.callFunction(imageUrl!!);
            }
        }
    }

    @Synchronized fun setIdNickname(key : String, value : String){
        idNickname[key] = value
    }
}
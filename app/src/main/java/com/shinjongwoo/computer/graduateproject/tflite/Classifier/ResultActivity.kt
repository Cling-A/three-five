package com.shinjongwoo.computer.graduateproject.tflite.Classifier

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.kakao.message.template.*
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import com.kakao.network.storage.ImageUploadResponse
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import kotlinx.android.synthetic.main.result_activity.*
import com.shinjongwoo.computer.graduateproject.R
import kotlinx.android.synthetic.main.stt_activity.*
import kotlinx.android.synthetic.main.stt_dialog.*
import java.io.File

class ResultActivity : AppCompatActivity() {
    // permission
    val RECORD_REQUEST_CODE = 1000
    val STORAGE_REQUEST_CODE = 1000

    var capturedImage : Bitmap ?= null
    var STTtext : String  ?= null
    var params : TemplateParams ?= null
    var imageUrl : String ?= null
    var uuids =  mutableListOf<String>();
    var convertedImageUrl : String ?= null
    val templateId = "25313"
    val templateArgs: MutableMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("abcd", "Launch Start")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_activity)
        capturedImage = intent.getParcelableExtra<Bitmap>("bitmap")
        STTtext = intent.getStringExtra("text")
        imageUrl = intent.getStringExtra("imageUrl")
        params = makeFeedTemplateMessage()

        resultText.text = STTtext
        resultImage.setImageBitmap(capturedImage)

        initUUIDs()
        initImage()

        SpeechRecognizerManager.getInstance().initializeLibrary(this)
        val builder =
            SpeechRecognizerClient.Builder().setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB)

        setupPermissions()


        sttBtn.setOnClickListener{onClickHandler()}

        btn1.setOnClickListener{sendMyTemplate()}
        btn2.setOnClickListener{sendToMe()}
        btn3.setOnClickListener{sendAutomatically()}

    }

    override fun onDestroy() {
        super.onDestroy()
        SpeechRecognizerManager.getInstance().finalizeLibrary()
    }

    private fun onClickHandler() {
        val customDialog : CustomDialog = CustomDialog(this)
        customDialog.callFunction(STTtext)
        startUsingSpeechSDK()
    }

    // KakaoTalk Message function
    private fun sendSelected() {
        KakaoLinkService.getInstance()
            .sendDefault(this, params, object : ResponseCallback<KakaoLinkResponse>() {
                override fun onFailure(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "카카오링크 공유 실패: $errorResult")
                }

                override fun onSuccess(result: KakaoLinkResponse) {
                    Log.i("KAKAO_API", "카카오링크 공유 성공")
                    // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                    Log.w("KAKAO_API", "warning messages: " + result.warningMsg)
                    Log.w("KAKAO_API", "argument messages: " + result.argumentMsg)
                }
            })
    }

    private fun sendToMe() {
        KakaoTalkService.getInstance()
            .requestSendMemo(object : TalkResponseCallback<Boolean?>() {
                override fun onNotKakaoTalkUser() {
                    Log.e("KAKAO_API", "카카오톡 사용자가 아님")
                }

                override fun onSessionClosed(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "세션이 닫혀 있음: $errorResult")
                }

                override fun onFailure(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "나에게 보내기 실패: $errorResult")
                }

                override fun onSuccess(result: Boolean?) {
                    Log.i("KAKAO_API", "나에게 보내기 성공")
                }
            }, params)
    }

    private fun sendAutomatically(){
        // 기본 템플릿으로 친구에게 보내기
        KakaoTalkService.getInstance()
            .sendMessageToFriends(
                uuids,
                params!!,
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

                    override fun onSuccess(result: MessageSendResponse?) {
                        if (result?.successfulReceiverUuids() != null) {
                            Log.i("KAKAO_API", "친구에게 보내기 성공")
                            Log.d(
                                "KAKAO_API",
                                "전송에 성공한 대상: " + result.successfulReceiverUuids()
                            )
                        }
                        if (result?.failureInfo() != null) {
                            Log.e("KAKAO_API", "일부 사용자에게 메시 보내기 실패")
                            for (failureInfo in result?.failureInfo()!!) {
                                Log.d("KAKAO_API", "code: " + failureInfo.code())
                                Log.d("KAKAO_API", "msg: " + failureInfo.msg())
                                Log.d(
                                    "KAKAO_API",
                                    "failure_uuids: " + failureInfo.receiverUuids()
                                )
                            }
                        }
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
                        if (result!!.successfulReceiverUuids() != null) {
                            Log.i("KAKAO_API", "친구에게 보내기 성공")
                            Log.d(
                                "KAKAO_API",
                                "전송에 성공한 대상: " + result.successfulReceiverUuids()
                            )
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
                        }
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

    private fun makeFeedTemplateMessage() : TemplateParams {
        return FeedTemplate
            .newBuilder(
                ContentObject.newBuilder(
                    "디저트 사진",
                    "http://mud-kage.kakao.co.kr/dn/NTmhS/btqfEUdFAUf/FjKzkZsnoeE4o19klTOVI1/openlink_640x640s.jpg",
                    LinkObject.newBuilder()
                        .setWebUrl("https://www.naver.com")
                        .setMobileWebUrl("https://www.google.com")
                        .build()
                )
                    .build()
            )
            .addButton(
                ButtonObject(
                    "웹에서 보기",
                    LinkObject.newBuilder()
                        .setWebUrl("https://www.naver.com")
                        .setMobileWebUrl("https://www.google.com")
                        .build()
                )
            )
            .build()
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


    private fun startUsingSpeechSDK(){
        Toast.makeText(this, "Start Newton", Toast.LENGTH_SHORT).show()

        //SDK 초기화
        SpeechRecognizerManager.getInstance().initializeLibrary(this)

        if(recordBtn == null)
            Log.d("abcd", "recordBtn is null")
        else
            Log.d("abcd", "recordBtn is not null")
        //버튼 클릭
        recordBtn.setOnClickListener {
            //클라이언트 생성
            val builder = SpeechRecognizerClient.Builder().setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB)
            val client = builder.build()

            //Callback
            client.setSpeechRecognizeListener(object : SpeechRecognizeListener {
                //콜백함수들
                override fun onReady() {
                    Log.d("STT", "모든 하드웨어 및 오디오 서비스가 준비되었습니다.")
                }

                override fun onBeginningOfSpeech() {
                    Log.d("STT", "사용자가 말을 하기 시작했습니다.")
                }

                override fun onEndOfSpeech() {
                    Log.d("STT", "사용자의 말하기가 끝이 났습니다. 데이터를 서버로 전달합니다.")
                }

                override fun onPartialResult(partialResult: String?) {
                    //현재 인식된 음성테이터 문자열을 출력해 준다. 여러번 호출됨. 필요에 따라 사용하면 됨.
                    //Log.d(TAG, "현재까지 인식된 문자열:" + partialResult)
                }

                /*
                최종결과 - 음성입력이 종료 혹은 stopRecording()이 호출되고 서버에 질의가 완료되고 나서 호출됨
                Bundle에 ArrayList로 값을 받음. 신뢰도가 높음 것 부터...
                 */
                override fun onResults(results: Bundle?) {
                    val texts = results?.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS)
                    val confs = results?.getIntegerArrayList(SpeechRecognizerClient.KEY_CONFIDENCE_VALUES)

                    Log.d("STT", texts?.get(0).toString())
                    //정확도가 높은 첫번째 결과값을 텍스트뷰에 출력
                    runOnUiThread {
                        STTtext = texts?.get(0)
                    }


                }

                override fun onAudioLevel(audioLevel: Float) {
                    Log.d("STT", "Audio Level(0~1): " + audioLevel.toString())
                }

                override fun onError(errorCode: Int, errorMsg: String?) {
                    //에러 출력 해 봄
                    Log.d("STT", "Error: " + errorMsg)
                }
                override fun onFinished() {
                }
            })

            //음성인식 시작함
            client.startRecording(true)
        }


    }


}



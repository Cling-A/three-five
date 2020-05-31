package com.shinjongwoo.computer.graduateproject.tflite

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.*
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import com.shinjongwoo.computer.graduateproject.R
import kotlinx.android.synthetic.main.stt_dialog.*


class CustomDialog(context: Context) {
    private val context: Context = context
    private val dlg = Dialog(context)
    private lateinit var recordBtn : ImageButton
    private lateinit var exitBtn : Button
    private lateinit var submitBtn : Button
    private var client : SpeechRecognizerClient ?= null

    // 호출할 다이얼로그 함수를 정의한다.
    fun callFunction(STTtext : String) {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(R.layout.stt_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴
//        dlg.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함


        SpeechRecognizerManager.getInstance().initializeLibrary(context)
        val builder =
            SpeechRecognizerClient.Builder().setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB)
        client = builder.build()


        recordBtn = dlg.findViewById(R.id.recordBtn)
        exitBtn = dlg.findViewById(R.id.exitBtn)
        submitBtn = dlg.findViewById(R.id.submitBtn)


        startUsingSpeechSDK()
        exitBtn.setOnClickListener {
            dlg.dismiss()
        }

        submitBtn.setOnClickListener{
            dlg.dismiss()
        }

        // 커스텀 다이얼로그를 노출한다.
        dlg.show()
    }

    private fun startUsingSpeechSDK(){
        //버튼 클릭
        recordBtn.setOnClickListener {
            //클라이언트 생성

            //Callback
            client!!.setSpeechRecognizeListener(object : SpeechRecognizeListener {
                //콜백함수들
                override fun onReady() {
                    Log.d("STT", "모든 하드웨어 및 오디오 서비스가 준비되었습니다.")
                    // TODO 아이콘 바꾸기
                    dlg.explainTxt.text="준비가 되었습니다."
                }

                override fun onBeginningOfSpeech() {
                    Log.d("STT", "사용자가 말을 하기 시작했습니다.")
                    // TODO 아이콘 바꾸기 (실행중이라는 직관적인 것으로)
                    dlg.explainTxt.text = "인식 중 입니다."
                }

                override fun onEndOfSpeech() {
                    Log.d("STT", "사용자의 말하기가 끝이 났습니다. 데이터를 서버로 전달합니다.")
                    // TODO 아이콘 바꾸기 (기존 것으로)
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

                    dlg.explainTxt.text = "이것이 맞나요?"
                    dlg.resultTxt.setText(texts?.get(0).toString())
                }

                override fun onAudioLevel(audioLevel: Float) {
//                    Log.d("STT", "Audio Level(0~1): " + audioLevel.toString())
                }

                override fun onError(errorCode: Int, errorMsg: String?) {
                    //에러 출력 해 봄
                    Log.d("STT", "Error: " + errorMsg)
                }
                override fun onFinished() {
                }
            })

            //음성인식 시작함
            client!!.startRecording(true)
        }
    }

// renInUiThread
}
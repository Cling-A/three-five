package com.shinjongwoo.computer.graduateproject.tflite

import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import com.shinjongwoo.computer.graduateproject.R
import kotlinx.android.synthetic.main.stt_dialog.*
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream

class CustomDialog(context: Context) {
    private val context: Context = context
    private val dlg = Dialog(context)

    private lateinit var recordBtn : ImageButton
    private lateinit var firstExitBtn : Button
    private lateinit var firstSubmitBtn : Button
    private lateinit var secondExitBtn : Button
    private lateinit var secondSubmitBtn : Button

    private lateinit var sttLayout: LinearLayout
    private lateinit var resultLayout: LinearLayout

    private var bitmap : Bitmap ? = null

    private var client : SpeechRecognizerClient ?= null
    private var outputStream: FileOutputStream? = null
    private lateinit var listener : MyDialogOKClickedListener

    private var paint : Paint?=null
    private var color : Color ?= null

    private var oldXvalue = 0f
    private var oldYvalue = 0f

    private var size = 10f



    // 호출할 다이얼로그 함수를 정의한다.
    fun callFunction(imageUrl : String) {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(R.layout.stt_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴
        dlg.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

        SpeechRecognizerManager.getInstance().initializeLibrary(context)
        val builder =
            SpeechRecognizerClient.Builder().setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB)
        client = builder.build()

        recordBtn = dlg.findViewById(R.id.recordBtn)
        firstSubmitBtn = dlg.findViewById(R.id.firstSubmitBtn)
        firstExitBtn = dlg.findViewById(R.id.firstExitBtn)
        secondExitBtn = dlg.findViewById(R.id.secondExitBtn)
        secondSubmitBtn = dlg.findViewById(R.id.secondSubmitBtn)
        sttLayout = dlg.findViewById(R.id.sttLayout)
        resultLayout = dlg.findViewById(R.id.resultLayout)

        startUsingSpeechSDK()
        firstExitBtn.setOnClickListener {
            dlg.dismiss()
        }
        secondExitBtn.setOnClickListener {
            dlg.dismiss()
        }
        firstSubmitBtn.setOnClickListener{
            toggleVisibility(View.INVISIBLE)
            val sttText : String = dlg.resultTxt.text.toString()
            bitmap = mark(BitmapFactory.decodeFile(imageUrl), sttText)
            dlg.sttImageView.setImageBitmap(bitmap)
        }

        secondSubmitBtn.setOnClickListener{
            toggleVisibility(View.INVISIBLE)

            outputStream = FileOutputStream(imageUrl)
            outputStream!!.write(bitmapToByteArray(bitmap!!))

            listener.onOKClicked(bitmap!!)
            dlg.dismiss()
        }

        // 커스텀 다이얼로그를 노출한다.
        toggleVisibility(View.VISIBLE)
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
        recordBtn.visibility = View.VISIBLE;
    }

    fun mark(src: Bitmap, watermark: String?): Bitmap? {
        val w = src.width
        val h = src.height
        val shader: Shader = LinearGradient(
            0F,
            0F,
            100F,
            100F,
            Color.TRANSPARENT,
            Color.BLACK,
            Shader.TileMode.CLAMP
        )
        val result = Bitmap.createBitmap(w, h, src.config)
        val canvas = Canvas(result)
        canvas.drawBitmap(src, 0f, 0f, null)
        paint = Paint()
        paint.setColor(Color.WHITE)
        paint.setTextSize(100F)
        paint.setAntiAlias(true)
        paint.setShader(shader)
        paint.setUnderlineText(false)
        canvas.drawText(watermark, 10f, h - 15.toFloat(), paint)
        return result
    }

    private fun bitmapToByteArray(`$bitmap`: Bitmap): ByteArray? {
        val stream = ByteArrayOutputStream()
        `$bitmap`.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    private fun toggleVisibility(visibility : Int){
        when(visibility){
            View.VISIBLE -> {
                sttLayout.visibility = View.VISIBLE
                resultLayout.visibility = View.INVISIBLE
            }
            View.INVISIBLE -> {
                sttLayout.visibility = View.INVISIBLE
                resultLayout.visibility = View.VISIBLE
            }
            else -> {
                Log.d("abcd", "잘못된 값 입력")
            }
        }
    }

    fun setOnOKClickedListener(listener: (Bitmap) -> Unit) {
        this.listener = object: MyDialogOKClickedListener {
            override fun onOKClicked(content: Bitmap) {
                listener(content)
            }
        }
    }

    interface MyDialogOKClickedListener {
        fun onOKClicked(content : Bitmap)
    }

    private fun initBtn(){
        redButton.setOnTouchListener(OnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                textView.setTextColor(Color.parseColor("#FF0000"))
            }
            false
        })

        orangeButton.setOnTouchListener(OnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                textView.setTextColor(Color.parseColor("#FFA500"))
            }
            false
        })

        yellowButton.setOnTouchListener(OnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                textView.setTextColor(Color.parseColor("#FFFF00"))
            }
            false
        })

        greenButton.setOnTouchListener(OnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                textView.setTextColor(Color.parseColor("#008000"))
            }
            false
        })

        blueButton.setOnTouchListener(OnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                textView.setTextColor(Color.parseColor("#0000FF"))
            }
            false
        })

        purpleButton.setOnTouchListener(OnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                textView.setTextColor(Color.parseColor("#800080"))
            }
            false
        })

        whiteButton.setOnTouchListener(OnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                textView.setTextColor(Color.parseColor("#FFFFFF"))
            }
            false
        })

        blackButton.setOnTouchListener(OnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                textView.setTextColor(Color.parseColor("#000000"))
            }
            false
        })


        //seekBar로 글자크기 조절
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textView.setTextSize(i.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        textView.setOnTouchListener(this)
        textView.setTextSize(15.toFloat())

        seekBar.setMax(50)
        seekBar.setMin(15)
        seekBar.setProgress(size)

    }//end of initBtn


    fun onTouch(v: View, event: MotionEvent): Boolean {
        val width = (v.parent as ViewGroup).width - v.width
        val height = (v.parent as ViewGroup).height - v.height
        if (event.action == MotionEvent.ACTION_DOWN) {
            oldXvalue = event.x
            oldYvalue = event.y
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            v.x = v.x + event.x - v.width / 2
            v.y = v.y + event.y - v.height / 2
        } else if (event.action == MotionEvent.ACTION_UP) {
            if (v.x > width && v.y > height) {
                v.x = width.toFloat()
                v.y = height.toFloat()
            } else if (v.x < 0 && v.y > height) {
                v.x = 0f
                v.y = height.toFloat()
            } else if (v.x > width && v.y < 0) {
                v.x = width.toFloat()
                v.y = 0f
            } else if (v.x < 0 && v.y < 0) {
                v.x = 0f
                v.y = 0f
            } else if (v.x < 0 || v.x > width) {
                if (v.x < 0) {
                    v.x = 0f
                    v.y = v.y + event.y - v.height / 2
                } else {
                    v.x = width.toFloat()
                    v.y = v.y + event.y - v.height / 2
                }
            } else if (v.y < 0 || v.y > height) {
                if (v.y < 0) {
                    v.x = v.x + event.x - v.width / 2
                    v.y = 0f
                } else {
                    v.x = v.x + event.x - v.width / 2
                    v.y = height.toFloat()
                }
            }
        }
        return true
    } //end of onTouch

}
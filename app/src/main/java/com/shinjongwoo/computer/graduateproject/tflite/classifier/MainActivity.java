package com.shinjongwoo.computer.graduateproject.tflite.classifier;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.shinjongwoo.computer.graduateproject.R;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private ImageClassifier classifier;

    private ImageButton btnDetectObject, btnToggleCamera;
    private CameraView cameraView;

    private FileOutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = findViewById(R.id.cameraView);

        btnToggleCamera = findViewById(R.id.btnToggleCamera);
        btnDetectObject = findViewById(R.id.btnDetectObject);

        try {
            classifier = new ImageClassifier(this);
            makeButtonVisible();
        } catch (IOException e) {
            Log.e("abcd", "Failed to initialize an image classifier.");
        }


        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                // Filename n path
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String capturedTime = sdf.format(Calendar.getInstance().getTime());
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);

                // distribute face n sampling
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
                FaceDetector detector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), 10);
                FaceDetector.Face[] faces = new FaceDetector.Face[10];
                int detectedCount = detector.findFaces(bitmap, faces);

                Log.d("abcd", String.valueOf(bitmap.getConfig().compareTo(Bitmap.Config.RGB_565)));
                Log.d("abcd", String.valueOf(bitmap.getConfig().compareTo(Bitmap.Config.ALPHA_8)));
                Log.d("abcd", String.valueOf(bitmap.getConfig().compareTo(Bitmap.Config.ARGB_4444)));
                Log.d("abcd", String.valueOf(bitmap.getConfig().compareTo(Bitmap.Config.ARGB_8888)));
                Log.d("abcd", String.valueOf(bitmap.getConfig().compareTo(Bitmap.Config.HARDWARE)));
                Log.d("abcd", String.valueOf(bitmap.getConfig().compareTo(Bitmap.Config.RGBA_F16)));

                Log.d("abcd", "bitmap width : " + bitmap.getWidth() + " / height : " + bitmap.getHeight());
                if(detectedCount == 0) {
                    Log.d("abcd", "there is no face");
                    Log.d("abcd", String.valueOf(detectedCount));
                    Log.d("abcd", String.valueOf(faces[0].EULER_X));
                    Log.d("abcd", String.valueOf(faces[0].EULER_Y));
                    Log.d("abcd", String.valueOf(faces[0].EULER_Z));
                }
                else {
                    Log.d("abcd", "face result start");
                    for(int i = 0 ; i < detectedCount ; i ++) {
                        Log.d("abcd", String.valueOf(faces.length));
                        Log.d("abcd", String.valueOf(faces[i].EULER_X));
                        Log.d("abcd", String.valueOf(faces[i].EULER_Y));
                        Log.d("abcd", String.valueOf(faces[i].EULER_Z));
                    }
                    Log.d("abcd", "face result end");
                }
                byte[] bitmapByte = cameraKitImage.getJpeg();
                BitmapFactory.Options options = new BitmapFactory.Options();

                //TODO insampleSize 바꾸기
                options.inSampleSize = setSimpleSize(cameraKitImage.getBitmap(), 224, 224);
                Log.d("abcd", String.valueOf(options.inSampleSize));
//                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length, options);
                Log.d("abcd", "Width : " + bitmap.getWidth() + " / height : " + bitmap.getHeight());

//                classifyFrame(cameraKitImage.getBitmap());

                // store captured image
                try {
                    File savedPhoto = Environment.getExternalStorageDirectory();
                    String imageUrl = savedPhoto.getPath()+"/DCIM/Camera/" + capturedTime + ".jpg";
                    outputStream = new FileOutputStream(imageUrl);
//                    outputStream.write( bitmapToByteArray(bitmap));
                    outputStream.write(cameraKitImage.getJpeg());
                    cameraKitImage.getJpeg();
                    intent.putExtra("imageUrl", imageUrl);

                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }



//                intent.putExtra("bitmap", bitmap);
                startActivity(intent);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        btnToggleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.toggleFacing();
            }
        });

        btnDetectObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);
            }
        });
    }

    // 이미지 Resize 함수
    private int setSimpleSize(Bitmap bitmap, int requestWidth, int requestHeight){
        // 이미지 사이즈를 체크할 원본 이미지 가로/세로 사이즈를 임시 변수에 대입.
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        Log.d("abcd", "originalWidth : " + bitmap.getWidth());
        Log.d("abcd", "originalHeigth : " + bitmap.getHeight());
        // 원본 이미지 비율인 1로 초기화
        int size = 1;

        // 해상도가 깨지지 않을만한 요구되는 사이즈까지 2의 배수의 값으로 원본 이미지를 나눈다.
        while(requestWidth < originalWidth || requestHeight < originalHeight){
            originalWidth = originalWidth / 2;
            originalHeight = originalHeight / 2;

            size = size * 2;
        }
        return size;
    }

    public byte[] bitmapToByteArray( Bitmap $bitmap ) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
        $bitmap.compress( Bitmap.CompressFormat.JPEG, 100, stream) ;
        byte[] byteArray = stream.toByteArray() ;
        return byteArray ;
    }

    private void classifyFrame(Bitmap bitmap) {
        if (classifier == null || this == null) {
            Log.d("abcd","Uninitialized Classifier or invalid context.");
            return;
        }
        String textToShow = classifier.classifyFrame(bitmap);
        Log.d("abcd", "new One : " + textToShow);
    }
}

package com.shinjongwoo.computer.graduateproject.tflite;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import java.util.Calendar;

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

                if (classifier == null || this == null) {
                    Toast.makeText(getApplicationContext(), "Uninitialized Classifier or invalid context.", Toast.LENGTH_LONG).show();
                    return;
                }
                String textToShow = classifier.classifyFrame(Bitmap.createScaledBitmap(cameraKitImage.getBitmap(), 224, 224, true));

                Bitmap bitmap = cameraKitImage.getBitmap();
                Log.d("abcd", "Width : "+ bitmap.getWidth() + "/ Height : " + bitmap.getHeight());
                bitmap = scalingImage(bitmap);
                Log.d("abcd", "Width : "+ bitmap.getWidth() + "/ Height : " + bitmap.getHeight());

                // store captured image
                try {
                    File savedPhoto = Environment.getExternalStorageDirectory();
                    String imageUrl = savedPhoto.getPath()+"/DCIM/Camera/" + capturedTime + ".jpg";
                    outputStream = new FileOutputStream(imageUrl);
                    outputStream.write(bitmapToByteArray(bitmap));
                    intent.putExtra("imageUrl", imageUrl);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
                intent.putExtra("result", textToShow);
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

    private Bitmap  scalingImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Log.d("abcd", "Width : "+ bitmap.getWidth() + "/ Height : " + bitmap.getHeight());
        int i;
        for(i = 60 ; i >= 0 ; i--){
            if(width * height / 3600 * i * i < 1024*2048)
                break;
        }

        if ( i == 0 ) {
            Log.d("abcd", "사진이 너무 고해상도입니다. 관리자에게 문의해주시길 바랍니다.");
            return bitmap;
        }
        else
            return Bitmap.createScaledBitmap(bitmap, width /60 *i, height / 60 * i, true);
    }

}

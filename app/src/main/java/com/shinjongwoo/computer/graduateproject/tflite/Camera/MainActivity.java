package com.shinjongwoo.computer.graduateproject.tflite.Camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.shinjongwoo.computer.graduateproject.R;
import com.shinjongwoo.computer.graduateproject.tflite.classifier.Classifier;
import com.shinjongwoo.computer.graduateproject.tflite.classifier.ResultActivity;
import com.shinjongwoo.computer.graduateproject.tflite.classifier.TensorFlowImageClassifier;
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
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String MODEL_PATH = "mobilenet_quant_v1_224.tflite";
    private static final boolean QUANT = true;
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 224;

    private Executor executor = Executors.newSingleThreadExecutor();
    private Classifier classifier;

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

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String capturedTime = sdf.format(Calendar.getInstance().getTime());
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);

                byte[] bitmapByte = cameraKitImage.getJpeg();
                BitmapFactory.Options options = new BitmapFactory.Options();

                //TODO insampleSize 바꾸기
                options.inSampleSize = setSimpleSize(cameraKitImage.getBitmap(), 224, 224);
                Log.d("abcd", String.valueOf(options.inSampleSize));
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length, options);


                // store captured image
                try {
                    File savedPhoto = Environment.getExternalStorageDirectory();
                    String imageUrl = savedPhoto.getPath()+"/DCIM/Camera/" + capturedTime + ".jpg";
                    outputStream = new FileOutputStream(imageUrl);
                    outputStream.write( bitmapToByteArray(bitmap));
                    cameraKitImage.getJpeg();
                    intent.putExtra("imageUrl", imageUrl);

                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }


                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

                intent.putExtra("bitmap", bitmap);
                intent.putExtra("text", results.toString());
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

        initTensorFlowAndLoadModel();
        makeButtonVisible();
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
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
        try{
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                    makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
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
/*
    public void SaveBitmapToJPG(Bitmap bitmap, String strFilePath, String filename) {

        File file = new File(strFilePath);

        if (!file.exists()) {
            file.mkdirs();
        }

        File fileItem = new File(strFilePath + filename);
        OutputStream outStream = null;

        try {
            fileItem.createNewFile();
            outStream = new FileOutputStream(fileItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
 */
}

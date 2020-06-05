package com.shinjongwoo.computer.graduateproject.tflite;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {
    private ImageClassifier classifier;
    private Intent intent;
    private ImageButton btnDetectObject, btnToggleCamera, infoButton;
    private CameraView cameraView;

    private FileOutputStream outputStream;
    private DialogInterface mpopupDig = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = findViewById(R.id.cameraView);

        btnToggleCamera = findViewById(R.id.btnToggleCamera);
        btnDetectObject = findViewById(R.id.btnDetectObject);
        infoButton = findViewById(R.id.infoButton);

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
                // Filename & path
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String capturedTime = sdf.format(Calendar.getInstance().getTime());
                intent = new Intent(getApplicationContext(), ResultActivity.class);

                Bitmap bitmap = scalingImage(cameraKitImage.getBitmap());

                // store captured image
                try {
                    File savedPhoto = Environment.getExternalStorageDirectory();
                    String imageUrl = savedPhoto.getPath()+"/DCIM/Camera/" + capturedTime + ".jpg";
                    outputStream = new FileOutputStream(imageUrl);
                    outputStream.write(bitmapToByteArray(bitmap));
                    intent.putExtra("imageUrl", imageUrl);
                    faceDetection(cameraKitImage, imageUrl);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
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

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.info_dialog,null);
                builder.setView(layout);
                builder.create().show();
            }//end of onClick
        });
    }//end of onCreate

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
    private void faceDetection(CameraKitImage cameraKitImage, String imageUrl){
        DetectionThread detectionThread = new DetectionThread(cameraKitImage, imageUrl);
        detectionThread.start();
    }

    private class DetectionThread extends Thread {
        private CameraKitImage cameraKitImage;
        private String imageUrl;

        public DetectionThread(CameraKitImage cameraKitImage,String imageUrl){
            this.cameraKitImage = cameraKitImage;
            this.imageUrl = imageUrl;
        }

        @Override
        public void run() {
            JSONArray resp = HttpConnectionUtil.postRequest(imageUrl);
            try {
                for ( int i = 0 ; i < resp.length() ; i++) {
                    Log.d("abcd", "i번째 : " + i);
                    Log.d("abcd", "x : " + resp.getJSONObject(i).getInt("x"));
                    Log.d("abcd", "y : " + resp.getJSONObject(i).getInt("y"));
                    Log.d("abcd", "w : " + resp.getJSONObject(i).getInt("w"));
                    Log.d("abcd", "h : " + resp.getJSONObject(i).getInt("h"));
                    Bitmap bitmap = BitmapFactory.decodeFile(imageUrl);
                    Log.d("abcd", "Bitmap의 X 값 : " + bitmap.getWidth() + " / Y 값 : " + bitmap.getHeight());
                    String res = imageRecognition(Bitmap.createBitmap(BitmapFactory.decodeFile(imageUrl)
                            , resp.getJSONObject(i).getInt("x")
                            , resp.getJSONObject(i).getInt("y")
                            , resp.getJSONObject(i).getInt("w")
                            , resp.getJSONObject(i).getInt("h")));
                    resp.getJSONObject(i).put("name", res);
                }
                intent.putExtra("faces", resp.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            startActivity(intent);
        }
    }

    public String imageRecognition(Bitmap bitmap){
        Log.d("abcd", "imageRecognition start");

        if (classifier == null || this == null) {
            Toast.makeText(getApplicationContext(), "Uninitialized Classifier or invalid context.", Toast.LENGTH_LONG).show();
            return null;
        }
        String result = classifier.classifyFrame(Bitmap.createScaledBitmap(bitmap, 224, 224, true));
        StringTokenizer sb = new StringTokenizer(result, "\n");
        sb.nextToken();
        return sb.nextToken();
    }
}

package com.shinjongwoo.computer.graduateproject.tflite;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HttpConnectionUtil {

    public static JSONArray postRequest(String imageUrl) {
        File file = new File(imageUrl);

        String MYAPP_KEY = "45bc90729f45906f7b2365e56570a1e2";
        String myResult = "";
        JSONArray result = new JSONArray();

        String CRLF = "\r\n";
        String TWO_HYPHENS = "--";
        String BOUNDARY = "---------------------------012345678901234567890123456";


        DataOutputStream dos = null;
        FileInputStream fis = null;

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {
            //   URL 설정하고 접속하기
            URL url = new URL("https://kapi.kakao.com/v1/vision/face/detect"); // URL 설정
            HttpsURLConnection http = (HttpsURLConnection) url.openConnection(); // 접속


            //--------------------------
            //   전송 모드 설정 - 기본적인 설정
            //--------------------------
            http.setUseCaches(false);
            http.setDoInput(true);
            http.setDoOutput(true); // 서버로 쓰기 모드 지정
            http.setRequestMethod("POST"); // 전송 방식은 POST

            //--------------------------
            // 헤더 세팅
            //--------------------------
            http.setRequestProperty("Connection", "Keep-Alive");
            http.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            http.setRequestProperty("Authorization", "KakaoAK " + MYAPP_KEY);
            http.setRequestProperty("Cache-Control", "no-cache");

            //--------------------------
            //   서버로 값 전송
            //--------------------------

            dos = new DataOutputStream(http.getOutputStream());
            dos.writeBytes(TWO_HYPHENS + BOUNDARY + CRLF);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\";" + " filename=\"" + file.getName() + "\"" + CRLF);
            dos.writeBytes(CRLF);


            fis = new FileInputStream(file);
            bytesAvailable = fis.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            bytesRead = fis.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fis.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fis.read(buffer, 0, bufferSize);
            }
            dos.writeBytes(CRLF);

            // finish delimiter
            dos.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + CRLF);

            fis.close();
            dos.flush();
            dos.close();
            //--------------------------
            //   Response Code
            //--------------------------
            Log.d("abcd", "연결결과는 " + http.getResponseCode());


            //--------------------------
            //   서버에서 전송받기
            //--------------------------
            InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(tmp);
            StringBuilder builder = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                builder.append(str + "\n");
            }
            reader.close();
            myResult = builder.toString();

            JSONObject jsonObject = new JSONObject(myResult).getJSONObject("result"), iterator;
            JSONArray jsonArray = jsonObject.getJSONArray("faces");
            int width = jsonObject.getInt("width");
            int height = jsonObject.getInt("height");
            for(int i = 0 ; i < jsonArray.length() ; i++){
                iterator = jsonArray.getJSONObject(i);
                JSONObject data = new JSONObject();
                data.put("x",  Math.round(iterator.getDouble("x") * width));
                data.put("y",  Math.round(iterator.getDouble("y") * height));
                data.put("w",  Math.round(iterator.getDouble("w") * width));
                data.put("h",  Math.round(iterator.getDouble("h") * height));
                result.put(data);
            }
            return result;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

}
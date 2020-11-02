package com.example.uploadtest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private Button sent;
    TextView txt;
    EditText CRIM,RM,AGE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sent = findViewById(R.id.sent);
        txt = findViewById(R.id.txt);
        CRIM = findViewById(R.id.CRIM);
        RM = findViewById(R.id.RM);
        AGE = findViewById(R.id.AGE);

        sent.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                httpRequest();
            }
        });

    }
    // python django use
    @RequiresApi(api = Build.VERSION_CODES.N)
    void httpRequest(){

        //OkHttpClinet生成
        OkHttpClient client = new OkHttpClient();

//        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//
//        Map<String, String> formParamMap = new HashMap<>();
//        formParamMap.put("CRIM", String.valueOf(CRIM.getText()));
//        formParamMap.put("RM", String.valueOf(RM.getText()));
//        formParamMap.put("AGE", String.valueOf(AGE.getText()));
//
//        JSONObject parameter = new JSONObject(formParamMap);
//        //RequestBody requestBody = RequestBody.create(JSON, arameter.toString(p));

        // Names and values will be url encoded
        RequestBody formBody = new FormBody.Builder()
                .add("CRIM", String.valueOf(CRIM.getText()))
                .add("RM", String.valueOf(RM.getText()))
                .add("AGE", String.valueOf(AGE.getText()))
                .build();
        //request生成
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8000/fileapi/rom/")
                .post(formBody)
                .build();

        //非同期リクエスト
        client.newCall(request)
                .enqueue(new Callback() {

                    //エラーのとき
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.e("Hoge", Objects.requireNonNull(e.getMessage()));
                    }

                    //正常のとき
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                        //response取り出し
                        final String res = Objects.requireNonNull(response.body()).string();

                        //JSON処理
                        try {

                            //親スレッドUI更新
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    txt.setText(res);
                                }
                            });


                        } catch (Exception e) {
                            Log.e("OkHttp", Objects.requireNonNull(e.getMessage()));
                        }

                    }
                });
    }

}
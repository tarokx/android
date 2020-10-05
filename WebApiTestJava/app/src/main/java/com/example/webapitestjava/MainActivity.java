package com.example.webapitestjava;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //Widget宣言
    TextView txt01;
    Button btn01;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Widget初期化
        txt01 = findViewById(R.id.txt01);
        btn01 = findViewById(R.id.btn01);

        //ボタンクリック
        btn01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //httpリクエスト
                try{
                    //okhttpを利用するカスタム関数（下記）
                    httpRequest("http://10.0.2.2:8080/map?data1=35&data2=140");
                }catch(Exception e){
                    Log.e("Hoge",e.getMessage());
                }

            }
        });
    }

    void httpRequest(String url) throws IOException {

        //OkHttpClinet生成
        OkHttpClient client = new OkHttpClient();

        //request生成
        Request request = new Request.Builder()
                .url(url)
                .build();

        //非同期リクエスト
        client.newCall(request)
                .enqueue(new Callback() {

                    //エラーのとき
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.e("Hoge",e.getMessage());
                    }

                    //正常のとき
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                        //response取り出し
                        final String jsonStr = response.body().string();
                        Log.d("Hoge","jsonStr=" + jsonStr);

                        //JSON処理
                        try{
                            //jsonパース
                            JSONArray jsonArray = new JSONArray(jsonStr);
                            final ArrayList<String> list = new ArrayList<String>();
                            if (jsonArray != null) {
                                int len = jsonArray.length();
                                for (int i=0;i<len;i++){
                                    list.add(jsonArray.get(i).toString());
                                }
                            }

                            String listString = "";

                            for (String s : list)
                            {
                                listString += s + "\n";
                            }

                            //親スレッドUI更新
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            final String finalListString = listString;
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    txt01.setText(finalListString);
                                }
                            });


                        }catch(Exception e){
                            Log.e("Hoge",e.getMessage());
                        }

                    }
                });
    }
}
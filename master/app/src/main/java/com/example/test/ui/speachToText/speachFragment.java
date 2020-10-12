package com.example.test.ui.speachToText;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;


import com.example.test.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class speachFragment extends Fragment {
    //Widget宣言
    Button btn01;
    TextView ana;
    private boolean isRecord = false;
    private Button btn_record_wav;
    private Button btn_stop;
    private TextView txt;
    private Button.OnClickListener btn_record_wav_clickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            record();
        }
    };
    private Button.OnClickListener btn_stop_clickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            stop();
        }
    };

    private Handler handler;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_totext, container, false);

        return root;
    }

    @Override
    public void onStop(){
        super.onStop();
        this.stop();
    }

    @Override
    public void onStart() {
        super.onStart();
        findViewByIds();
        setListeners();

        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO},
                1);


        handler =  new Handler(){
            //メッセージ受信
            public void handleMessage(Message message) {
                //メッセージの表示
                switch(message.what) {
                    case 1:
                        // msg.objはObject型なのでキャストする必要がある
                        txt.setText((String) message.obj);
                        break;
                    case 2:
                        // msg.objはObject型なのでキャストする必要がある
                        ana.setText((String) message.obj);
                        break;
                    default :
                        break;
                }
                //メッセージの種類に応じてswitch文で制御すれば
                //イベント制御に利用できます
            };
        };

    }

    private void findViewByIds() {
        btn_record_wav = (Button) getActivity().findViewById(R.id.btn_record_wav);
        btn_stop = (Button) getActivity().findViewById(R.id.btn_stop);
        txt = (TextView) getActivity().findViewById(R.id.text);
        //Widget初期化
        btn01 = getActivity().findViewById(R.id.btn01);
        ana = getActivity().findViewById(R.id.ana);
    }

    private void setListeners() {
        btn_record_wav.setOnClickListener(btn_record_wav_clickListener);
        btn_stop.setOnClickListener(btn_stop_clickListener);
        //ボタンクリック
        btn01.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                //httpリクエスト
                try {
                    //okhttpを利用するカスタム関数（下記）
                    httpRequest();
                } catch (Exception e) {
                    Log.e("Send", Objects.requireNonNull(e.getMessage()));
                }
            }
        });
    }

    /**
     * 开始录音
     */
    private void record() {
        if (isRecord) {
            return;
        }

        AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance(handler);
        int mResult = mRecord_1.startRecordAndFile();

        if (mResult == ErrorCode.SUCCESS) {
            isRecord = true;
            txt.setText("record");
        } else {
            System.out.print("ERR");
        }

    }

    /**
     * 停止录音
     */
    private void stop() {
        if (isRecord) {

            AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance(handler);
            mRecord_1.stopRecordAndFile();
            isRecord = false;
            txt.setText("stop");
        } else {

                System.out.print("ERR");
        }
    }

    void httpRequest(){

        MediaType MEDIA_TYPE_WAV
                = MediaType.parse("audio/wav");
        File file = new File(AudioFileFunc.getWavFilePath());

        //OkHttpClinet生成
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MEDIA_TYPE_WAV, file))
                .build();

        //request生成
        Request request = new Request.Builder()
                .url("http://192.168.11.8:8000/fileapi/speachToText/")
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
                        final String jsonStr = Objects.requireNonNull(response.body()).string();
                        Log.d("Hoge", "jsonStr=" + jsonStr);

                        //JSON処理
                        try {
                            //jsonパース
                            JSONArray jsonArray = new JSONArray(jsonStr);
                            final ArrayList<String> list = new ArrayList<>();
                            int len = jsonArray.length();
                            for (int i = 0; i < len; i++) {
                                list.add(jsonArray.getJSONObject(i).getString("massage"));
                            }

                            StringBuilder listString = new StringBuilder();

                            for (String s : list) {
                                listString.append(s).append("\n");
                            }

                            //親スレッドUI更新
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            final String finalListString = listString.toString();
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    txt.setText(finalListString);
                                }
                            });


                        } catch (Exception e) {
                            Log.e("OkHttp", Objects.requireNonNull(e.getMessage()));
                        }

                    }
                });
    }

}

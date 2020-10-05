package com.example.test.ui.speachToText;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;


import com.example.test.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class speachFragment extends Fragment {
    private final static int FLAG_WAV = 0;
    private final static int FLAG_AMR = 1;
    private int mState = -1;    //-1:没再录制，0：录制wav，1：录制amr
    private Button btn_record_wav;
    private Button btn_stop;
    private TextView txt;
    private UIHandler uiHandler;
    private UIThread uiThread;
    //Widget宣言
    Button btn01;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_totext, container, false);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        findViewByIds();
        setListeners();
        init();
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        //Widget初期化
        btn01 = getActivity().findViewById(R.id.btn01);

        //ボタンクリック
        btn01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //httpリクエスト
                try{
                    //okhttpを利用するカスタム関数（下記）
                    httpRequest("http://192.168.11.8:8000/fileapi/speachToText/");
                }catch(Exception e){
                    Log.e("Hoge",e.getMessage());
                }

            }
        });

    }

    void httpRequest(String url) throws IOException {

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
                .url(url)
                .post(formBody)
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
                                    txt.setText(finalListString);
                                }
                            });


                        }catch(Exception e){
                            Log.e("Hoge",e.getMessage());
                        }

                    }
                });
    }

    private void findViewByIds(){
        btn_record_wav = getActivity().findViewById(R.id.btn_record_wav);
        btn_stop = getActivity().findViewById(R.id.btn_stop);
        txt = getActivity().findViewById(R.id.text);
    }
    private void setListeners(){
        btn_record_wav.setOnClickListener(btn_record_wav_clickListener);
        btn_stop.setOnClickListener(btn_stop_clickListener);
    }
    private void init(){
        uiHandler = new UIHandler();
    }
    private Button.OnClickListener btn_record_wav_clickListener = new Button.OnClickListener(){
        public void onClick(View v){
            record(FLAG_WAV);
        }
    };
    private Button.OnClickListener btn_record_amr_clickListener = new Button.OnClickListener(){
        public void onClick(View v){
            record(FLAG_AMR);
        }
    };
    private Button.OnClickListener btn_stop_clickListener = new Button.OnClickListener(){
        public void onClick(View v){
            stop();
        }
    };
    /**
     * 开始录音
     * @param mFlag，0：录制wav格式，1：录音amr格式
     */
    private void record(int mFlag){
        if(mState != -1){
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putInt("cmd",CMD_RECORDFAIL);
            b.putInt("msg", ErrorCode.E_STATE_RECODING);
            msg.setData(b);

            uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
            return;
        }
        int mResult = -1;
        switch(mFlag){
            case FLAG_WAV:
                AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance();
                mResult = mRecord_1.startRecordAndFile();
                break;
            case FLAG_AMR:
                MediaRecordFunc mRecord_2 = MediaRecordFunc.getInstance();
                mResult = mRecord_2.startRecordAndFile();
                break;
        }
        if(mResult == ErrorCode.SUCCESS){
            uiThread = new UIThread();
            new Thread(uiThread).start();
            mState = mFlag;
        }else{
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putInt("cmd",CMD_RECORDFAIL);
            b.putInt("msg", mResult);
            msg.setData(b);

            uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
        }
    }
    /**
     * 停止录音
     */
    private void stop(){
        if(mState != -1){
            switch(mState){
                case FLAG_WAV:
                    AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance();
                    mRecord_1.stopRecordAndFile();
                    break;
                case FLAG_AMR:
                    MediaRecordFunc mRecord_2 = MediaRecordFunc.getInstance();
                    mRecord_2.stopRecordAndFile();
                    break;
            }
            if(uiThread != null){
                uiThread.stopThread();
            }
            if(uiHandler != null)
                uiHandler.removeCallbacks(uiThread);
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putInt("cmd",CMD_STOP);
            b.putInt("msg", mState);
            msg.setData(b);
            uiHandler.sendMessageDelayed(msg,1000); // 向Handler发送消息,更新UI
            mState = -1;
        }
    }
    private final static int CMD_RECORDING_TIME = 2000;
    private final static int CMD_RECORDFAIL = 2001;
    private final static int CMD_STOP = 2002;
    class UIHandler extends Handler{
        public UIHandler() {
        }
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.d("MyHandler", "handleMessage......");
            super.handleMessage(msg);
            Bundle b = msg.getData();
            int vCmd = b.getInt("cmd");
            switch(vCmd)
            {
                case CMD_RECORDING_TIME:
                    int vTime = b.getInt("msg");
                    txt.setText("正在录音中，已录制："+vTime+" s");
                    break;
                case CMD_RECORDFAIL:
                    int vErrorCode = b.getInt("msg");
                    String vMsg = ErrorCode.getErrorInfo(getActivity(), vErrorCode);
                    txt.setText("录音失败："+vMsg);
                    break;
                case CMD_STOP:
                    int vFileType = b.getInt("msg");
                    switch(vFileType){
                        case FLAG_WAV:
                            AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance();
                            long mSize = mRecord_1.getRecordFileSize();
                            txt.setText("录音已停止.录音文件:"+AudioFileFunc.getWavFilePath()+"\n文件大小："+mSize);
                            break;
                        case FLAG_AMR:
                            MediaRecordFunc mRecord_2 = MediaRecordFunc.getInstance();
                            mSize = mRecord_2.getRecordFileSize();
                            txt.setText("录音已停止.录音文件:"+AudioFileFunc.getAMRFilePath()+"\n文件大小："+mSize);
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };
    class UIThread implements Runnable {
        int mTimeMill = 0;
        boolean vRun = true;
        public void stopThread(){
            vRun = false;
        }
        public void run() {
            while(vRun){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mTimeMill ++;
                Log.d("thread", "mThread........"+mTimeMill);
                Message msg = new Message();
                Bundle b = new Bundle();// 存放数据
                b.putInt("cmd",CMD_RECORDING_TIME);
                b.putInt("msg", mTimeMill);
                msg.setData(b);

                uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
            }

        }
    }

}

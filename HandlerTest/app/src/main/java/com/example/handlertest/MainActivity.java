package com.example.handlertest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView tv = (TextView) findViewById(R.id.textview);

        mHandler = new Handler() {
            //メッセージ受信
            public void handleMessage(Message message) {
                //メッセージの表示
                tv.setText((String) message.obj);
                //メッセージの種類に応じてswitch文で制御すれば
                //イベント制御に利用できます
            }
        };
    }
}

package com.example.uihandlertest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Send Message Sample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //UI部分の設定
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        Button button = new Button(this);
        setContentView(layout);
        final TextView tv = new TextView(this);
        tv.setText("What's Message");
        layout.addView(tv);
        layout.addView(button);

        //Handlerインスタンスの生成
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                tv.setText(String.format("what=%s", msg.what));
                Log.v(TAG, String.format("what=%s\targ1=%d\targ2=%d\t%s",
                        msg.what, msg.arg1, msg.arg2, msg.obj));
            }
        };

        button.setText("PUSH");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        //Messageオブジェクトを使ってsendMessageを行う
                        Message msg = new Message();
                        msg.what = 4645;
                        msg.arg1 = 37458;
                        msg.arg2 = 223606;
                        msg.obj = "obj";

                        handler.sendMessage(msg);
                    }
                }).start();
            }
        });
    }
}


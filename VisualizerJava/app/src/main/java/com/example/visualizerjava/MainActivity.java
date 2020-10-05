package com.example.visualizerjava;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private MainActivity.Record _record;
    boolean isRecording = false;
    Button button;
    TextView textView0;
    TextView textView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView0 = findViewById(R.id.t0);
        textView1 = findViewById(R.id.t1);

        button = findViewById(R.id.buttonStart);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording){
                    stopRecord();
                }else {
                    startRecord();
                }

            }
        });
    }

    private void stopRecord(){
        isRecording =false;
        button.setText("start");

        if(_record != null) {
            _record.cancel(true);
        }
    }
    private  void startRecord(){
        isRecording =true;
        button.setText("stop");

        this._record = new MainActivity.Record();
        _record.execute();
    }

    public final class Record extends AsyncTask {
        @Nullable
        protected Void doInBackground() {
            int sampleRate = 44100;
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, 16, 2) * 2;
            if (minBufferSize < 0) {
                return null;
            } else {
                AudioRecord audioRecord = new AudioRecord(1, sampleRate, 12, 2, minBufferSize);
                int sec = 1;
                short[] buffer = new short[sampleRate * 2 * 1 * sec];
                audioRecord.startRecording();

                try {
                    while (isRecording) {
                        int readSize = audioRecord.read(buffer, 0, minBufferSize);
                        if (readSize < 0) {
                            break;
                        }

                        if (readSize != 0) {
                            boolean var9 = false;
                            if (buffer.length == 0) {
                                break;
                            }
                            if (buffer[0] < 1000 && buffer[0] > -1000) {
                                textView0.setText("ok");
                            } else {
                                textView0.setText("no");
                            }
                            textView1.setText(String.valueOf(buffer[0]));
                        }
                    }
                } finally {
                    audioRecord.stop();
                    audioRecord.release();
                }

                return null;
            }
        }

        // $FF: synthetic method
        // $FF: bridge method
        public Object doInBackground(Object[] var1) {
            return this.doInBackground();
        }
    }
}
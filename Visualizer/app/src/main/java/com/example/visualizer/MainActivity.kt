package com.example.visualizer

import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.AsyncTask
import android.os.Bundle
import android.view.SurfaceView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var _record: Record? = null
    var _isRecording = false
    var _button: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _button = findViewById(R.id.buttonStart) as Button
        _button?.setOnClickListener {
            if(_isRecording)
                stopRecord()
            else
                doRecord()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        stopRecord()
    }

    override fun onPause() {
        super.onPause()
        stopRecord()
    }

    fun stopRecord(){
        _isRecording = false
        _button?.text = "start"
        _record?.cancel(true)
    }

    fun doRecord(){
        _isRecording = true
        _button?.text = "stop"

        // AsyncTaskは使い捨て１回こっきりなので毎回作ります
        _record = Record()
        _record?.execute()
    }

    inner class Record : AsyncTask<Void, DoubleArray, Void>() {
        override fun doInBackground(vararg params: Void): Void? {
            // サンプリングレート。1秒あたりのサンプル数
            // （8000, 11025, 22050, 44100, エミュでは8kbじゃないとだめ？）
            val sampleRate = 8000

            // 最低限のバッファサイズ
            val minBufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT) * 2

            // バッファサイズが取得できない。サンプリングレート等の設定を端末がサポートしていない可能性がある。
            if(minBufferSize < 0){
                return null
            }

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize)

            val sec = 1
            val buffer: ShortArray = ShortArray(sampleRate * (16 / 8) * 1 * sec)

            audioRecord.startRecording()

            try {
                while (_isRecording) {
                    val readSize = audioRecord.read(buffer, 0, minBufferSize)

                    if (readSize < 0) {
                        break
                    }
                    if (readSize == 0) {
                        continue
                    }
                    if(buffer.isEmpty()){
                        break
                    }

                    if(buffer[0] < 1000 && buffer[0] > -1000){
                        t1.text=buffer[0].toString()
                        textView.text="ok"
                    }else{
                        t1.text=buffer[0].toString()
                        textView.text="no"
                    }
                    //_visualizer?.update(buffer, readSize)
                }
            } finally {
                audioRecord.stop()
                audioRecord.release()
            }

            return null
        }
    }
}
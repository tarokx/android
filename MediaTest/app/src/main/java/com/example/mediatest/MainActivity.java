package com.example.mediatest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MainActivity extends AppCompatActivity {
    Button startButton;
    Button stopButton;
    Button playButton;
    String filePath ="";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);

        startButton=findViewById(R.id.start);
        stopButton=findViewById(R.id.stop);
        playButton=findViewById(R.id.play);

        filePath = getExternalCacheDir().getAbsolutePath();
        filePath += "/t.wav";

        initAudioRecord();
        initAudioRecordButton();
    }

    AudioRecord audioRecord; //録音用のオーディオレコードクラス
    final static int SAMPLING_RATE = 44100; //オーディオレコード用サンプリング周波数
    private int bufSize;//オーディオレコード用バッファのサイズ
    private short[] shortData; //オーディオレコード用バッファ
    private MyWaveFile wav1 = new MyWaveFile();

    //AudioRecordの初期化
    private void initAudioRecord(){
        wav1.createFile(filePath);
        // AudioRecordオブジェクトを作成
        bufSize = android.media.AudioRecord.getMinBufferSize(SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufSize);

        shortData = new short[bufSize / 2];

        // コールバックを指定
        audioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
            // フレームごとの処理
            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                // TODO Auto-generated method stub
                audioRecord.read(shortData, 0, bufSize / 2); // 読み込む
                wav1.addBigEndianData(shortData); // ファイルに書き出す
            }

            @Override
            public void onMarkerReached(AudioRecord recorder) {
                // TODO Auto-generated method stub

            }
        });
        // コールバックが呼ばれる間隔を指定
        audioRecord.setPositionNotificationPeriod(bufSize / 2);
    }

    /**
     * オーディオレコード用のボタン初期化
     */
    private void initAudioRecordButton(){
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAudioRecord();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAudioRecord();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRecord();
            }
        });
    }

    private static final String LOG_TAG = "AudioRecordTest";
    private MediaPlayer   player = null;
    private void playRecord() {
        player = new MediaPlayer();
        try {
            player.setDataSource(filePath);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }


    private void startAudioRecord(){
        audioRecord.startRecording();
        audioRecord.read(shortData, 0, bufSize/2);
    }

    //オーディオレコードを停止する
    private void stopAudioRecord(){
        audioRecord.stop();
    }



    public class MyWaveFile {
        private final int FILESIZE_SEEK = 4;
        private final int DATASIZE_SEEK = 40;
        int SAMPLING_RATE = 44100;
        private RandomAccessFile raf; //リアルタイム処理なのでランダムアクセスファイルクラスを使用する
        private File recFile; //録音後の書き込み、読み込みようファイル
        private String fileName = "/sdcard/teruuuSound.wav"; //録音ファイルのパス
        private byte[] RIFF = {'R','I','F','F'}; //wavファイルリフチャンクに書き込むチャンクID用
        private int fileSize = 36;
        private byte[] WAVE = {'W','A','V','E'}; //WAV形式でRIFFフォーマットを使用する
        private byte[] fmt = {'f','m','t',' '}; //fmtチャンク スペースも必要
        private int fmtSize = 16; //fmtチャンクのバイト数
        private byte[] fmtID = {1, 0}; // フォーマットID リニアPCMの場合01 00 2byte
        private short chCount = 1; //チャネルカウント モノラルなので1 ステレオなら2にする
        private int bytePerSec = SAMPLING_RATE * (fmtSize / 8) * chCount; //データ速度
        private short blockSize = (short) ((fmtSize / 8) * chCount); //ブロックサイズ (Byte/サンプリングレート * チャンネル数)
        private short bitPerSample = 16; //サンプルあたりのビット数 WAVでは8bitか16ビットが選べる
        private byte[] data = {'d', 'a', 't', 'a'}; //dataチャンク
        private int dataSize = 0; //波形データのバイト数


        public	void	createFile(String	fName){
            fileName	=	fName;
            //	ファイルを作成
            recFile	=	new	File(fileName);
            if(recFile.exists()){
                recFile.delete();
            }
            try	{
                recFile.createNewFile();
            }	catch	(IOException	e)	{
                //	TODO	Auto-generated	catch	block
                e.printStackTrace();
            }

            try	{
                raf	=	new	RandomAccessFile(recFile,	"rw");
            }	catch	(FileNotFoundException e)	{
                //	TODO	Auto-generated	catch	block
                e.printStackTrace();
            }

            //wavのヘッダを書き込み
            try	{
                raf.seek(0);
                raf.write(RIFF);
                raf.write(littleEndianInteger(fileSize));
                raf.write(WAVE);
                raf.write(fmt);
                raf.write(littleEndianInteger(fmtSize));
                raf.write(fmtID);
                raf.write(littleEndianShort(chCount));
                raf.write(littleEndianInteger(SAMPLING_RATE)); //サンプリング周波数
                raf.write(littleEndianInteger(bytePerSec));
                raf.write(littleEndianShort(blockSize));
                raf.write(littleEndianShort(bitPerSample));
                raf.write(data);
                raf.write(littleEndianInteger(dataSize));

            }	catch	(IOException	e)	{
                //	TODO	Auto-generated	catch	block
                e.printStackTrace();
            }

        }


        private byte[] littleEndianInteger(int i){
            byte[] buffer = new byte[4];
            buffer[0] = (byte) i;
            buffer[1] = (byte) (i >> 8);
            buffer[2] = (byte) (i >> 16);
            buffer[3] = (byte) (i >> 24);

            return buffer;

        }

        // PCMデータを追記するメソッド
        public void addBigEndianData(short[] shortData){

            // ファイルにデータを追記
            try {
                raf.seek(raf.length());
                raf.write(littleEndianShorts(shortData));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // ファイルサイズを更新
            updateFileSize();

            // データサイズを更新
            updateDataSize();

        }

        // ファイルサイズを更新
        private void updateFileSize(){

            fileSize = (int) (recFile.length() - 8);
            byte[] fileSizeBytes = littleEndianInteger(fileSize);
            try {
                raf.seek(FILESIZE_SEEK);
                raf.write(fileSizeBytes);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        // データサイズを更新
        private void updateDataSize(){

            dataSize = (int) (recFile.length() - 44);
            byte[] dataSizeBytes = littleEndianInteger(dataSize);
            try {
                raf.seek(DATASIZE_SEEK);
                raf.write(dataSizeBytes);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        // short型変数をリトルエンディアンのbyte配列に変更
        private byte[] littleEndianShort(short s){

            byte[] buffer = new byte[2];

            buffer[0] = (byte) s;
            buffer[1] = (byte) (s >> 8);

            return buffer;

        }

        // short型配列をリトルエンディアンのbyte配列に変更
        private byte[] littleEndianShorts(short[] s){

            byte[] buffer = new byte[s.length * 2];
            int i;

            for(i = 0; i < s.length; i++){
                buffer[2 * i] = (byte) s[i];
                buffer[2 * i + 1] = (byte) (s[i] >> 8);
            }
            return buffer;
        }


        // ファイルを閉じる
        public void close(){
            try {
                raf.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
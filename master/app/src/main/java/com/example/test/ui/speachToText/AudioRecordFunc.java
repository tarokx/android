package com.example.test.ui.speachToText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AudioRecordFunc {
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;

    //AudioName裸音频数据文件 ，麦克风
    private String AudioName = "";

    //NewAudioName可播放的音频文件
    private String NewAudioName = "";

    private AudioRecord audioRecord;
    private boolean isRecord = false;// 设置正在录制的状

    private static AudioRecordFunc mInstance;

    private AudioRecordFunc(){

    }

    private static Handler mhandler;

    public synchronized static AudioRecordFunc getInstance(Handler handler)
    {
        if(mhandler == null)
            mhandler = handler;
        if(mInstance == null)
            mInstance = new AudioRecordFunc();
        return mInstance;
    }

    public int startRecordAndFile() {
        //判断是否有外部存储设备sdcard
        if(AudioFileFunc.isSdcardExit())
        {
            if(isRecord)
            {
                return ErrorCode.E_STATE_RECODING;
            }
            else
            {
                if(audioRecord == null)
                    creatAudioRecord();

                audioRecord.startRecording();
                // 让录制状态为true
                isRecord = true;
                // 开启音频文件写入线程
                new Thread(new AudioRecordThread()).start();

                return ErrorCode.SUCCESS;
            }

        }
        else
        {
            return ErrorCode.E_NOSDCARD;
        }

    }

    public void stopRecordAndFile() {
        close();
    }


    public long getRecordFileSize(){
        return AudioFileFunc.getFileSize(NewAudioName);
    }


    private void close() {
        if (audioRecord != null) {
            System.out.println("stopRecord");
            isRecord = false;//停止文件写入
            //audioRecord.stop();
            //audioRecord.release();//释放资源
            //audioRecord = null;
        }
    }


    private void creatAudioRecord() {
        // 获取音频文件路径
        AudioName = AudioFileFunc.getRawFilePath();
        NewAudioName = AudioFileFunc.getWavFilePath();

        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AudioFileFunc.AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        // 创建AudioRecord对象
        audioRecord = new AudioRecord(AudioFileFunc.AUDIO_INPUT, AudioFileFunc.AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
    }


    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            try {
                writeDateTOFile();//往文件中写入裸数据
            } catch (IOException e) {
                e.printStackTrace();
            }
            copyWaveFile(AudioName, NewAudioName);//给裸数据加上头文件


        }
    }

    /**
     * 这里将数据写入文件，但是并不能播放，因为AudioRecord获得的音频是原始的裸音频，
     * 如果需要播放就必须加入一些格式或者编码的头信息。但是这样的好处就是你可以对音频的 裸数据进行处理，比如你要做一个爱说话的TOM
     * 猫在这里就进行音频的处理，然后重新封装 所以说这样得到的音频比较容易做一些音频的处理。
     */
    private void writeDateTOFile() throws IOException {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        FileOutputStream fos = null;
        int readsize = 0;
        try {
            File file = new File(AudioName);
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);// 建立一个可存取字节的文件
        } catch (Exception e) {
            e.printStackTrace();
        }
        int SpTime = 0;
        int StTime = 0;
        byte[] audiodata = new byte[bufferSizeInBytes];

        short buffer = 0;
        while (isRecord == true) {
            //readsize = audioRecord.read(buffer, 0, 2);
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            buffer = ByteBuffer.wrap(audiodata).getShort(1);

            if (AudioRecord.ERROR_INVALID_OPERATION != readsize && fos!=null) {
                try {
                    fos.write(audiodata);

//                    Message msg = Message.obtain(); //推奨
//                    msg.what = 1;
//                    msg.obj = "speak: " + SpTime + "\nstop: " + StTime + "\n" + String.valueOf(buffer);

                    SpTime++;
                    if(SpTime > 600 | StTime > 5){
                        if(SpTime > 10 ) {
                            audioRecord.stop();

                            copyWaveFile(AudioName, NewAudioName);//给裸数据加上头文件
                            //this.httpRequest();

                            try {
                                File file = new File(AudioName);
                                if (file.exists()) {
                                    file.delete();
                                }
                                fos = new FileOutputStream(file);// 建立一个可存取字节的文件
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            audioRecord.startRecording();
                        }
                        SpTime = 0;
                        StTime = 0;
                    }
                    int lee = 300;
                    if (buffer < lee && buffer > -lee) {
                        StTime++;
                    }else{
                        StTime = 0;
                    }
//                    if((SpTime % 3) == 0) {
//                        //ハンドラへのメッセージ送信
//                        mhandler.sendMessage(msg);
//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            if(fos != null)
                fos.close();// 关闭写入流
        } catch (IOException e) {
            e.printStackTrace();
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
                                list.add(jsonArray.getJSONObject(i).getString("message"));
                            }

                            StringBuilder listString = new StringBuilder();

                            for (String s : list) {
                                listString.append(s).append("\n");
                            }

                            Message msg = Message.obtain(); //推奨
                            msg.what = 2;
                            msg.obj = listString.toString();
                            mhandler.sendMessage(msg);

                        } catch (Exception e) {
                            Log.e("OkHttp", Objects.requireNonNull(e.getMessage()));
                        }

                    }
                });
    }

    // 这里得到可播放的音频文件
    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = AudioFileFunc.AUDIO_SAMPLE_RATE;
        int channels = 2;
        long byteRate = 16 * channels / 8;
        byte[] data = new byte[bufferSizeInBytes];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。
     * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
     * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有
     * 自己特有的头文件。
     */
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }
}
package com.example.downloadtest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    static final String DOWNLOAD_FILE_NAME = "1026364.jpg";
    static final String DOWNLOAD_URL = "http://10.0.2.2:8080/downloadFile/";
    static final int TIMEOUT_SECOND = 60000;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 駄目× getApplicationContext() ／ 駄目× getBaseContext() ／ 良し◎ MainActivity.this
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("ダウンロード進捗");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    public void onClickDownloadButton(View view) {
        DownLoadTask task = new DownLoadTask();
        task.execute(DOWNLOAD_URL + DOWNLOAD_FILE_NAME);
    }

    private class DownLoadTask extends AsyncTask<String, Integer, Boolean> {
        private final String TAG = DownLoadTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            URL url;
            try {
                url = new URL(params[0]);
            } catch (MalformedURLException e) {
                Log.e(getClass().getSimpleName(), "誤ったURL書式 : " + params[0], e);
                return null;
            }

            HttpURLConnection conn = null;

            long total = 0;

            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                // Keep-Aliveを無効にしないと、2回目の通信時にReadTimeOutを喰らう
                if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2 &&
                        Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ){
                    conn.setRequestProperty("Connection", "close");
                }
                conn.setReadTimeout(TIMEOUT_SECOND);
                conn.setConnectTimeout(TIMEOUT_SECOND);
                conn.connect();
                // ファイルサイズの取得（％表示するため）
                int fileLength = conn.getContentLength();
                // SDカードのパスは環境によって異なるので、動的に取得する
                String sdPath = Environment.getExternalStorageDirectory().getPath();
                String filePath = sdPath + "/" + DOWNLOAD_FILE_NAME;

                // ファイルダウンロード
                try (InputStream input = new BufferedInputStream(conn.getInputStream());
                     OutputStream output = new FileOutputStream(filePath)) {
                    byte data[] = new byte[1024];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        // ProgressBarに進捗を表示してもらう
                        publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);
                    }
                    output.flush();
                }
            } catch (ProtocolException e) {
                // ここが「ダウンロードの頓挫」
                Log.e(TAG, "total received = " + total, e);
                return false;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                return false;
            } finally {
                if (conn != null) {
                    try {
                        conn.disconnect();
                    } catch (Exception ignore) {
                        Log.e(TAG, ignore.getMessage(), ignore);
                    }
                }
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();
            String toastMessage = (success) ? "ダウンロード完了" : "ダウンロード失敗";
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
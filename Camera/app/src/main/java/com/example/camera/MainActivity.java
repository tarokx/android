package com.example.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {

    //カメラ表示
    private SurfaceView sv;
    //カメラ
    private Camera cam;
    //画面レイアウト
    private FrameLayout fl;

    //ボタン
    private Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //レイアウト作成
        fl = new FrameLayout(this);
        setContentView(fl);

        //表示画面
        sv = new SurfaceView(this);
        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(new SurfaceHolderCallback());

        //ボタン
        btn = new Button(this);
        btn.setText("撮影");
        btn.setLayoutParams(new LayoutParams(400, 300));
        btn.setOnClickListener(new TakePictureClickListener());

        //レイアウトに追加
        fl.addView(sv);
        fl.addView(btn);
    }

    class SurfaceHolderCallback implements SurfaceHolder.Callback {
        //起動時
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            cam = Camera.open();
            Parameters param = cam.getParameters();
            List<Size> ss = param.getSupportedPictureSizes();
            Size pictSize = ss.get(0);

            param.setPictureSize(pictSize.width, pictSize.height);
            cam.setParameters(param);
        }
        //？
        @Override
        public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
            try {
                cam.setDisplayOrientation(0);
                cam.setPreviewDisplay(sv.getHolder());

                Parameters param = cam.getParameters();
                List<Size> previewSizes =
                        cam.getParameters().getSupportedPreviewSizes();
                Size pre = previewSizes.get(0);
                param.setPreviewSize(pre.width, pre.height);

                LayoutParams lp = new LayoutParams(pre.width, pre.height);
                sv.setLayoutParams(lp);

                Grid grid = new Grid(getApplicationContext(), pre.width, pre.height);
                fl.addView(grid);

                cam.setParameters(param);
                cam.startPreview();
            } catch (Exception e) { }
        }
        //終了時
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            cam.stopPreview();
            cam.release();
        }
    }

    //フォーカス
    class TakePictureClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            cam.autoFocus(autoFocusCallback);
        }

        private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                cam.takePicture(
                        null//new Camera.ShutterCallback() {           @Override        public void onShutter() {} }
                , null, new TakePictureCallback());
            }
        };
    }

    //写真撮影
    class TakePictureCallback implements Camera.PictureCallback {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                File dir = new File(
                        Environment.getExternalStorageDirectory(), "Camera");
                if(!dir.exists()) {
                    dir.mkdir();
                }
                //日にち
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
                //保存の名前
                String fn = sdf.format(date) + ".jpg";

                File f = new File(dir, fn);
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(data);

                String[] path = {Environment.getExternalStorageDirectory() + "/Camera/" + fn};
                String[] mimeType = {"image/jpeg"};
                //保存
                MediaScannerConnection.scanFile(getApplicationContext(), path, mimeType, null);

                Toast.makeText(getApplicationContext(),
                        fn+"写真を保存しました", Toast.LENGTH_LONG).show();
                fos.close();

                //byte to Drawable
                ByteArrayInputStream is = new ByteArrayInputStream(data);
                Drawable drw = Drawable.createFromStream(is, "articleImage");
                //画像使用
                btn.setBackground(drw);

                //カメラ再起動
                cam.startPreview();
            } catch (Exception e) { }
        }
    }
}
package com.example.filesavetest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private EditText editText;
    private String fileName_txt = "test.txt";
    private String fileName_img = "test.jpg";

    SharedPreferences prefs;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text_view);

        editText = findViewById(R.id.edit_text);
        editText.setText("edit");

        Button buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                // エディットテキストのテキストを取得
                String text = editText.getText().toString();

                saveFile(fileName_txt, text);
                if(text.length() == 0){
                    textView.setText(R.string.no_text);
                }
                else{
                    textView.setText(R.string.saved);
                }
            }
        });

        Button buttonRead = findViewById(R.id.button_read);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                String str = readFile(fileName_txt);
                if (str != null) {
                    textView.setText(str);
                } else {
                    textView.setText(R.string.read_error);
                }
            }
        });

        prefs = getSharedPreferences(
                "SaveData", Context.MODE_PRIVATE);


        ImageView imageView3 = findViewById(R.id.image_view_3);
        AssetManager assets = getResources().getAssets();

// try-with-resources
        try (InputStream istream = assets.open("515683.jpg")){
            Bitmap bitmap = BitmapFactory.decodeStream(istream);
            imageView3.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // ファイルを保存
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveFile(String file, String str) {

        AssetManager assets = getResources().getAssets();

        Bitmap bitmap = null;
        InputStream istream;
// try-with-resources
        try {
            istream = assets.open("515683.jpg");
            bitmap = BitmapFactory.decodeStream(istream);
            FileOutputStream  fileOutputstream = openFileOutput(fileName_img,
                    Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputstream);
            fileOutputstream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // try-with-resources
        try (FileOutputStream  fileOutputstream = openFileOutput(file,
                Context.MODE_PRIVATE);){

            fileOutputstream.write(str.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }


        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("string", "_value");
        editor.putBoolean("boolean", true);
        editor.putInt("int", 0);
        editor.putLong("long", 0);
        editor.putFloat("float", (float) 0.0);
        editor.apply();

    }

    // ファイルを読み出し
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String readFile(String file) {
        String text = null;

        // try-with-resources
        try (FileInputStream fileInputStream = openFileInput(file);
             BufferedReader reader= new BufferedReader(
                     new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))) {

            String lineBuffer;
            while( (lineBuffer = reader.readLine()) != null ) {
                text = lineBuffer ;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        String str = prefs.getString("string", String.valueOf(0));
        boolean bool = prefs.getBoolean("boolean",false);
        int intNum = prefs.getInt("int",1);
        long longNum = prefs.getLong("long",1);
        float floatNum = prefs.getFloat("float",1);

        return text + str;
    }
}
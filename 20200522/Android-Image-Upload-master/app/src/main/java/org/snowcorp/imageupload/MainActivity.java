package org.snowcorp.imageupload;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button btnChoose, btnUpload;
    private ProgressBar progressBar;

    public static String BASE_URL = "http://192.168.11.7:8080/uploadFile";
    static final int PICK_IMAGE_REQUEST = 1;
    String filePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        btnChoose = (Button) findViewById(R.id.button_choose);
        btnUpload = (Button) findViewById(R.id.button_upload);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                imageBrowse();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filePath != null) {
                    imageUpload(filePath);
                } else {
                    Toast.makeText(getApplicationContext(), "Image not selected!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void imageBrowse() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
        // ok
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Intent intent = new Intent(Intent.ACTION_PICK);
        Uri uri = Uri.parse("/storage/emulated/0" //Environment.getExternalStorageDirectory().getPath()
                +  File.separator + "Pictures" + File.separator);
        intent.setDataAndType(uri, "image/*");
        //intent.setDataAndType(uri, "*/*");
        //startActivity(Intent.createChooser(intent, "Open folder"));
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

        //  Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //Intent galleryIntent = new Intent(Intent.ACTION_PICK,
        //        Uri.parse("/storage/emulated/0"
        //         +  File.separator + "Pictures" + File.separator));
        //Intent galleryIntent = new Intent(Intent.ACTION_PICK, Uri.parse(DownloadManager.ACTION_VIEW_DOWNLOADS));
        // Start the Intent
        //startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
        //System.out.println("aa");

//        StorageManager sm = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
//        StorageVolume volume = sm.getPrimaryStorageVolume();
//        Intent intent = volume.createAccessIntent(Environment.DIRECTORY_PICTURES);
//        intent.setAction(Intent.ACTION_PICK);
//        startActivityForResult(intent, PICK_IMAGE_REQUEST);

     //   Intent intent = new Intent(Intent.ACTION_PICK);
     //   intent.setDataAndType(Uri.parse("content://sdcard/Pictures/"), "image/*");
     //   startActivityForResult(intent, 1);

//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
//        startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));

//        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        getIntent.setType("image/*");
//        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        pickIntent.setType("image/*");
//        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
//        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
//        startActivityForResult(chooserIntent, 1);

//        startChoosePicture(this, PICK_IMAGE_REQUEST);

    }

//    @TargetApi(Build.VERSION_CODES.KITKAT)
//    public static void startChoosePicture(Activity context, int code) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            intent.setType("image/*");
//            context.startActivityForResult(intent, code);
//        } else {
//            Intent intent = new Intent(Intent.ACTION_PICK);
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            intent.setType("image/*");
//            context.startActivityForResult(intent, code);
//        }
//    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if(requestCode == PICK_IMAGE_REQUEST){
                Uri picUri = data.getData();

                filePath = getPath(picUri);

                Log.d("picUri", picUri.toString());
                Log.d("filePath", filePath);

                imageView.setImageURI(picUri);

            }

        }

    }

    private void imageUpload(final String imagePath) {

        SimpleMultiPartRequest smr = new SimpleMultiPartRequest(Request.Method.POST, BASE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        try {
                            JSONObject jObj = new JSONObject(response);
                            String message = jObj.getString("message");

                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        smr.addFile("file", imagePath);
        smr.addStringParam("id","100");
        MyApplication.getInstance().addToRequestQueue(smr);

    }

    private String getPath(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

}

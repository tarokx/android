package com.example.test.ui.notifications;

import android.Manifest;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.test.MultipartRequest;
import com.example.test.MyApplication;
import com.example.test.R;

public class NotificationsFragment extends Fragment {
    private ImageView imageView;
    private Button btnChoose, btnUpload;

    //public static String BASE_URL = "http://192.168.11.7:8080/uploadFile";
    public static String BASE_URL = "http://192.168.10.47:8000/fileapi/do_upload";
    static final int PICK_IMAGE_REQUEST = 1;
    String filePath;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        return root;
    }
    @Override
    public void onStart() {
        super.onStart();

        imageView = (ImageView)getActivity().findViewById(R.id.imageView);
        btnChoose = (Button)getActivity().findViewById(R.id.button_choose);
        btnUpload = (Button)getActivity().findViewById(R.id.button_upload);

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
                    Toast.makeText(getActivity(), "Image not selected!", Toast.LENGTH_LONG).show();
                }

            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void imageBrowse() {

        ActivityCompat.requestPermissions(getActivity(),
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

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == -1 /*RESULT_OK*/) {

            if(requestCode == PICK_IMAGE_REQUEST){
                Uri picUri = data.getData();

                filePath = getPath(picUri);

                Log.d("picUri", picUri.toString());
                Log.d("filePath", filePath);

                imageView.setImageURI(picUri);

            }

        }

    }

    // python django use
    private void imageUpload(final String imagePath) {
        Map m = new HashMap<String, String>();
        m.put("id" +
                "", "501");
        MultipartRequest mr = new MultipartRequest(BASE_URL, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        try {
                            JSONObject jObj = new JSONObject(response);
                            String message = jObj.getString("message");

                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new File(imagePath), new File(imagePath).length(), m,
                null,
                "file", null);

        MyApplication.getInstance().addToRequestQueue(mr);
    }
    private String getPath(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getActivity(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

}

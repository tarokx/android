package com.example.test.ui.objsize;

import android.Manifest;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.example.test.MultipartRequest;
import com.example.test.MyApplication;
import com.example.test.R;
import com.example.test.sub.SubActivityImg;
import com.example.test.sub.SubActivityText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ObjSizeFragment extends Fragment {
    private ImageView imageView;
    private Button btnChoose, btnUpload;

    static final int PICK_IMAGE_REQUEST = 1;
    String filePath;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_objsize, container, false);

        return root;
    }
    @Override
    public void onStart() {
        super.onStart();

        imageView = (ImageView)getActivity().findViewById(R.id.imageView_objsize);
        btnChoose = (Button)getActivity().findViewById(R.id.button_choose_objsize);
        btnUpload = (Button)getActivity().findViewById(R.id.button_upload_objsize);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                imageBrowse();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
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
        MultipartRequest mr = new MultipartRequest(getString(R.string.ObjSizeUrl), new Response.ErrorListener() {
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

                            //WebView myWebView = (WebView) getActivity().findViewById(R.id.web_view);
                            //myWebView.setWebViewClient(new WebViewClient());
                            //myWebView.loadUrl(getString(R.string.ViewImg));

                            // インテントの作成
                            Intent intent = new Intent(getActivity(), SubActivityImg.class);
                            //データをセット
                            intent.putExtra("sendText",message);
                            //遷移先の画面を起動
                            startActivity(intent);
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

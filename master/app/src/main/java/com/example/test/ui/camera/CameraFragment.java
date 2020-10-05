package com.example.test.ui.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.test.R;

public class CameraFragment extends Fragment {

    private final static int RESULT_CAMERA = 1001;
    private ImageView imageView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        imageView = getActivity().findViewById(R.id.image_view);

        Button cameraButton = getActivity().findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, RESULT_CAMERA);


            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CAMERA) {
            Bitmap bitmap;
            // cancelしたケースも含む
            if (data.getExtras() == null) {
                Log.d("debug", "cancel ?");
                return;
            } else {
                bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    // 画像サイズを計測
                    int bmpWidth = bitmap.getWidth();
                    int bmpHeight = bitmap.getHeight();
                    Log.d("debug", String.format("w= %d", bmpWidth));
                    Log.d("debug", String.format("h= %d", bmpHeight));
                }
            }

            imageView.setImageBitmap(bitmap);
        }
    }
}

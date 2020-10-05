package com.example.test.ui.camera

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.test.R

class CameraFragment : Fragment() {
    private val RESULT_CAMERA = 1001
    private var imageView: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onStart() {
        super.onStart()
        imageView = requireActivity().findViewById(R.id.image_view)
        val cameraButton =
            requireActivity().findViewById<Button>(R.id.camera_button)
        cameraButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(
                intent,
                RESULT_CAMERA
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CAMERA) {
            val bitmap: Bitmap?
            // cancelしたケースも含む
            if (data == null) {
                Log.d("debug", "cancel ?")
                return
            } else {
                bitmap = data.extras!!["data"] as Bitmap?
                if (bitmap != null) {
                    // 画像サイズを計測
                    val bmpWidth = bitmap.width
                    val bmpHeight = bitmap.height
                    Log.d("debug", String.format("w= %d", bmpWidth))
                    Log.d("debug", String.format("h= %d", bmpHeight))
                }
            }
            imageView!!.setImageBitmap(bitmap)
        }
    }
}
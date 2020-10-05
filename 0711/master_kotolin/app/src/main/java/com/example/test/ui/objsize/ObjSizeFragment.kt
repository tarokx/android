package com.example.test.ui.objsize

import android.Manifest
import android.content.CursorLoader
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.example.test.MultipartRequest
import com.example.test.MyApplication
import com.example.test.R
import com.example.test.sub.SubActivityImg
import com.example.test.sub.SubActivityText
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

class ObjSizeFragment : Fragment() {
    var filePath: String? = null
    private var imageView: ImageView? = null
    private var btnChoose: Button? = null
    private var btnUpload: Button? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_objsize, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()
        imageView =
            requireActivity().findViewById<View>(R.id.imageView_objsize) as ImageView
        btnChoose =
            requireActivity().findViewById<View>(R.id.button_choose_objsize) as Button
        btnUpload =
            requireActivity().findViewById<View>(R.id.button_upload_objsize) as Button
        btnChoose!!.setOnClickListener { imageBrowse() }
        btnUpload!!.setOnClickListener {
            if (filePath != null) {
                imageUpload(filePath!!)
            } else {
                Toast.makeText(activity, "Image not selected!", Toast.LENGTH_LONG).show()
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun imageBrowse() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            1
        )
        // ok
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        val intent = Intent(Intent.ACTION_PICK)
        val uri = Uri.parse(
            "/storage/emulated/0" //Environment.getExternalStorageDirectory().getPath()
                    + File.separator + "Pictures" + File.separator
        )
        intent.setDataAndType(uri, "image/*")
        //intent.setDataAndType(uri, "*/*");
        //startActivity(Intent.createChooser(intent, "Open folder"));
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == -1 /*RESULT_OK*/) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                val picUri = data!!.data
                filePath = getPath(picUri)
                Log.d("picUri", picUri.toString())
                Log.d("filePath", filePath)
                imageView!!.setImageURI(picUri)
            }
        }
    }

    // python django use
    private fun imageUpload(imagePath: String) {
        val m = kotlin.collections.HashMap<String , String>()
        m["id"]="501"
        val mr = MultipartRequest(
            getString(R.string.BaseUrl) + getString(R.string.ObjSizeUrl),
            Response.ErrorListener { error ->
                Toast.makeText(
                    activity,
                    error.message,
                    Toast.LENGTH_LONG
                ).show()
            },
            Response.Listener { response ->
                Log.d("Response", response)
                try {
                    val jObj = JSONObject(response)
                    val message = jObj.getString("message")
                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show()

                    // インテントの作成
                    val intent = Intent(activity, SubActivityImg::class.java)
                    //データをセット
                    intent.putExtra("sendText", message)
                    //遷移先の画面を起動
                    startActivity(intent)
                } catch (e: JSONException) {
                    // JSON error
                    e.printStackTrace()
                    Toast.makeText(activity, "Json error: " + e.message, Toast.LENGTH_LONG)
                        .show()
                }
            },
            File(imagePath),
            File(imagePath).length(),
            m,
            null,
            "file",
            null
        )
        MyApplication.instance?.addToRequestQueue(mr)
    }

    private fun getPath(contentUri: Uri?): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val loader =
            CursorLoader(activity, contentUri, proj, null, null, null)
        val cursor = loader.loadInBackground()
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val result = cursor.getString(column_index)
        cursor.close()
        return result
    }

    companion object {
        //public static String BASE_URL = "http://192.168.11.7:8080/uploadFile";
        //public static String BASE_URL = "http://192.168.10.47:8000/fileapi/do_upload";
        const val PICK_IMAGE_REQUEST = 1
    }
}
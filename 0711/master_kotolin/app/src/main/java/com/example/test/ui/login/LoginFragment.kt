package com.example.test.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.example.test.MultipartRequest
import com.example.test.MyApplication
import com.example.test.R
import com.example.test.sub.SubActivityText
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onStart() {
        super.onStart()
        val usernameEditText = requireActivity().findViewById<EditText>(R.id.username)
        val passwordEditText = requireActivity().findViewById<EditText>(R.id.password)
        val loginButton =
            requireActivity().findViewById<Button>(R.id.login)
        loginButton.setOnClickListener { loginUpload(usernameEditText.text, passwordEditText.text) }
    }

    private fun loginUpload(id: Editable, ps: Editable) {
        val m = kotlin.collections.HashMap<String , String>()
        m["id"]=id.toString()
        m["pass"]=ps.toString()
        val mr = MultipartRequest(
            getString(R.string.BaseUrl) + getString(R.string.LoginUrl),
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
                    Toast.makeText(activity, "$message!", Toast.LENGTH_LONG).show()

                    print(message)

                    // インテントの作成
                    val intent = Intent(activity, SubActivityText::class.java)
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
            null,
            0,
            m,
            null,
            "file",
            null
        )
        MyApplication.instance?.addToRequestQueue(mr)
    }
}
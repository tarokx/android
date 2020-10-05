package com.example.test.sub

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.test.R

class SubActivityText : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sub_show_text)
        val logoutButton = findViewById<Button>(R.id.back)
        val textView = findViewById<TextView>(R.id.textView_sub)
        logoutButton.setOnClickListener { finish() }
        val intent = this.intent
        val text = intent.getStringExtra("sendText")
        textView.text = text
    }
}
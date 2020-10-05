package com.example.timer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var cnt=0
    var time=0
    val hnd0=Handler()
    var flag=false
    val rnb0=object : Runnable{
        override fun run() {
            t0.text=cnt.toString()
            t1.text=time.toString()
            hnd0.postDelayed(this,10)
            if(flag){
                cnt++
                time=0
            }else{
                time++
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button1.setOnClickListener{
            flag = !flag
        }
        hnd0.post(rnb0)
    }
}
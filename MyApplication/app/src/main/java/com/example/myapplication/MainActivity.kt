package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1
        val handler: Handler = MyHandler()

// 2
        val msg: Message = handler.obtainMessage(MyHandler.MSG_DO_SOMETHING1)
        handler.handleMessage(msg)

// 3
        handler.sendEmptyMessage(MyHandler.MSG_DO_SOMETHING2)

// 4
        val msg2 = Message.obtain(handler, MyHandler.MSG_DO_SOMETHING3)
        handler.handleMessage(msg2)
    }
}
class MyHandler : Handler() {
    companion object {
        const val TAG = "MyHandler"
        const val MSG_DO_SOMETHING1 = 1
        const val MSG_DO_SOMETHING2 = 2
        const val MSG_DO_SOMETHING3 = 3
        const val MSG_DO_SOMETHING4 = 4
    }
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            MSG_DO_SOMETHING1 -> {
                Log.d(TAG, "Do something1")
            }
            MSG_DO_SOMETHING2 -> {
                Log.d(TAG, "Do something2")
            }
            MSG_DO_SOMETHING3 -> {
                Log.d(TAG, "Do something3")
            }
            MSG_DO_SOMETHING4 -> {
                Log.d(TAG, "Do something4, arg1: ${msg.arg1}," +
                        " arg2: ${msg.arg2}, obj: ${msg.obj}")
            }
        }
    }
}
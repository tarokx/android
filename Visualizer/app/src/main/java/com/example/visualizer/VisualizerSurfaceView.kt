package com.example.visualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView

open class VisualizerSurfaceView : SurfaceView, SurfaceHolder.Callback, Runnable {

    val _paint = Paint()
    var _buffer: ShortArray = ShortArray(0)
    var _holder: SurfaceHolder
    var _thread: Thread? = null
    var _view: View? = null
    var _text: TextView
    var _t1: TextView
    var time=0

    override fun run() {
        while(_thread != null){
            doDraw(_holder)
        }
    }

    constructor(view: MainActivity, context: Context, surface: SurfaceView)
            : super(context) {

        _text = view.findViewById(R.id.textView)
        _t1 = view.findViewById(R.id.t1)

        _holder = surface.holder
        _holder.addCallback(this)

        // 線の太さ、アンチエイリアス、色、とか
        _paint.strokeWidth  = 2f
        _paint.isAntiAlias  = true
        _paint.color        = Color.WHITE

        // この2つを書いてフォーカスを当てないとSurfaceViewが動かない？
//        isFocusable = true
//        requestFocus()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if(holder != null){
            val canvas = holder.lockCanvas()

            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        _thread = Thread(this)
        _thread?.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        _thread = null
    }

    fun update(buffer: ShortArray, size: Int) {
        _buffer = buffer.copyOf(size)
//      postInvalidate()
    }

    private fun doDraw(holder: SurfaceHolder) {
        if(_buffer.size == 0){
            return
        }

        try {
            val canvas: Canvas = holder.lockCanvas()

            if (canvas != null) {
                canvas.drawColor(Color.BLACK)

                val baseLine: Float = canvas.height / 2f
                var oldX: Float = 0f
                var oldY: Float = baseLine

                for ((index, value) in _buffer.withIndex()) {
                    val x: Float = canvas.width.toFloat() / _buffer.size.toFloat() * index.toFloat()
                    val y: Float = value / 128 + baseLine

                    canvas.drawLine(oldX, oldY, x, y, _paint)

                    oldX = x
                    oldY = y
                }

                _buffer = ShortArray(0)

                holder.unlockCanvasAndPost(canvas)


                _text.text=oldY.toString()
                if (oldY < baseLine + 5 && oldY > baseLine - 5 ){
                    _text.setBackgroundColor(Color.RED)
                    time++
                }else{
                    _text.setBackgroundColor(Color.GREEN)
                    time=0
                }
                _t1.text=time.toString()
            }
        }catch(e: Exception){
            Log.e(this.javaClass.name, "doDraw", e)
        }
    }
}
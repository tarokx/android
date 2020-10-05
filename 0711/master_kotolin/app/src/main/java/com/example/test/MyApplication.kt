package com.example.test

import android.app.Application
import android.text.TextUtils
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * Created by Akshay Raj on 7/17/2016.
 * Snow Corporation Inc.
 * www.snowcorp.org
 */
class MyApplication : Application() {
    private var mRequestQueue: RequestQueue? = null

    val TAG = MyApplication::class.java.simpleName
    private val mInstance: MyApplication? = null

    @Synchronized
    internal fun getInstance(): MyApplication? {
        return mInstance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    val requestQueue: RequestQueue?
        get() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(applicationContext)
            }
            return mRequestQueue
        }

    fun <T> addToRequestQueue(
        req: Request<T>,
        tag: String?
    ) {
        req.tag = if (TextUtils.isEmpty(tag)) TAG else tag
        requestQueue!!.add(req)
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        req.tag = TAG
        requestQueue!!.add(req)
    }

    fun cancelPendingRequests(tag: Any?) {
        if (mRequestQueue != null) {
            mRequestQueue!!.cancelAll(tag)
        }
    }

    companion object {
        val TAG = MyApplication::class.java.simpleName

        @get:Synchronized
        var instance: MyApplication? = null
            private set
    }
}
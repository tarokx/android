package com.example.test

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyLog
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.util.CharsetUtils
import java.io.*

//import com.android.volley.AuthFailureError;
//import com.beusoft.app.AppContext;
class MultipartRequest(
    url: String?,
    errorListener: Response.ErrorListener?,
    private val mListener: Response.Listener<String>,
    private val mFilePart: File?,
    fileLength: Long,
    mStringPart: Map<String, String>?,
    headerParams: Map<String, String>?,
    partName: String,
    progLitener: MultipartProgressListener?
) : Request<String>(
    Method.POST,
    url,
    errorListener
) {
    private var mStringPart: Map<String, String>? = null
    private var multipartProgressListener: MultipartProgressListener? = null
    var entity: MultipartEntityBuilder = MultipartEntityBuilder.create()
    private var httpentity: HttpEntity? = null
    private var FILE_PART_NAME = "files"
    private var headerParams: Map<String, String>? = null
    private var fileLength = 0L

    // public void addStringBody(String param, String value) {
    // if (mStringPart != null) {
    // mStringPart.put(param, value);
    // }
    // }
    private fun buildMultipartEntity() {
        if (mFilePart != null) {
            entity.addPart(
                FILE_PART_NAME,
                FileBody(
                    mFilePart,
                    ContentType.create("image/gif"),
                    mFilePart.name
                )
            )
        }
        if (mStringPart != null) {
            for ((key, value) in mStringPart!!) {
                entity.addTextBody(key, value)
            }
        }
    }

    override fun getBodyContentType(): String? {
        return httpentity?.contentType?.value
    }

    override fun getBody(): ByteArray /*throws AuthFailureError*/ {
        val bos = ByteArrayOutputStream()
        try {
            httpentity?.writeTo(
                CountingOutputStream(
                    bos, fileLength,
                    multipartProgressListener
                )
            )
        } catch (e: IOException) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream")
        }
        return bos.toByteArray()
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
        return try {
            //          System.out.println("Network Response "+ new String(response.data, "UTF-8"));
            Response.success(
                String(response.data),
                cacheEntry
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            // fuck it, it should never happen though
            Response.success(
                String(response.data),
                cacheEntry
            )
        }
    }

    override fun deliverResponse(response: String) {
        mListener.onResponse(response)
    }

    //Override getHeaders() if you want to put anything in header
    interface MultipartProgressListener {
        fun transferred(transfered: Long, progress: Int)
    }

    class CountingOutputStream(
        out: OutputStream?, private val fileLength: Long,
        private val progListener: MultipartProgressListener?
    ) :
        FilterOutputStream(out) {
        private var transferred: Long = 0

        @Throws(IOException::class)
        override fun write(b: ByteArray, off: Int, len: Int) {
            out.write(b, off, len)
            if (progListener != null) {
                transferred += len.toLong()
                val prog = (transferred * 100 / fileLength).toInt()
                progListener.transferred(transferred, prog)
            }
        }

        @Throws(IOException::class)
        override fun write(b: Int) {
            out.write(b)
            if (progListener != null) {
                transferred++
                val prog = (transferred * 100 / fileLength).toInt()
                progListener.transferred(transferred, prog)
            }
        }

    }

    init {
        this.fileLength = fileLength
        this.mStringPart = mStringPart
        if (headerParams != null) {
            this.headerParams = headerParams
        }
        FILE_PART_NAME = partName
        if (progLitener != null) {
            multipartProgressListener = progLitener
        }
        entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
        try {
            entity.setCharset(CharsetUtils.get("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        buildMultipartEntity()
        httpentity = entity.build()
    }
}
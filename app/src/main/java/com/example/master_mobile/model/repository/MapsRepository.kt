package com.example.master_mobile.model.repository

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

const val TAG = "MapsRepository"

class MapsRepository() {
    private val client = OkHttpClient()
    private val baseUrl = "https://mongoapi-lr9d.onrender.com"

    fun getStressData() {
        Log.d(TAG, "getStressData: requesting..")

        val request = Request.Builder()
            .url("$baseUrl/all")
            .build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use{
                    if(!response.isSuccessful) throw IOException("Unexpected code $response")
                    
                    val result = response.body?.string() ?: ""
                    Log.d(TAG, "onResponse: result-> $result")
                }
            }
        })
    }

    fun get(url: String, callback: Callback): Call{
        val request = Request.Builder()
            .url(url)
            .build()

        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

}
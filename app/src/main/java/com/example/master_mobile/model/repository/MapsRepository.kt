package com.example.master_mobile.model.repository

import android.util.Log
import com.example.master_mobile.model.StressData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import java.util.concurrent.TimeUnit

const val TAG = "MapsRepository"

class MapsRepository() {
    private val client = OkHttpClient()
    private val baseUrl = "https://ok.mimic.uiocloud.no"
    val builder = OkHttpClient.Builder()
    
    interface StressDataCallback {
        // Called when request succeeds
        fun onSuccess(data: List<StressData>)

        // Called when request fails
        fun onError(e: IOException)
    }

    fun getStressDataInDateRange(startDate: Long, endDate: Long, callBack: StressDataCallback){
        Log.d(TAG, "getStressDataInDateRange: requesting for $startDate, - $endDate")
        val _client = OkHttpClient()
        val _builder = OkHttpClient.Builder()
        //TODO: datoene må sendes i stringformat til endepunkt
        //unix

        //endeupuntk /between
        // start og end

        // Configure timeouts as needed
        _builder.connectTimeout(100, TimeUnit.SECONDS)
        _builder.readTimeout(100, TimeUnit.SECONDS)
        _builder.writeTimeout(100, TimeUnit.SECONDS)

        val jsonObject = JSONObject()

        try{
            jsonObject.put("start", startDate!!.toString())
            jsonObject.put("end", endDate!!.toString())
        } catch (e: JSONException){
            e.printStackTrace()
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonObject.toString().toRequestBody(mediaType)

        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("ok.mimic.uiocloud.no")
            .addPathSegment("between")
            .addQueryParameter("start", startDate!!.toString())
            .addQueryParameter("end", endDate!!.toString())
            .build()


        val request = Request.Builder()
            .url(httpUrl)
            //.method("GET", null)
            .build()

        Log.d(TAG, "getStressDataInDateRange: request: $request")
        _client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                callBack.onError(e)
                e.printStackTrace()
            }
            
            override fun onResponse(call: Call, response: Response) {
                response.use{
                    if(!response.isSuccessful) {
                        // Server responded with an error code
                        callBack.onError(IOException("Unexpected code $response"))
                        return
                    }

                    val result = response.body?.string() ?: ""
                    Log.d(TAG, "onResponse: result: $result")

                    // use Gson to parse JSON to kotlin objects
                    val gson = Gson()

                    val stressDataListType = object : TypeToken<Array<StressData>>() {}.type
                    val stressDataArray: Array<StressData> = gson.fromJson(result, stressDataListType)
                    val stressDataList: List<StressData> = stressDataArray.toList()


                    Log.d(TAG, "onResponse: stressDataList: ${stressDataList}")

                    // Return parsed dat through the callback
                    callBack.onSuccess(stressDataList)
                }
            }
        })

        Log.d(TAG, "getStressDataInDateRange: done")
    }

    fun getStressData(callBack: StressDataCallback) {
        Log.d(TAG, "getStressData: requesting..")

        // Configure timeouts as needed
        builder.connectTimeout(100, TimeUnit.SECONDS)
        builder.readTimeout(100, TimeUnit.SECONDS)
        builder.writeTimeout(100, TimeUnit.SECONDS)
        
        
        val request = Request.Builder()
            .url("$baseUrl/all")
            .build()
        Log.d(TAG, "getStressData: request: $request")
        // fetch stress data and pass back via StressDataCallback
        client.newCall(request).enqueue(object : Callback{
            // onFailure is called by the OkHttpClient if the request fails (network problem or request cancelled)
            override fun onFailure(call: Call, e: IOException) {
                callBack.onError(e)
                e.printStackTrace()
            }

            // onResponse is called by the OkHttpClient if the server responds (either successfully or with an error code)
            override fun onResponse(call: Call, response: Response) {
                response.use{
                    if(!response.isSuccessful) {
                        // Server responded with an error code
                        callBack.onError(IOException("Unexpected code $response"))
                        return
                    }
                    
                    val result = response.body?.string() ?: ""
                    Log.d(TAG, "onResponse: result: $result")
                    
                    // use Gson to parse JSON to kotlin objects
                    val gson = Gson()

                    val stressDataListType = object : TypeToken<Array<StressData>>() {}.type
                    val stressDataArray: Array<StressData> = gson.fromJson(result, stressDataListType)
                    val stressDataList: List<StressData> = stressDataArray.toList()


                    Log.d(TAG, "onResponse: stressDataList: ${stressDataList[0]}")

                    // Return parsed dat through the callback
                    callBack.onSuccess(stressDataList)
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
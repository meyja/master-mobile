package com.example.master_mobile.model.repository

import android.util.Log
import com.example.master_mobile.model.StressData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

const val TAG = "MapsRepository"

class MapsRepository() {
    private val client = OkHttpClient()
    private val baseUrl = "ok.mimic.uiocloud.no"
    val builder = OkHttpClient.Builder()
    
    interface StressDataCallback {
        // Called when request succeeds
        fun onSuccess(data: ArrayList<StressData>)

        // Called when request fails
        fun onError(e: IOException)
    }

    fun getResponse(httpUrl: HttpUrl, callBack: StressDataCallback){
        // Configure timeouts as needed
        builder.connectTimeout(100, TimeUnit.SECONDS)
        builder.readTimeout(100, TimeUnit.SECONDS)
        builder.writeTimeout(100, TimeUnit.SECONDS)

        val request = Request.Builder()
            .url(httpUrl)
            .build()
        Log.d(TAG, "getResponse: request: $request")

        // fetch stress data and pass back via StressDataCallback
        client.newCall(request).enqueue(object : Callback{
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


                    //TODO se på castingen her, det er shait
                    val stressDataListType = object : TypeToken<Array<StressData>>() {}.type
                    val stressDataArray: Array<StressData> = gson.fromJson(result, stressDataListType)
                    val stressDataList: ArrayList<StressData> = ArrayList(stressDataArray.toList())


                    Log.d(TAG, "onResponse: stressDataList: ${stressDataList}")

                    // Return parsed dat through the callback
                    callBack.onSuccess(stressDataList)
                }
            }
        })
    }

    fun getStressDataInDateRange(startDate: Long, endDate: Long, callBack: StressDataCallback){
        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host(baseUrl)
            .addPathSegment("between")
            .addQueryParameter("start", startDate.toString())
            .addQueryParameter("end", endDate.toString())
            .build()

        Log.d(TAG, "getStressDataInDateRange: requesting for $startDate, - $endDate")

        /*
        client.newCall(request).enqueue(object : Callback{
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
         */

        getResponse(httpUrl, callBack)
    }

    fun getStressData(callBack: StressDataCallback) {
        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host(baseUrl)
            .addPathSegment("all")
            .build()

        getResponse(httpUrl, callBack)

        /*
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

         */
    }
}
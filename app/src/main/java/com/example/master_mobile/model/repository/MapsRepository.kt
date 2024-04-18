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
import java.util.Calendar
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
        // if startDate = endDate, convert the two to date at start of day and date at end of day respectively
        // if not the same, use the original values from parameters
        val (newStartDate, newEndDate) = if (startDate == endDate) {
            getStartAndEndOfDate(startDate)
        } else {
            startDate to endDate
        }

        Log.d(TAG, "getStressDataInDateRange: requesting for $newStartDate - $newEndDate")
        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host(baseUrl)
            .addPathSegment("between")
            .addQueryParameter("start", newStartDate.toString())
            .addQueryParameter("end", newEndDate.toString())
            .build()

        getResponse(httpUrl, callBack)
    }

    /**
     * Get stressdata within the last 24 hours
     */
    fun getStressDataLast24Hrs(callBack: StressDataCallback){
        Log.d(TAG, "getStressDataLast24Hrs: get data last 24 hours")
        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host(baseUrl)
            .addPathSegment("last24h")
            .build()

        getResponse(httpUrl, callBack)
    }

    /**
     * Get all stresssdata
     */
    fun getStressData(callBack: StressDataCallback) {
        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host(baseUrl)
            .addPathSegment("all")
            .build()

        getResponse(httpUrl, callBack)
    }

    /**
     * Convert a date to two dates, where one is the start of day and the other is the end of day
     */
    fun getStartAndEndOfDate(date: Long): Pair<Long, Long> {
        val dateInMilliSeconds = date // your date in milli time
        val cal = Calendar.getInstance()

        // set the calendar time to your date
        cal.timeInMillis = dateInMilliSeconds

        // get start of the day
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDayInMilliSeconds = cal.timeInMillis
        println(startOfDayInMilliSeconds)

        // get end of the day
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endOfDayInMilliSeconds = cal.timeInMillis
        println(endOfDayInMilliSeconds)
        return Pair(startOfDayInMilliSeconds, endOfDayInMilliSeconds)
    }
}
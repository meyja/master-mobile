package com.example.master_mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.master_mobile.model.StressData
import com.example.master_mobile.model.repository.MapsRepository
import java.io.IOException
import java.util.Calendar

const val TAG = "MapsViewModel"

class MapsViewModel(
    private val mapsRepository: MapsRepository,
) : ViewModel() {

    private val mutableStressData = MutableLiveData<ArrayList<StressData>>()
    val stressDataList: LiveData<ArrayList<StressData>> = mutableStressData
    private val error =
        MutableLiveData<Exception>()  // If you want to handle network errors in your Activity

    // when MapsViewModel is created, fetch stress data
    init {
        fetchStressDataLast24Hrs()
    }

    /**
     * Fetches stress data for the last 24 hours and updates the stress data live data.
     *
     * This method retrieves stress data points from the past 24 hours using the `MapsRepository`.
     * The retrieved data is then posted to the `mutableStressData` LiveData to update the UI.
     * In case of errors during data fetching, the error is posted to the `error` LiveData for handling.
     */
    fun fetchStressDataLast24Hrs(){
        mapsRepository.getStressDataLast24Hrs(object : MapsRepository.StressDataCallback {
            override fun onSuccess(data: ArrayList<StressData>) {
                //mutableStressData.postValue(data as ArrayList<StressData>?)
                postStressData(data)
            }

            override fun onError(e: IOException) {
                //error.postValue(e)
                postError(e)
            }
        })
    }

    /**
     * Fetches all stress data within the database and updates the stress data live data.
     *
     * This method retrieves all stress data points from the data source using the `MapsRepository`.
     * The retrieved data is then posted to the `mutableStressData` LiveData to update the UI.
     * In case of errors during data fetching, the error is posted to the `error` LiveData for handling.
     */
    private fun fetchStressDataAll() {
        // call getStressData with callback to handle the response
        mapsRepository.getStressData(object : MapsRepository.StressDataCallback {
            override fun onSuccess(data: ArrayList<StressData>) {
                //mutableStressData.postValue(data as ArrayList<StressData>?)
                postStressData(data)
            }

            override fun onError(e: IOException) {
                //error.postValue(e)
                postError(e)
            }
        })
    }

    /**
     * Fetches stress data within a selected date range and updates the stress data live data.
     *
     * This method retrieves stress data points within the provided `startDate` and `endDate` range
     * using the `MapsRepository`. It also handles the case where the user selects a single day.
     * The retrieved data is then posted to the `mutableStressData` LiveData to update the UI.
     * In case of errors during data fetching, the error is posted to the `error` LiveData for handling.
     *
     * @param startDate The start date of the range in milliseconds.
     * @param endDate The end date of the range in milliseconds.
     */
    fun fetchStressDataInDataRange(startDate: Long, endDate: Long) {
        // if startDate = endDate, convert the two to date at start of day and date at end of day respectively
        // if not the same, use the original values from parameters
        val (newStartDate, newEndDate) = if (startDate == endDate) {
            getStartAndEndOfDate(startDate)
        } else {
            startDate to endDate
        }

        mapsRepository.getStressDataInDateRange(newStartDate, newEndDate, object: MapsRepository.StressDataCallback{
            override fun onSuccess(data: ArrayList<StressData>) {
                //mutableStressData.postValue(data as ArrayList<StressData>?)
                postStressData(data)
            }

            override fun onError(e: IOException) {
                //error.postValue(e)
                postError(e)
            }
        })
    }

    /**
     * Converts a date to two dates, where one is the start of the day and the other is the end of the day.
     * @param date The date to convert
     * @return A Pair of Long values representing the start and end of the day in milliseconds
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

        // get end of the day
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endOfDayInMilliSeconds = cal.timeInMillis
        return Pair(startOfDayInMilliSeconds, endOfDayInMilliSeconds)
    }

    /**
     * Posts the updated stress data to the live data
     * @param data The updated stress data
     */
    private fun postStressData(data: ArrayList<StressData>){
        mutableStressData.postValue(data as ArrayList<StressData>?)
    }

    /**
     * Posts an error to the live data
     * @param e The IOException to be posted as an error
     */
    private fun postError(e: IOException){
        error.postValue(e)
    }
}
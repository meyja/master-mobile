package com.example.master_mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.master_mobile.model.StressData
import com.example.master_mobile.model.repository.MapsRepository
import java.io.IOException

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
     * Fetching stressdata within the last 24 hours
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
     * Fetching all stressdata within the database
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
     * Fetching stressdata withing a selected data range
     */
    fun fetchStressDataInDataRange(startDate: Long, endDate: Long) {
        mapsRepository.getStressDataInDateRange(startDate, endDate, object: MapsRepository.StressDataCallback{
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

    private fun postStressData(data: ArrayList<StressData>){
        mutableStressData.postValue(data as ArrayList<StressData>?)
    }

    private fun postError(e: IOException){
        error.postValue(e)
    }
}
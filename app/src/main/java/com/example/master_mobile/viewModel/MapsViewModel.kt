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
        fetchStressData()
    }

    private fun fetchStressData() {
        // call getStressData with callback to handle the response
        mapsRepository.getStressData(object : MapsRepository.StressDataCallback {
            override fun onSuccess(data: List<StressData>) {
                //mutableStressData.postValue(data as ArrayList<StressData>?)
                postStressData(data)
            }

            override fun onError(e: IOException) {
                //error.postValue(e)
                postError(e)
            }
        })
    }

    fun fetchStressDataInDataRange(startDate: Long, endDate: Long) {
        mapsRepository.getStressDataInDateRange(startDate, endDate, object: MapsRepository.StressDataCallback{
            override fun onSuccess(data: List<StressData>) {
                //mutableStressData.postValue(data as ArrayList<StressData>?)
                postStressData(data)
            }

            override fun onError(e: IOException) {
                //error.postValue(e)
                postError(e)
            }

        })
    }

    private fun postStressData(data: List<StressData>){
        mutableStressData.postValue(data as ArrayList<StressData>?)
    }

    private fun postError(e: IOException){
        error.postValue(e)
    }
}
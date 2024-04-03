package com.example.master_mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.master_mobile.model.TempStressData
import com.example.master_mobile.model.repository.MapsRepository
import java.io.IOException

class MapsViewModel(
    private val mapsRepository: MapsRepository,
) : ViewModel() {
    private val mutableStressData = MutableLiveData<List<TempStressData>>()
    val stressDataList: LiveData<List<TempStressData>> = mutableStressData
    private val error =
        MutableLiveData<Exception>()  // If you want to handle network errors in your Activity

    init {
        fetchStressData()
    }

    private fun fetchStressData() {

        // call getStressData with callback to handle the response
        mapsRepository.getStressData(object : MapsRepository.StressDataCallback {
            override fun onSuccess(data: List<TempStressData>) {
                mutableStressData.postValue(data)
            }

            override fun onError(e: IOException) {
                error.postValue(e)
            }
        })
    }
    //TODO må lage noe type Factory?
}
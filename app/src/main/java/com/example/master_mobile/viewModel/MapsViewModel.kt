package com.example.master_mobile.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.master_mobile.model.TempStressData
import com.example.master_mobile.model.repository.MapsRepository
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
const val TAG = "MapsViewModel"
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

    fun getLatLongs(): List<LatLng?>? {
        val latLonList = stressDataList.value?.map { tempStressData ->
            LatLng(
                tempStressData.lat.toDouble(),
                tempStressData.lon.toDouble()
            )
        }
        Log.d(TAG, "getLatLongs: latLonList: $latLonList")
        return latLonList ?: emptyList()
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
}
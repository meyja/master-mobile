package com.example.master_mobile.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import com.example.master_mobile.R
import com.example.master_mobile.databinding.ActivityMapsBinding
import com.example.master_mobile.model.repository.MapsRepository
import com.example.master_mobile.viewModel.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider
import org.json.JSONArray
import org.json.JSONException
import java.util.Scanner


const val TAG = "MapsActivity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var viewModel: MapsViewModel
    private var heatMapOverlay: TileOverlay? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapsRepository = MapsRepository()

        viewModel = MapsViewModel(mapsRepository)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Observe the LiveData for stress data
        viewModel.stressDataList.observe(this) { newData ->
            // This callback will be invoked whenever the stressDataList changes.
            // Update the heatmap with the new data.
            newData?.let { it ->
                updateHeatMap(it.map { data -> LatLng(data.lat.toDouble(), data.lon.toDouble()) })
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val oslo = LatLng(59.9, 10.75)
        mMap.addMarker(MarkerOptions().position(oslo).title("Marker in Oslo"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oslo, 12F))
        mMap.uiSettings.isZoomControlsEnabled = true

        addHeatMap()
    }

    // source: https://developers.google.com/maps/documentation/android-sdk/utility/heatmap
    private fun addHeatMap() {
        var osloLatLon = emptyList<LatLng?>()
        // Get the data: latitude/longitude positions of police stations.
        try {
            osloLatLon = readItems(R.raw.coordinates)


        } catch (e: JSONException) {
            Toast.makeText(this, "Problem reading list of locations.", Toast.LENGTH_LONG).show()
        }
        /*
        var latLngs: List<LatLng?>? = null
        latLngs = viewModel.getLatLongs()
        Log.d(TAG, "addHeatMap: latLngs: $latLngs")

        if (latLngs == emptyList<LatLng>()) latLngs = osloLatLon

        // Create a heat map tile provider, passing it the latlngs
        val provider = HeatmapTileProvider.Builder().data(latLngs).build()

        // Add a tile overlay to the map, using the heat map tile provider.
        mMap.addTileOverlay(TileOverlayOptions().tileProvider(provider))

         */

        // Initial data, can be an empty list.
        //val initialData = emptyList<LatLng>()
        val initialData = osloLatLon
        val heatmapTileProvider = HeatmapTileProvider.Builder()
            .data(initialData)
            .build()
        heatMapOverlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(heatmapTileProvider))
    }

    fun updateHeatMap(newLatLons: List<LatLng>) {
        // Clear the old heatmap
        heatMapOverlay?.remove()

        // Create a heat map tile provider, passing it the new data.
        val heatmapTileProvider = HeatmapTileProvider.Builder()
            .data(newLatLons)
            .build()

        // Add a tile overlay to the map, using the new heat map tile provider.
        heatMapOverlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(heatmapTileProvider))
    }

    @Throws(JSONException::class)
    private fun readItems(@RawRes resource: Int): List<LatLng?> {
        val result: MutableList<LatLng?> = ArrayList()
        val inputStream = this.resources.openRawResource(resource)
        val json = Scanner(inputStream).useDelimiter("\\A").next()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val `object` = array.getJSONObject(i)
            val lat = `object`.getDouble("lat")
            val lng = `object`.getDouble("lng")
            result.add(LatLng(lat, lng))
        }
        return result
    }
}
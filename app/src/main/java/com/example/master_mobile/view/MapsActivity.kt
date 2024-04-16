package com.example.master_mobile.view

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.maps.android.heatmaps.HeatmapTileProvider
import androidx.core.util.Pair
import com.google.android.material.datepicker.CalendarConstraints
import java.util.Calendar
import java.util.TimeZone

const val TAG = "MapsActivity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var viewModel: MapsViewModel
    private var heatMapOverlay: TileOverlay? = null
    private var latLons: ArrayList<LatLng> = arrayListOf()


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
                latLons = it.map { data ->
                    LatLng(
                        data.lat.toDouble(), data.lon.toDouble()
                    )
                } as ArrayList<LatLng>
                updateHeatMap()
            }
        }/*

        setContent {
            MastermobileTheme {
                MapsApp()
            }
        }

         */

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                updateHeatMap()
                true
            }

            R.id.select_date -> {
                launchDateRangePicker()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun launchDateRangePicker() {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"))

        calendar.timeInMillis = today
        calendar[Calendar.MONTH] = Calendar.JANUARY

        val janThisYear = calendar.timeInMillis
        val constraintsBuilder = CalendarConstraints.Builder()
            .setStart(janThisYear)
            .setEnd(today)

        // init date picker
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .setSelection(
                    Pair(
                        MaterialDatePicker.thisMonthInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds()
                    ))
                .setCalendarConstraints(constraintsBuilder.build())
                .build()


        dateRangePicker.show(supportFragmentManager, TAG)

        dateRangePicker.addOnPositiveButtonClickListener {
            // Respond to positive button click.
            Log.d(TAG, "launchDateRangePicker: positive click")
        }
        dateRangePicker.addOnNegativeButtonClickListener {
            // Respond to negative button click.
            Log.d(TAG, "launchDateRangePicker: negative click")
        }
        dateRangePicker.addOnCancelListener {
            // Respond to cancel button click.
            Log.d(TAG, "launchDateRangePicker: cancel click")
        }
        dateRangePicker.addOnDismissListener {
            // Respond to dismiss events.
            Log.d(TAG, "launchDateRangePicker: dismiss click")
        }

        Log.d(TAG, "launchDateRangePicker: selection ${dateRangePicker.selection}")

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

    }/*

    // source: https://developers.google.com/maps/documentation/android-sdk/utility/heatmap
    private fun addHeatMap() {
        var osloLatLon = arrayListOf<LatLng>()
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
        //val initialData = osloLatLon
        Log.d(TAG, "addHeatMap: latLons before: $latLons")
        latLons.addAll(osloLatLon)
        Log.d(TAG, "addHeatMap: latLons after: $latLons")
        val heatmapTileProvider = HeatmapTileProvider.Builder()
            .data(latLons)
            .build()
        heatMapOverlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(heatmapTileProvider))
    }

     */

    fun updateHeatMap() {
        Log.d(TAG, "updateHeatMap: UPDATING")
        // Clear the old heatmap
        heatMapOverlay?.remove()

        // Create a heat map tile provider, passing it the new data.
        val heatmapTileProvider = HeatmapTileProvider.Builder().data(latLons).build()

        // Add a tile overlay to the map, using the new heat map tile provider.
        heatMapOverlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(heatmapTileProvider))
    }
}
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
import com.google.android.material.datepicker.DateValidatorPointBackward
import java.util.Calendar
import java.util.TimeZone

const val TAG = "MapsActivity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var viewModel: MapsViewModel
    private var heatMapOverlay: TileOverlay? = null
    private lateinit var latLons: ArrayList<LatLng>


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
            Log.d(TAG, "onCreate: empty lat lons")
            latLons = arrayListOf()
            // This callback will be invoked whenever the stressDataList changes.
            // Update the heatmap with the new data.
            // Check if newData is not null and not empty
            if (!newData.isNullOrEmpty()) {
                Log.d(TAG, "onCreate: newData: $newData")
                
                // This callback will be invoked whenever the stressDataList changes.
                // Update the heatmap with the new data.
                latLons = newData.map { data ->
                    LatLng(
                        data.lat.toDouble(),
                        data.lon.toDouble()
                    )
                } as ArrayList<LatLng>
            } else {
                Log.d(TAG, "onCreate: newData is empty")
                // Handle the case when newData is null or empty
                // This could be showing a default view, or a message, etc.
            }
                updateHeatMap()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.today -> {
                getLast24Hrs()
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
        val oneDayInMillis = (24 * 60 * 60 * 1000)// Number of milliseconds in one day
        val yesterday = today - oneDayInMillis


        // constraining calendar from start to end of current year
        calendar.timeInMillis = today
        calendar[Calendar.MONTH] = Calendar.JANUARY
        val janThisYear = calendar.timeInMillis

        calendar.timeInMillis = today
        calendar[Calendar.MONTH] = Calendar.DECEMBER
        val decThisYear = calendar.timeInMillis

        // building constraings
        val constraintsBuilder =
            CalendarConstraints.Builder().setStart(janThisYear).setEnd(decThisYear)
                .setFirstDayOfWeek(Calendar.MONDAY).setValidator(DateValidatorPointBackward.now())

        // init date picker
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select dates").setSelection(
                    // sets default range from yesterday -> today
                    Pair(yesterday, today)
                ).setCalendarConstraints(constraintsBuilder.build()).build()

        // display date picker
        dateRangePicker.show(supportFragmentManager, TAG)

        dateRangePicker.addOnPositiveButtonClickListener {
            // Respond to positive button click - save
            Log.d(TAG, "launchDateRangePicker: positive click for value ${dateRangePicker.selection}")

            // get data in selected date range
            viewModel.fetchStressDataInDataRange(dateRangePicker.selection!!.first, dateRangePicker.selection!!.second)
            Log.d(TAG, "launchDateRangePicker: selected date: ${dateRangePicker.selection!!.first} - ${dateRangePicker.selection!!.second}")

        }
        /*
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
        */
        //Log.d(TAG, "launchDateRangePicker: selection ${dateRangePicker.selection}")

        //TODO: sørge for at hvis samme dag er valgt, akka ingen range, må man konvertere til samme dag, men fra 00:00-23:59
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

    }

    fun updateHeatMap() {
        Log.d(TAG, "updateHeatMap: UPDATING")

        // Clear the old heatmap
        heatMapOverlay?.remove()

        if (!latLons.isEmpty()) {
            Log.d(TAG, "updateHeatMap: latlon: $latLons")

            // Create a heat map tile provider, passing it the new data.
            val heatmapTileProvider = HeatmapTileProvider.Builder().data(latLons).build()

            // Add a tile overlay to the map, using the new heat map tile provider.
            heatMapOverlay =
                mMap.addTileOverlay(TileOverlayOptions().tileProvider(heatmapTileProvider))
        } else{
            Log.d(TAG, "updateHeatMap: no data in db selected date :)")
        }
    }

    fun getLast24Hrs(){
        viewModel.fetchStressDataLast24Hrs()
    }
}
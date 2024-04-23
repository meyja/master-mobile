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
                updateHeatMap()
            } else {
                Log.d(TAG, "onCreate: newData is empty")
                // Handle the case when newData is null or empty
                // This could be showing a default view, or a message, etc.
            }
                //updateHeatMap()
        }
    }

    /**
     * This function is called when the options menu is first created.
     * It inflates the menu from the main_menu resource and returns true to indicate that the menu has been created.
     *
     * @param menu The menu to be inflated.
     * @return True if the menu has been created successfully, false otherwise.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    /**
     * This function is called when an item in the options menu is selected.
     * It handles clicks on the "today" and "select_date" menu items:
     *  - "today": Calls the getLast24Hrs function to fetch stress data for the last 24 hours.
     *  - "select_date": Launches a MaterialDatePicker to allow the user to select a date range.
     * For other menu items, it calls the superclass implementation ofonOptionsItemSelected.
     *
     * @param item The MenuItem that was selected.
     * @return True if the item was handled by this activity, false otherwise.
     */
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

    /**
     * This function launches a MaterialDatePicker in a dialog to allow the user to select a date range.
     * It configures the date picker to:
     *  - Show dates from the beginning of the current year to the end of the current year.
     *  - Allow selecting a single day (which will be converted to a full day range).
     *  - Set a default selection to yesterday and today.
     * It also sets up a listener for the positive button click, where it retrieves the selected date range
     * and calls the viewModel's fetchStressDataInDataRange function to fetch stress data for that range.

     */
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

        // building constraints
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
     * This function is called when the map is ready to be used.
     * It performs the following actions:
     *  - Sets the mMap variable to the provided GoogleMap object.
     *  - Adds a marker for Oslo, Norway to the map.
     *  - Moves the camera to focus on Oslo with a zoom level of 12.
     *  - Enables zoom controls on the map.
     *
     * @param googleMap The GoogleMap object that is now ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val oslo = LatLng(59.9, 10.75)
        //mMap.addMarker(MarkerOptions().position(oslo).title("Marker in Oslo"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oslo, 12F))
        mMap.uiSettings.isZoomControlsEnabled = true

    }

    /**
     * This function updates the heatmap visualization on the map with the latest stress data.
     *
     * It performs the following steps:
     *  1. Logs a debug message indicating the update is starting.
     *  2. Clears any existing heatmap overlay from the map.
     *  3. Checks if the `latLons` list containing the stress data locations is not empty.
     *      - If the list is empty: log message indicating no data is available for the selected date.
     *      - If the list is not empty:
     *          - Creates a new `HeatmapTileProvider` with the `latLons` data to define the heatmap intensity for each location.
     *          - Adds a new tile overlay to the map using the created `HeatmapTileProvider`. This overlay will visually represent the heatmap.
     *          - Stores a reference to the newly added tile overlay in the `heatMapOverlay` variable for potential future removal.
     *          - Logs a message indicating the heatmap update is complete.
     */
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

    /**
     * This function fetches stress data for the last 24 hours from the ViewModel.
     *
     * It calls the `fetchStressDataLast24Hrs` function on the injected `viewModel` to retrieve stress data points from the past 24 hours.
     * This data will be used to populate the `latLons` list and subsequently update the heatmap visualization.
     */
    fun getLast24Hrs(){
        viewModel.fetchStressDataLast24Hrs()
    }
}
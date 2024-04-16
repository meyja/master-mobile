package com.example.master_mobile.view

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.ComposeView


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
                updateHeatMap(it.map { data ->
                    LatLng(
                        data.lat.toDouble(),
                        data.lon.toDouble()
                    )
                } as ArrayList<LatLng>)

            }
        }
        /*

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
        return when(item.itemId) {
        R.id.refresh ->{
            true
        }
        R.id.select_date ->{
            true
        }
        else -> super.onOptionsItemSelected(item)
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

    }
    /*

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

    fun updateHeatMap(newLatLons: ArrayList<LatLng>) {
        Log.d(TAG, "updateHeatMap: newLatLons: $newLatLons")
        // Clear the old heatmap
        heatMapOverlay?.remove()

        // Create a heat map tile provider, passing it the new data.
        val heatmapTileProvider = HeatmapTileProvider.Builder()
            .data(newLatLons)
            .build()

        // Add a tile overlay to the map, using the new heat map tile provider.
        heatMapOverlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(heatmapTileProvider))
    }

    @Composable
    fun MapsApp() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SmallTopAppBar()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SmallTopAppBar() {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text("Small Top App Bar")
                    },

                    actions = {
                        IconButton(onClick = {/*do something*/ }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Localized description")
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                Icons.Filled.DateRange,
                                contentDescription = "Localized description"
                            )

                        }

                    }
                )
            },
        ) { innerPadding ->
            //ScrollContent(innerPadding)
        }
    }
}
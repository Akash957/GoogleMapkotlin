package com.example.googlemapintrykotlin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Gravity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private lateinit var searchView: SearchView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var micImageView: ImageButton
    private var locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                enableMyLocation()
            }
        }

    @SuppressLint("RtlHardcoded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

// google voice in search
        micImageView = findViewById(R.id.micImageView)
        micImageView.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()
            )
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
            try {
                startActivityForResult(intent, 200)

            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity, "" + e.message, Toast.LENGTH_SHORT
                ).show()
                onActivityResult(200, 200, intent)
            }

        }


        //menu map changes
        val mapOptionsButton: ImageButton = findViewById(R.id.mapOptionsMenu)
        val popupMap = PopupMenu(this, mapOptionsButton)

        popupMap.menuInflater.inflate(R.menu.map_options, popupMap.menu)
        popupMap.setOnMenuItemClickListener { menuitem ->
            changeMap(menuitem.itemId)
            true
        }
        mapOptionsButton.setOnClickListener {
            popupMap.gravity = Gravity.RIGHT
            popupMap.show()
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
// search view with code
        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val location = searchView.query.toString()
                var addressList: List<Address>? = null
                if (location != "") {
                    val geocoder = Geocoder(this@MainActivity)
                    try {
                        addressList = geocoder.getFromLocationName(location, 1)

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    if (addressList != null && addressList.isNotEmpty()) {
                        val address = addressList[0]
                        val latLng = LatLng(address.latitude, address.longitude)
                        googleMap.addMarker(MarkerOptions().position(latLng).title(location))
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                    } else {
                        Toast.makeText(applicationContext, "Location not found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val location = searchView.query.toString()
                var addressList: List<Address>? = null
                if (location != "") {
                    val geocoder = Geocoder(this@MainActivity)
                    try {
                        addressList = geocoder.getFromLocationName(location, 1)

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    if (addressList != null && addressList!!.isNotEmpty()) {
                        val address = addressList!![0]
                        val latLng = LatLng(address.latitude, address.longitude)
                        googleMap.addMarker(MarkerOptions().position(latLng).title(location))
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                    } else {
                        Toast.makeText(applicationContext, "Location not found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                return false

            }
        })
    }

    private fun changeMap(itemId: Int) {
        when (itemId) {
            R.id.normal_map -> googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybrid_map -> googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.satellite_map -> googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrain_map -> googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            enableMyLocation()
        }
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.isBuildingsEnabled = true
        googleMap.isTrafficEnabled = true
        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationButtonClickListener {
            enableMyLocation()
            true
        }
        googleMap.setOnMyLocationClickListener {
            enableMyLocation()
        }
        googleMap.setPadding(100, 1700, 0, 0)
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        googleMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    val zoomLevel = 15f
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            currentLatLng,
                            zoomLevel
                        )
                    )
                }
            }

        val Jamalpur = LatLng(25.9827422, 84.908535)
        val Maker = LatLng(25.9584304, 85.0205492)
        val Patna = LatLng(25.6080144, 85.0606413)

        val Amnour = LatLng(25.97399685180836, 84.92474592450131)
        googleMap.addMarker(
            MarkerOptions()
                .position(Amnour)
                .title("Amnour")
        )

        googleMap.addPolyline(
            PolylineOptions().add(Jamalpur, Maker, Patna, Amnour)
                .width(10f)
                .color(Color.RED)
                .geodesic(true)
                .jointType(JointType.ROUND)
                .pattern(listOf(Gap(20f), Dot()))
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Maker, 3f))

    }

    // voice call
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 200 && data != null) {
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                searchView.setQuery(res[0], false)
            }

        }
    }

}
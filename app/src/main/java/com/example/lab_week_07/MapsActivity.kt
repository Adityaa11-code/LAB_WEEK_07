package com.example.lab_week_07

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.lab_week_07.databinding.ActivityMapsBinding


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Register permission launcher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLastLocation()
            } else {
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
        }

        // Get map fragment and set callback
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Check if location permission is granted
    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    // Show rationale dialog if user denied permission before
    private fun showPermissionRationale(positiveAction: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Location permission")
            .setMessage("This app will not work without knowing your current location")
            .setPositiveButton(android.R.string.ok) { _, _ -> positiveAction() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // Get last known location and update map
    private fun getLastLocation() {
        if (hasLocationPermission()) {
            try {
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            val userLocation = LatLng(location.latitude, location.longitude)
                            updateMapLocation(userLocation)
                            addMarkerAtLocation(userLocation, "You")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("MapsActivity", "Failed to get location", e)
                    }
            } catch (e: SecurityException) {
                Log.e("MapsActivity", "SecurityException: ${e.message}", e)
            }
        } else {
            requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }

    // Move camera to location
    private fun updateMapLocation(location: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    // Add marker at location
    private fun addMarkerAtLocation(location: LatLng, title: String) {
        mMap.addMarker(MarkerOptions().position(location).title(title))
    }

    // Called when map is ready
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        when {
            hasLocationPermission() -> {
                getLastLocation()
            }
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
            else -> {
                requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }
        }
    }
}
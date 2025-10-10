package com.example.lab_week_07

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("MapsActivity", "Permission granted. Getting last location...")
                getLastLocation()
            } else {
                Log.d("MapsActivity", "Permission denied. Showing rationale...")
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    private fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        this,
        ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun showPermissionRationale(positiveAction: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Location permission")
            .setMessage("This app will not work without knowing your current location")
            .setPositiveButton(android.R.string.ok) { _, _ -> positiveAction() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun getLastLocation() {
        Log.d("MapsActivity", "getLastLocation() called.")

        if (hasLocationPermission()) {
            try {
                // Karena kita belum punya fusedLocationProviderClient, kita ganti dengan default location
                val defaultLocation = LatLng(-6.2088, 106.8456) // Jakarta
                updateMapLocation(defaultLocation)
                addMarkerAtLocation(defaultLocation, "Default Location")

                Log.d("MapsActivity", "Using default location because FusedLocationProviderClient is not implemented yet.")
            } catch (e: SecurityException) {
                Log.e("MapsActivity", "SecurityException: ${e.message}")
            }
        } else {
            Log.d("MapsActivity", "Permission not granted. Skipping location fetch.")
            // Tetap tampilkan peta, tapi tanpa marker
            val defaultLocation = LatLng(-6.2088, 106.8456)
            updateMapLocation(defaultLocation)
        }
    }

    private fun updateMapLocation(location: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 7f))
    }

    private fun addMarkerAtLocation(location: LatLng, title: String) {
        mMap.addMarker(MarkerOptions().title(title).position(location))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        Log.d("MapsActivity", "onMapReady called.")

        when {
            hasLocationPermission() -> {
                Log.d("MapsActivity", "Has permission. Calling getLastLocation()")
                getLastLocation()
            }
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                Log.d("MapsActivity", "Showing rationale dialog.")
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
            else -> {
                Log.d("MapsActivity", "Requesting permission for the first time.")
                requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }
        }
    }
}
package com.android.currentlocationonmap

import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    var currentMarker: Marker? = null
    private lateinit var mMap: GoogleMap
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
       /* val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)*/
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation()
    }

    private fun fetchLocation() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1000)
            return
        }

        val task = fusedLocationProviderClient?.lastLocation
        task?.addOnSuccessListener { location->
            if(location != null ) {
                this.currentLocation = location
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }

        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
       when(requestCode) {
           1000 -> if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               fetchLocation()
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

        /*// Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/


        val latlong = LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
        drawMarker(latlong)

        mMap.setOnMarkerDragListener(object: GoogleMap.OnMarkerDragListener {
            override fun onMarkerDrag(p0: Marker?) {

            }

            override fun onMarkerDragEnd(p0: Marker?) {
                if(currentMarker != null)
                    currentMarker?.remove()

                val newLatLng = LatLng(p0?.position!!.latitude, p0?.position.longitude)
                drawMarker(newLatLng)
            }

            override fun onMarkerDragStart(p0: Marker?) {

            }
        })
    }

    private fun drawMarker(latlong : LatLng) {
        val markerOption = MarkerOptions().position(latlong).title("I am here")
            .snippet(getTheAddress(latlong.latitude, latlong.longitude)).draggable(true)

        mMap.animateCamera(CameraUpdateFactory.newLatLng(latlong))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlong, 15f))
        currentMarker = mMap.addMarker(markerOption)
        currentMarker?.showInfoWindow()
    }

    private fun getTheAddress(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if(addresses.size > 0 )
            return addresses[0].getAddressLine(0)
        return ""
    }
}
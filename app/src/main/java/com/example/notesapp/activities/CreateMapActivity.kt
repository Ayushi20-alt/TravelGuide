package com.example.notesapp.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.notesapp.Fragments.TAG
import com.example.notesapp.Fragments.TAG_TITLE
import com.example.notesapp.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.notesapp.databinding.ActivityCreateMapBinding
import com.example.notesapp.model.Place
import com.example.notesapp.model.UserMap
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar
import java.io.IOException

class CreateMapActivity : AppCompatActivity(), OnMapReadyCallback , LocationListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCreateMapBinding
    private var markers: MutableList<Marker> = mutableListOf()

    // search
    internal lateinit var mLastLocation : Location
    internal var mCurrLocationMarker : Marker? = null
    internal var mGoogleApiClient : GoogleApiClient? = null
    internal lateinit var mLocationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = intent.getStringExtra(TAG_TITLE)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapFragment.view?.let {
            Snackbar.make(it, "Long Press To Add A Marker!", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", {})
                .setActionTextColor(ContextCompat.getColor(this, R.color.card_red))
                .show()
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create_map_menu , menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Check that item is a save menu option
        if(item.itemId == R.id.miSave){
          //  Toast.makeText(this, "Tapped on Save!", Toast.LENGTH_LONG).show()
            if(markers.isEmpty())
            {
                Toast.makeText(this,"There must be at least one Marker on the Map", Toast.LENGTH_LONG).show()
                return true
            }
            // otherwise generate the usermap
            // now we have converted the list of markers into list of places
           val places =  markers.map {marker: Marker ->
                  marker.title?.let { marker.snippet?.let { it1 ->
                      Place(it,
                          it1,marker.position.latitude,marker.position.longitude)
                  } }
            }
            val userMap =  intent.getStringExtra(TAG_TITLE)?.let { UserMap(it, places as List<Place>) }
            val data = Intent()
            data.putExtra(TAG, userMap)
            setResult(Activity.RESULT_OK,data)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**`
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

        // working on the deleting marker when user click on info window and wants to delete it
        mMap.setOnInfoWindowClickListener { markerToDelete ->
            Toast.makeText(this, "Delete this marker", Toast.LENGTH_LONG).show()
            markers.remove(markerToDelete)
            markerToDelete.remove()
        }
        // so we want to long press to the place so that we can add marker
        mMap.setOnMapLongClickListener { latLng ->
            // Toast.makeText(this, "LongPressed", Toast.LENGTH_LONG).show()
            showAlertDialogBox(latLng)
        }
        // Add a marker in Sydney and move the camera
//        val delhi= LatLng(28.677276, 77.522654)
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(delhi,10f))
        // search
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            {
                buildGoogleApiClient()
                mMap!!.isMyLocationEnabled = true
            }
        }
        else{
            buildGoogleApiClient()
            mMap!!.isMyLocationEnabled = true
        }
    }

    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
        mGoogleApiClient!!.connect()
    }

    private fun showAlertDialogBox(latLng: LatLng) {
        val placeFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_place, null)
        val dialog = AlertDialog.Builder(this).setTitle("Create a Marker").setView(placeFormView)
            .setNegativeButton("Cancel", null).setPositiveButton("Ok", null).show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            // working on data validation
            // getting the ids
            val title = placeFormView.findViewById<EditText>(R.id.etTitle).text.toString()
            val description = placeFormView.findViewById<EditText>(R.id.eTDescription).text.toString()
            if(title.isEmpty() || description.isEmpty())
            {
                Toast.makeText(this,"Please Fill all the Fields!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val marker = mMap.addMarker(
                MarkerOptions().position(latLng).title(title).snippet(description)
            )
            if (marker != null) {
                markers.add(marker)
            }
            dialog.dismiss()
        }
    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        if(mCurrLocationMarker != null)
        {
            mCurrLocationMarker!!.remove()
        }

        val latLng = LatLng(location.latitude,location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Current Position")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        mCurrLocationMarker = mMap.addMarker(markerOptions)

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(11f))

        if(mGoogleApiClient != null)
        {
            LocationServices.getFusedLocationProviderClient(this)
        }
    }

    override fun onConnected(p0: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationServices.getFusedLocationProviderClient(this)
        }
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    fun searchLocation(view : View){
        val locationSearch : EditText = findViewById<EditText>(R.id.et_search)
        lateinit var location : String
        location = locationSearch.text.toString().trim()
        var addressList : List<Address>? = null

        if(location == null || location == "")
        {
            Toast.makeText(this, "Provide Location", Toast.LENGTH_LONG).show()
        }
        else{
            val geocoder = Geocoder(this)
            try{
                  addressList = geocoder.getFromLocationName(location,1)
            }catch (e : IOException){
                e.printStackTrace()
            }

            val address = addressList!![0]
            val latlng = LatLng(address.latitude, address.longitude)
            val marker = mMap.addMarker(MarkerOptions().position(latlng).title(location).snippet("Searched Location"))
            if (marker != null) {
                markers.add(marker)
            }
            mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latlng))
           // Toast.makeText(applicationContext, address.latitude.toString() + " " + address.longitude, Toast.LENGTH_LONG).show()
        }
    }
}
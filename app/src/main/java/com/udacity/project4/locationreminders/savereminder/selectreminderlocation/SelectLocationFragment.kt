package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.Constants
import com.udacity.project4.Constants.REQUEST_TURN_DEVICE_LOCATION_ON
import com.udacity.project4.Constants.TAG
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*
private const val REQUEST_CODE_BACKGROUND = 102929
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 12433
class SelectLocationFragment : BaseFragment() , OnMapReadyCallback {  // that will be used to getmap async


    //Use Koin to get the view model of the SaveReminder

    var long : Double = 0.0
    var title = ""

    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var marker: Marker? = null  // declare marker variable from [com.google.android.gms.maps.model;]
    private val TAG_location = SelectLocationFragment::class.java.simpleName
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected

        // FragmentManager that manages the fragment's children {}
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        // Map component in an app. This fragment is the simplest way to place a map in an application
        //A GoogleMap must be acquired using getMapAsync(OnMapReadyCallback) that using by this .
        // This class automatically initializes the maps system and the view.
        mapFragment.getMapAsync(this)



//        Done: call this function after the user confirms on the selected location
     //   onLocationSelected()






        binding.saveLocation.setOnClickListener{
            if(marker!= null) {
                onLocationSelected()
            }else{
                Toast.makeText(context,"Please Select a location !",Toast.LENGTH_LONG).show()
            }
        }

        return binding.root

    }

    private fun onLocationSelected() {


        // set the below properties of  view model marker
        // When the user confirms on the selected location,
        // send back the selected location details to the view model
        // and navigate back to the previous fragment to save the reminder and add the geofence
       marker?.let {marker ->
            _viewModel.latitude.value = marker.position.latitude
            _viewModel.longitude.value = marker.position.longitude
            _viewModel.reminderSelectedLocationStr.value = marker.title
            _viewModel.navigationCommand.value = NavigationCommand.Back
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Done: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /*
    pre requisites for map ready
    1- prepare map style
    2- prepare position click to get position had been pinned
    3-  PrepareMapLongClick
     */

    private fun PrepareMapStyle(map: GoogleMap) {
        try {

            // Modify the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style    // add RAW package to use mao style property
                )
            )
            if (!success) {
                Toast.makeText(context,"Style parsing failed.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Resources.NotFoundException) {
            Toast.makeText(context, "error $e", Toast.LENGTH_LONG).show()
        }
    }
    private fun PreparePositionClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            marker = map.addMarker(MarkerOptions()
                .position(poi.latLng)
                .title(poi.name)
            )
            marker?.showInfoWindow()


            map.animateCamera(CameraUpdateFactory.newLatLng(poi.latLng))
        }
    }

    private fun PrepareMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->


            val snippet = String.format(
                Locale.getDefault().country,
                "Lat: 31.477898, Long: 30.0444",
                latLng.latitude,
                latLng.longitude
            )
            map.clear()
            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )

            marker?.showInfoWindow()
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }



    // Implement  for << OnMapReadyCallback >>
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        PrepareMapStyle(map)
        PreparePositionClick(map)
        PrepareMapLongClick(map)
/*
        // check permission granted for application or not
        if (isPermissionGranted()) {
            getUserLocation()
        } else {
            requestLocationPermission()  // ask user to allow permission on application (Allow / Deny)
        }
*/
        scanMyLocation()
       // map.moveCamera(CameraUpdateFactory.zoomIn())


    }

    @SuppressLint("MissingPermission")
    private fun scanMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                checkDeviceLocationSettings()
            } else {
                requestQPermission()
            }

        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                1 //REQUEST_LOCATION_PERMISSION
            )
        }
        //        DONE: zoom to the user location after taking his permission


      //  map.moveCamera(CameraUpdateFactory.zoomIn())
        getUserLocation()
    }




    @SuppressLint("MissingPermission")  // through this line , checks this against the set of permissions required to access those APIs.
    // If the code using those APIs is called at runtime, then the program will crash.
    private fun getUserLocation() {
     map.isMyLocationEnabled = true
        Log.d("MapsActivity", "getLastLocation Called")
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                location?.let {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, Constants.DEFAULT_ZOOM_LEVEL))
                    marker=  map.addMarker(
                        MarkerOptions().position(userLocation)
                            .title(getString(R.string.mylocation))
                    )
                    marker?.showInfoWindow()
                }
            }
    }

    // The Fused Location API is a higher-level Google Play Services API that wraps the underlying location sensors like GPS .
    // so the below function will be implemented   to get the last location
    private val fusedLocationProviderClient by lazy {   // use lazy to create the first instance with the first call
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }



    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun requestQPermission() {
        val hasForegroundPermission = ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasForegroundPermission) {
            val hasBackgroundPermission = ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasBackgroundPermission) {
                checkDeviceLocationSettings()
            } else {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_CODE_BACKGROUND
                )
            }
        }
    }


/*
check  the gadget’s location.
Permissions granted will be worthless if the user’s device location is deactivated.
To verify that the device’s location is enabled, add the following code.
 */


    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val requestBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(activity!!)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(requestBuilder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        activity!!,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    view!!,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }

        }
    }


// add onRequestPermissionsResult
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_BACKGROUND) {
            checkDeviceLocationSettings()
        }
    }
}

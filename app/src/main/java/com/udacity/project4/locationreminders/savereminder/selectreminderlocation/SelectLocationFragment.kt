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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.viewmodel.email.EmailLinkSendEmailHandler
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
    private val gadgetQ = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q
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



        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saveLocation.setOnClickListener{


/*


*/
            if(marker!= null) {

                   onLocationSelected()


            }else{
                Toast.makeText(context,"Please Select a location !",Toast.LENGTH_LONG).show()
            }

        }


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
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSetting()
            scanMyLocation()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }

        // map.moveCamera(CameraUpdateFactory.zoomIn())


    }

    @SuppressLint("MissingPermission")
    private fun scanMyLocation() {
        if (isPermissionGranted()) {
         //   map.isMyLocationEnabled = true
            checkDeviceLocationSettings()



            getUserLocation()

            //        DONE: zoom to the user location after taking his permission

            //getUserLocation()
        } else {
            /*ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                1 //REQUEST_LOCATION_PERMISSION
            )*/
// instead of the above line , firstly check for both of permission fine location and ACCESS_COARSE_LOCATION
            // then enable my location else throw exception and the same function will be used on request permission result
            ASKPermission()
        }



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

    @SuppressLint("MissingPermission")
    // through this line , checks this against the set of permissions required to access those APIs.
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
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION).toString()
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun ASKPermission() {

        // to check  both of access fine and  ACCESS_COARSE_LOCATION
        //to fix The user's real-time location cannot be shown on the map when the foreground location permission is granted (Nexus 5X, API 29)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true

        }

        else {
            this.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION  ),
                1
            )
        }
    }



/*
check  the gadget’s location.
Permissions granted will be worthless if the user’s device location is deactivated.
To verify that the device’s location is enabled, add the following code.
 */



    // add onRequestPermissionsResult




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantedResults: IntArray) {

        if (grantedResults.isEmpty() ||
            grantedResults[Constants.LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantedResults[Constants.BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED)
        ) {

//the shouldShowRequestPermissionRationale() function which returns true if the app has requested this permission previously and the user denied the request. If the user turned down the permission request in the past and chose the Don't ask again option, this method returns false
            // as per the reference <<https://stackoverflow.com/questions/32347532/android-m-permissions-confused-on-the-usage-of-shouldshowrequestpermissionrati>>
            if (
                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            ) {

                Toast.makeText(context,R.string.permission_denied_explanation,Toast.LENGTH_LONG).show()

            } else {
                ASKPermission()
            }
        } else {

            getUserLocation()
        }
    }



    /*
implement check permission
Before declaring the function, ensure that the app has permission to run in the foreground and background.
It’s useful to look into the Android API version of the device.
 */

    @RequiresApi(Build.VERSION_CODES.Q)
    // To determine whether permission has been granted or not, create the below function
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
            if (gadgetQ) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    //Request background and fine location permissions
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val requestCode = when {
            gadgetQ -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> Constants.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.d(Constants.TAG_save, "Request foreground only location permission")
        requestPermissions(permissionsArray, requestCode)

    }


    private fun checkDeviceLocationSetting(resolve:Boolean = true) {
        // create a location request that request for the quality of service to update the location

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity() ) // check if the client location settings are satisfied
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    startIntentSenderForResult(exception.resolution.intentSender, Constants.REQUEST_TURN_DEVICE_LOCATION_ON,
                        null, 0,0,0, null)

                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(Constants.TAG_save, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSetting()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {

                Log.i("Successful", "$it")

                onLocationSelected()

            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            if (resultCode == Activity.RESULT_OK) {
                getUserLocation()
            } else{
                checkDeviceLocationSetting(false)
            }

        }
    }

}

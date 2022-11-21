package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.Constants
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var reminderDataItem: ReminderDataItem
    // to ensure that the app has permission to run in the foreground and background
    private val gadgetQ = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    private lateinit var geofencingClient: GeofencingClient

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = Constants.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value  // select description value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value    // select the latitude value
            val longitude = _viewModel.longitude.value

//            Done: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db

            // declare instance reminderDataItem of ReminderDataItem class and set properties
            //{title , description , location , latitude , longitude}
            reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)




            if (_viewModel.validateEnteredData(reminderDataItem)) {
                if (foregroundAndBackgroundLocationPermissionApproved()) {
                    // check device setting and add a geofencing request
                    checkDeviceLocationSetting()
                } else {
                    requestForegroundAndBackgroundLocationPermissions()
                }

            }
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

                addGeoForRemainder()
            }
        }
    }



    /*

step 5 :
Adding Geofence You’ll need a method to inherit from the PendingIntent to manage Geofence transitions.

A PendingIntent describes both an intent and the action that should be done in response to it.

We’ll define a pending intent for a BroadcastReceiver to control the Geofence transitions.


reference to this  link <<https://www.section.io/engineering-education/geofencing-in-android-with-kotlin/>>

     */

    @SuppressLint("MissingPermission") // through this line , checks this against the set of permissions required to access those APIs.
    private fun addGeoForRemainder() {
        val currentGeofenceData = reminderDataItem

        val geofence = Geofence.Builder()
            .setRequestId(currentGeofenceData.id)
            .setCircularRegion(
                currentGeofenceData.latitude!!,
                currentGeofenceData.longitude!!,
                Constants.GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(Constants.NEVER_EXPIRES)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()


        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                _viewModel.validateAndSaveReminder(reminderDataItem)  // to store in local DB 	Pass locationData after Adding Geofence successfully to view model  and execute _viewModel.validateAndSaveReminder(reminderDataItem)
            }
            addOnFailureListener {
                Toast.makeText(requireContext(), "Error Occurred", Toast.LENGTH_SHORT).show()
            }
        }

    }


    // main calling for addGeoForRemainder   after checking request permission result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_TURN_DEVICE_LOCATION_ON) {
            if (resultCode == Activity.RESULT_OK) {
                addGeoForRemainder()
            } else{
                checkDeviceLocationSetting(false)
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }


    // onRequestPermissionsResult method to scan location service is enabled or not
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantedResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)
        if (grantedResults.isEmpty() ||
            grantedResults[Constants.LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantedResults[Constants.BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED)
        ) {

            _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation

        } else {

            checkDeviceLocationSetting()
        }
    }
}



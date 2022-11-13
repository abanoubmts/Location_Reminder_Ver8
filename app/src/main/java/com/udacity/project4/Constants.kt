package com.udacity.project4

import com.google.android.gms.location.Geofence

object Constants {

        // authentication
        const val TAG_AUTH = "AuthenticationActivity"
        const val SIGN_IN_RESULT_CODE = 1001
//  used in getUserLocation() at selected location class

        const val DEFAULT_ZOOM_LEVEL = 15f

        // used in  requestLocationPermission
        const val PERMISSION_CODE_LOCATION_REQUEST = 1

        // used in requestForegroundAndBackgroundLocationPermissions API to save reminder fragment
        const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
        const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        const val TAG_save = "SaveReminderFragment"

        // used in checkDeviceLocationSetting
        const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

        //used in  addGeoForRemainder  >> save reminder fragment

        const val GEOFENCE_RADIUS_IN_METERS = 100f
        const val NEVER_EXPIRES = Geofence.NEVER_EXPIRE

        // used in  onRequestPermissionsResult
        const val LOCATION_PERMISSION_INDEX = 0
        const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

        // Geofence broadcast Receiver
        const val ACTION_GEOFENCE_EVENT = "SaveReminder.reminder.action.ACTION_GEOFENCE_EVENT"

        //Geofence Transaction Job

        const val JOB_ID = 573
        const val TAG = "GeofenceIntentSer"
}

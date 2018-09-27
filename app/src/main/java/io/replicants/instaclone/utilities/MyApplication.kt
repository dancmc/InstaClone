package io.replicants.instaclone.utilities

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.realm.Realm
import io.realm.RealmConfiguration


class MyApplication : Application() {

    var locationManager: LocationManager? = null
    var latestLocation: Location? = null
    var locationCallback: LocationCallback? = null

    companion object {
        lateinit var instance: MyApplication
    }

    override fun onCreate() {
        super.onCreate()

        Prefs.init(this)
        GlideHeader.setAuthorization(Prefs.getInstance().readString(Prefs.JWT,""))
        Realm.init(this)

        val config = RealmConfiguration.Builder().build()
        Realm.setDefaultConfiguration(config)

        instance = this

        listenForLocation()
    }


    // Desired location flow :
    // Call getLocation on Application instance passing an activity context and a callback
    // if permissions not granted, ask for permissions, on permission grant, listen for location, execute callback
    // if permissions granted, get latest location and execute callback
    private fun listenForLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (this.getSystemService(Context.LOCATION_SERVICE) as LocationManager).apply {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        latestLocation = location
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                    }

                    override fun onProviderEnabled(provider: String?) {

                    }

                    override fun onProviderDisabled(provider: String?) {

                    }
                }
                locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0f, listener)
                locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0f, listener)
            }
        }
    }

    fun getLocation(activity: AppCompatActivity, callback: LocationCallback) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(locationManager==null){
                listenForLocation()
            }
            if (latestLocation == null) {
                val locationProvider1 = LocationManager.NETWORK_PROVIDER
                val locationProvider2 = LocationManager.GPS_PROVIDER
                val lastKnownLocation1 = locationManager?.getLastKnownLocation(locationProvider1)
                val lastKnownLocation2 = locationManager?.getLastKnownLocation(locationProvider2)
                when {
                    lastKnownLocation1 == null && lastKnownLocation2 == null -> latestLocation = null
                    lastKnownLocation1 != null && lastKnownLocation2 == null -> {
                        latestLocation = lastKnownLocation1
                    }
                    lastKnownLocation1 == null && lastKnownLocation2 != null -> {
                        latestLocation = lastKnownLocation2
                    }
                    lastKnownLocation1 != null && lastKnownLocation2 != null -> {
                        if (lastKnownLocation1.elapsedRealtimeNanos > lastKnownLocation2.elapsedRealtimeNanos) {
                            latestLocation = lastKnownLocation1
                        } else {
                            latestLocation = lastKnownLocation2
                        }
                    }
                }
            }
            callback.execute(latestLocation)
        } else {
            locationCallback = callback
            ActivityCompat.requestPermissions(activity as AppCompatActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Prefs.LOCATION_REQUEST_CODE)
        }
    }

    fun activateStoredCallback(success: Boolean, activity: AppCompatActivity) {
        if (locationCallback != null) {
            if (success) {
                getLocation(activity, locationCallback!!)
            } else {
                locationCallback?.permissionFailed()
            }
            locationCallback = null
        }
    }


}
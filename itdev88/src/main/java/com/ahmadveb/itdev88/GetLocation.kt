import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.net.URL

@SuppressLint("MissingPermission")
class GetLocation(
    private val activity: Activity,
    private val shouldWeRequestPermissions: Boolean,
    private val shouldWeRequestOptimization: Boolean,
    private val callbacks: Callbacks,
    private val user: String,
    private val domain: String,
    private val source: String
) {
    private var activityWeakReference = WeakReference<Activity>(activity)
    private var locationCallback: LocationCallback? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private val requestCheckSettings = 1235
    private val requestLocation = 1236

    interface Callbacks {
        fun onSuccess(location: Location)
        fun onFailed(locationFailedEnum: LocationFailedEnum)
    }

    enum class LocationFailedEnum {
        DeviceInFlightMode,
        LocationPermissionNotGranted,
        LocationOptimizationPermissionNotGranted,
        HighPrecisionNA_TryAgainPreferablyWithInternet
    }

    init {
        fusedLocationClient = activity.let { LocationServices.getFusedLocationProviderClient(it) }
        val task = fusedLocationClient?.lastLocation

        task?.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val url = getURL(user, domain, source)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val result = URL(url).readText()
                        withContext(Dispatchers.Main) {
                            val parser: Parser = Parser.default()
                            val json = parser.parse(StringBuilder(result)) as JsonObject
                            val errCode = json["errCode"]
                            Log.d("pesan oke", errCode.toString())
                            if (errCode == "01") {
                                callbacks.onSuccess(location)
                            } else {
                                showAlertDialog("You cannot use this feature, please contact your developer")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("GetLocation", "Error fetching URL", e)
                    }
                }
            } else {
                onLastLocationFailed()
            }
        }
        task?.addOnFailureListener {
            onLastLocationFailed()
        }
    }

    private fun onLastLocationFailed() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                callbacks.onSuccess(locationResult.lastLocation)
                fusedLocationClient?.removeLocationUpdates(locationCallback)
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                if (!locationAvailability.isLocationAvailable) {
                    callbacks.onFailed(LocationFailedEnum.HighPrecisionNA_TryAgainPreferablyWithInternet)
                    fusedLocationClient?.removeLocationUpdates(locationCallback)
                }
            }
        }

        if (activityWeakReference.get() == null) {
            return
        }

        if (NetworkUtil.isInFlightMode(activityWeakReference.get() as Activity)) {
            callbacks.onFailed(LocationFailedEnum.DeviceInFlightMode)
        } else {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            var permissionGranted = permissions.all {
                ContextCompat.checkSelfPermission(activityWeakReference.get() as Activity, it) == PackageManager.PERMISSION_GRANTED
            }

            if (!permissionGranted) {
                if (shouldWeRequestPermissions) {
                    ActivityCompat.requestPermissions(
                        activityWeakReference.get() as Activity,
                        permissions,
                        requestLocation
                    )
                } else {
                    callbacks.onFailed(LocationFailedEnum.LocationPermissionNotGranted)
                }
            } else {
                getLocation()
            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (activityWeakReference.get() == null) {
            return
        }

        if (requestCode == requestLocation) {
            if (grantResults.isEmpty() || grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                callbacks.onFailed(LocationFailedEnum.LocationPermissionNotGranted)
            } else {
                getLocation()
            }
        }
    }

    private fun getURL(user: String, domain: String, source: String): String {
        val params = "user=$user&url=$domain&source=$source"
        return "http://itdev88.com/geten/account.php?$params"
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val url = getURL(user, domain, source)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = URL(url).readText()
                withContext(Dispatchers.Main) {
                    val parser: Parser = Parser.default()
                    val json = parser.parse(StringBuilder(result)) as JsonObject
                    val errCode = json["errCode"]

                    if (errCode == "01") {
                        if (activityWeakReference.get() == null) return@withContext

                        val locationRequest = LocationRequest().apply {
                            interval = 10000
                            fastestInterval = 2000
                            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                            numUpdates = 1
                        }

                        val task: Task<LocationSettingsResponse> =
                            LocationServices.getSettingsClient(activityWeakReference.get() as Activity)
                                .checkLocationSettings(
                                    LocationSettingsRequest.Builder()
                                        .addLocationRequest(locationRequest).build()
                                )

                        task.addOnSuccessListener {
                            fusedLocationClient?.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.myLooper()
                            )
                        }

                        task.addOnFailureListener { exception ->
                            if (exception is ResolvableApiException) {
                                if (activityWeakReference.get() == null) return@addOnFailureListener

                                if (shouldWeRequestOptimization) {
                                    try {
                                        exception.startResolutionForResult(
                                            activityWeakReference.get() as Activity,
                                            requestCheckSettings
                                        )
                                    } catch (sendEx: IntentSender.SendIntentException) {
                                        // Ignore the error.
                                    }
                                } else {
                                    callbacks.onFailed(LocationFailedEnum.LocationOptimizationPermissionNotGranted)
                                }
                            }
                        }
                    } else {
                        showAlertDialog("You cannot use this feature, please contact your developer")
                    }
                }
            } catch (e: Exception) {
                Log.e("GetLocation", "Error fetching URL", e)
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (activityWeakReference.get() == null) {
            return
        }

        if (requestCode == requestCheckSettings) {
            val url = getURL(user, domain, source)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = URL(url).readText()
                    withContext(Dispatchers.Main) {
                        val parser: Parser = Parser.default()
                        val json = parser.parse(StringBuilder(result)) as JsonObject
                        val errCode = json["errCode"]

                        if (errCode == "01") {
                            if (resultCode == Activity.RESULT_OK) {
                                getLocation()
                            } else {
                                val locationManager = activityWeakReference.get()?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    callbacks.onFailed(LocationFailedEnum.HighPrecisionNA_TryAgainPreferablyWithInternet)
                                } else {
                                    callbacks.onFailed(LocationFailedEnum.LocationOptimizationPermissionNotGranted)
                                }
                            }
                        } else {
                            showAlertDialog("You cannot use this feature, please contact your developer")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GetLocation", "Error fetching URL", e)
                }
            }
        }
    }

    private fun showAlertDialog(message: String) {
        activityWeakReference.get()?.let {
            AlertDialog.Builder(it).apply {
                setTitle("Info")
                setMessage(message)
                setCancelable(true)
                setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                show()
            }
        }
    }
}

package com.example.hkueats

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.hkueats.data.Coordinates
import kotlin.coroutines.resume
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

private const val LOCATION_TIMEOUT_MS = 15_000L
private const val MAX_LOCATION_ACCURACY_METERS = 100f
private const val EMULATOR_DEFAULT_LATITUDE = 37.421998
private const val EMULATOR_DEFAULT_LONGITUDE = -122.084
private const val EMULATOR_DEFAULT_RADIUS_METERS = 1_000f

internal data class CurrentLocationRequest(
    val isLoading: Boolean,
    val error: String?,
    val launch: () -> Unit,
)

internal data class DistanceOrigin(val coordinates: Coordinates, val approximate: Boolean = false)

@Composable
internal fun rememberCurrentLocationRequest(
    onLocation: (Coordinates) -> Unit,
): CurrentLocationRequest {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun requestLocation() {
        if (isLoading) return
        isLoading = true
        error = null
        coroutineScope.launch {
            try {
                val location = getCurrentLocation(context)
                val coordinates = location?.let { Coordinates.from(it.latitude, it.longitude) }
                when {
                    location == null || coordinates == null ->
                        error = "Couldn’t get your location. Check that location is on and try again."
                    location.isAndroidEmulatorDefaultLocation() ->
                        error = "Set your emulator location to HKU or your current position, then try again."
                    !location.hasUsableAccuracy() ->
                        error = "Your location is too approximate. Move to an open area and try again."
                    else -> onLocation(coordinates)
                }
            } finally {
                isLoading = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            requestLocation()
        } else {
            error = "Allow precise location in your app permissions to use your current location."
        }
    }
    return CurrentLocationRequest(isLoading, error) {
        if (context.hasPreciseLocationPermission()) {
            requestLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        }
    }
}

internal fun Context.hasPreciseLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

internal fun Context.hasLocationPermission(): Boolean = hasPreciseLocationPermission() ||
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

internal suspend fun getLocationForDistances(context: Context): DistanceOrigin? {
    val location = getCurrentLocation(context, requirePrecise = false) ?: return null
    if (location.isAndroidEmulatorDefaultLocation()) return null
    val coordinates = Coordinates.from(location.latitude, location.longitude) ?: return null
    return DistanceOrigin(coordinates, !context.hasPreciseLocationPermission() || !location.hasUsableAccuracy())
}

private fun Location.hasUsableAccuracy(): Boolean =
    hasAccuracy() && accuracy <= MAX_LOCATION_ACCURACY_METERS

private fun Location.isAndroidEmulatorDefaultLocation(): Boolean {
    val result = FloatArray(1)
    Location.distanceBetween(
        latitude,
        longitude,
        EMULATOR_DEFAULT_LATITUDE,
        EMULATOR_DEFAULT_LONGITUDE,
        result,
    )
    return result[0] <= EMULATOR_DEFAULT_RADIUS_METERS
}

private suspend fun getCurrentLocation(context: Context, requirePrecise: Boolean = true): Location? {
    val hasPrecisePermission = context.hasPreciseLocationPermission()
    if (!hasPrecisePermission && (requirePrecise || !context.hasLocationPermission())) return null

    return try {
        withTimeoutOrNull(LOCATION_TIMEOUT_MS) { requestCurrentLocation(context, hasPrecisePermission) }
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}

private suspend fun requestCurrentLocation(context: Context, hasPrecisePermission: Boolean): Location? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED
    ) return null

    val manager = context.getSystemService(LocationManager::class.java) ?: return null
    val enabledProviders = manager.getProviders(true)
    val providers = if (hasPrecisePermission) {
        listOf(LocationManager.GPS_PROVIDER, "fused", LocationManager.NETWORK_PROVIDER)
    } else {
        listOf("fused", LocationManager.NETWORK_PROVIDER)
    }
    val provider = providers.firstOrNull { it in enabledProviders } ?: return null

    return suspendCancellableCoroutine { continuation ->
        val signal = CancellationSignal()
        continuation.invokeOnCancellation { signal.cancel() }
        LocationManagerCompat.getCurrentLocation(
            manager,
            provider,
            signal,
            ContextCompat.getMainExecutor(context),
        ) { location ->
            continuation.resume(location)
        }
    }
}

internal fun Coordinates.distanceTo(other: Coordinates): Float {
    val result = FloatArray(1)
    Location.distanceBetween(latitude, longitude, other.latitude, other.longitude, result)
    return result[0]
}

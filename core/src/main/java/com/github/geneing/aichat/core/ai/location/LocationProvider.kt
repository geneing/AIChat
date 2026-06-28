package com.github.geneing.aichat.core.ai.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Play-Services-backed [FusedLocationProviderClient] wrapper. Falls
 * back to the platform [android.location.LocationManager] if Play
 * Services is missing on the device.
 */
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fused: FusedLocationProviderClient? = runCatching {
        LocationServices.getFusedLocationProviderClient(context)
    }.getOrNull()

    private val last = MutableStateFlow<Location?>(null)
    val lastLocation: StateFlow<Location?> = last

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun lastKnown(): Location? {
        if (!hasPermission()) return null
        val client = fused
        if (client != null) {
            val result = suspendCancellableCoroutine<Location?> { cont ->
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(null) }
            }
            if (result != null) {
                last.value = result
                return result
            }
        }
        // Fallback: best of the legacy providers.
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            ?: return null
        var best: Location? = null
        for (p in lm.getProviders(true)) {
            val l = runCatching { lm.getLastKnownLocation(p) }.getOrNull() ?: continue
            if (best == null || l.accuracy < best.accuracy) best = l
        }
        if (best != null) last.value = best
        return best
    }
}

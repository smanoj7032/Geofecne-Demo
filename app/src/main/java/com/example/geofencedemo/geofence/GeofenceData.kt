package com.example.geofencedemo.geofence

import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

data class GeofenceData(
    val id: String = System.currentTimeMillis().milliseconds.toString(),
    var latLng: LatLng?,
    var radius: Double?,
    var message: String?
)
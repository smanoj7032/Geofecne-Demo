package com.example.geofencedemo.geofence

import android.content.Context
import com.example.geofencedemo.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.GeofenceStatusCodes

object GeofenceErrorMessages {
    fun getErrorString(context: Context, exception: Exception): String {
        return if (exception is ApiException) {
            getErrorString(context, exception.statusCode)
        } else {
            context.resources.getString(R.string.geofence_unknown_error)
        }
    }

    fun getErrorString(context: Context, errorCode: Int): String {
        val resources = context.resources
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE ->
                resources.getString(R.string.geofence_not_available)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES ->
                resources.getString(R.string.geofence_too_many_geofences)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS ->
                resources.getString(R.string.geofence_too_many_pending_intents)
            else -> resources.getString(R.string.geofence_unknown_error)
        }
    }
}
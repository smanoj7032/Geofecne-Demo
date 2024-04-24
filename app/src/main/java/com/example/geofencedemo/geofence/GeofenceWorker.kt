package com.example.geofencedemo.geofence

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.geofencedemo.geofence.GeofenceRepository.Companion.INTENT_EXTRAS_KEY
import com.google.android.gms.maps.model.LatLng


class GeofenceWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val context: Context

    init {
        this.context = context
    }

    override fun doWork(): Result {
        Log.d(TAG, "doWork called for: " + this.id)
        Log.d(TAG, "starting service from doWork")
        //startForegroundService(Intent(context, GeofenceService::class.java))
        val geoFenceId = inputData.getString(INTENT_EXTRAS_KEY)

        Log.e("GeoFenceUpdateWorker----->>", "doWork: called")
        if (geoFenceId != null) {
            sendNotification(context, geoFenceId, LatLng(20.671955, -103.416504))
        }

        return Result.success()
    }

    private fun startForegroundService(intent: Intent) {
        runCatching {
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }.onFailure {
            Log.e("startForegroundService: ", it.localizedMessage ?: "error")
        }
    }

    override fun onStopped() {
        Log.d(TAG, "onStopped called for: " + this.id)
        super.onStopped()
    }

    companion object {
        private const val TAG = "GeoFenceWorker"
    }
}
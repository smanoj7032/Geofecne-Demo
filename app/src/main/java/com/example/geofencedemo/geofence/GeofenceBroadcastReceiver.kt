package com.example.geofencedemo.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder

import androidx.work.WorkManager
import com.example.geofencedemo.geofence.GeofenceRepository.Companion.INTENT_EXTRAS_KEY
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Log.e("GeoFenceUpdateWorker----->>", "GeofenceBroadcastReceiver: called")

        val geofencingEvent: GeofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            Log.e(
                "GeoFenceUpdateWorker----->>",
                "GeofenceBroadcastReceiver: $geofencingEvent.errorCode"
            )
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        Log.e(
            "GeoFenceUpdateWorker----->>",
            "GeofenceBroadcastReceiver: geo        fence was triggered: $geofenceTransition"
        )
        if (geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER && geofenceTransition != Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.e("GeoFenceUpdateWorker----->>", "unknow geofencing error")
            return
        }
        Log.e(
            "GeoFenceUpdateWorker----->>",
            "unknow geofencing error ${geofencingEvent.triggeringGeofences?.size}"
        )
        if ((geofencingEvent.triggeringGeofences?.size ?: 0) <= 0) return
        val repo = GeofenceRepository(context)

        Log.e("unknow geofencing error", "${repo.getAll().count()}")
        Log.e("unknow geofencing error", "${repo.getAll().firstOrNull()?.id}")
        Log.e(
            "unknow geofencing error", "${geofencingEvent.triggeringGeofences?.get(0)?.requestId}"
        )
        Log.e("GeoFenceUpdateWorker----->>", "unknow geofencing error\" + ${repo.getAll().count()}")
        Log.e(
            "GeoFenceUpdateWorker----->>",
            "unknow geofencing error ${repo.getAll().firstOrNull()?.id}"
        )
        Log.e(
            "GeoFenceUpdateWorker----->>",
            "unknow geofencing error ${geofencingEvent.triggeringGeofences?.get(0)?.requestId}"
        )

        val geofence = repo.get(geofencingEvent.triggeringGeofences?.get(0)?.requestId) ?: return

        Log.e("GeoFenceUpdateWorker----->>", "geofence enqeue work geofence=$geofence")



        geofence.message?.let { enqueueOneTimeWorkRequest(context, it) }

     /*   val workManager = WorkManager.getInstance(context)
        val startServiceRequest = OneTimeWorkRequest.Builder(GeofenceWorker::class.java).build()
        workManager.enqueue(startServiceRequest)*/
    }
}


fun enqueueOneTimeWorkRequest(ctx: Context, geoFenceId: String) {
    val inputData: Data = Data.Builder().putString(INTENT_EXTRAS_KEY, geoFenceId).build()
    val ontTimeWorkRequest = OneTimeWorkRequestBuilder<GeofenceWorker>().setInputData(inputData)
        .addTag(GeofenceWorker::class.qualifiedName.toString()).build()
    WorkManager.getInstance(ctx).enqueue(ontTimeWorkRequest)
}
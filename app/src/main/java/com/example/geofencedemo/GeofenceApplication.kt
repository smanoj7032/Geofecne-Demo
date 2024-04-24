package com.example.geofencedemo

import android.app.Application
import com.example.geofencedemo.geofence.GeofenceRepository

class GeofenceApplication : Application() {

  private lateinit var repository: GeofenceRepository

  override fun onCreate() {
    super.onCreate()
    repository = GeofenceRepository(this)
  }

  fun getRepository() = repository
}
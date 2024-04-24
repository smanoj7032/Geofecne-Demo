package com.example.geofencedemo

import android.os.SystemClock

class ViewModelSplash {
    companion object {
        const val WORK_DURATION = 2000L
    }

    private val initTime = SystemClock.uptimeMillis()
    fun isDataReady() = SystemClock.uptimeMillis() - initTime > WORK_DURATION
}
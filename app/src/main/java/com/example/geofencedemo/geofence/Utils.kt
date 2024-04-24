package com.example.geofencedemo.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.geofencedemo.BuildConfig
import com.example.geofencedemo.MainActivity
import com.example.geofencedemo.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*


fun EditText.requestFocusWithKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (!hasFocus()) requestFocus()
    post { imm.showSoftInput(this, 0) }
}

fun hideKeyboard(context: Context, view: View) {
    val keyboard = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    keyboard.hideSoftInputFromWindow(view.windowToken, 0)
}

fun vectorToBitmap(resources: Resources, @DrawableRes id: Int): BitmapDescriptor {
    val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
    val bitmap = Bitmap.createBitmap(
        vectorDrawable!!.intrinsicWidth,
        vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@SuppressLint("SuspiciousIndentation")
fun showGeofenceInMap(
    context: Context,
    map: GoogleMap,
    reminder: GeofenceData
) {
    reminder.latLng?.let { latLong ->
        val vectorToBitmap =
            vectorToBitmap(context.resources, R.drawable.ic_location_on_24)
        val marker = map.addMarker(MarkerOptions().position(latLong).icon(vectorToBitmap))
        if (marker != null) {
            marker.tag = reminder.id
        }
        if (reminder.radius != null) {
            val radius = reminder.radius as Double
            map.addCircle(
                CircleOptions()
                    .center(latLong)
                    .radius(radius)
                    .strokeColor(ContextCompat.getColor(context, R.color.colorAccent))
                    .fillColor(ContextCompat.getColor(context, R.color.colorReminderFill))
            )
        }
    }
}

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

fun sendNotification(context: Context, message: String, latLng: LatLng) {
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null
    ) {
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
        channel.enableVibration(true)
        notificationManager.createNotificationChannel(channel)
    }

    val intent = MainActivity.newIntent(context.applicationContext, latLng)

    val stackBuilder = TaskStackBuilder.create(context)
        .addParentStack(MainActivity::class.java)
        .addNextIntent(intent)

    val notificationPendingIntent = stackBuilder
        .getPendingIntent(
            getUniqueId(),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(message)
        .setContentIntent(notificationPendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(getUniqueId(), notification)
}

fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())

fun getLocationPermissions(): Array<String> {
    return arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}
@RequiresApi(Build.VERSION_CODES.Q)
fun getBackgroundLocationPermission(): Array<String> {
    return arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
}
inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}


inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}
fun isSdkVersionGreaterThan(version: Int): Boolean {
    return Build.VERSION.SDK_INT > version
}

fun isSdkVersionGreaterThanOrEqualTo(version: Int): Boolean {
    return Build.VERSION.SDK_INT >= version
}

fun isSdkVersionLessThan(version: Int): Boolean {
    return Build.VERSION.SDK_INT < version
}

fun isSdkVersionLessThanOrEqualTo(version: Int): Boolean {
    return Build.VERSION.SDK_INT <= version
}

fun isSdkVersionEqualTo(version: Int): Boolean {
    return Build.VERSION.SDK_INT == version
}
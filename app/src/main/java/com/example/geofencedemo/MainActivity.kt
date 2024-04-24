package com.example.geofencedemo

import android.Manifest
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnticipateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.geofencedemo.databinding.ActivityMainBinding
import com.example.geofencedemo.geofence.GeofenceData
import com.example.geofencedemo.geofence.parcelable
import com.example.geofencedemo.geofence.showGeofenceInMap
import com.example.geofencedemo.permissionutils.runWithPermissions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar

class MainActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var splashScreen : SplashScreen

    companion object {
        private const val EXTRA_LAT_LNG = "EXTRA_LAT_LNG"
        private const val TIMER_ANIMATION: Long = 1200
        fun newIntent(context: Context, latLng: LatLng): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_LAT_LNG, latLng)
            }
        }
    }

    private val addGeoFenceActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                showGeoFences()

                val reminder = getRepository().getLast()
                reminder?.latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) }
                    ?.let { googleMap?.moveCamera(it) }

                Snackbar.make(binding.main, R.string.geofence_added_success, Snackbar.LENGTH_LONG)
                    .show()
            }
        }


    private var googleMap: GoogleMap? = null
    private lateinit var locationManager: LocationManager
    private lateinit var binding: ActivityMainBinding

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // keep splash screen on-screen indefinitely.
        // keepSplashScreenIndefinitely()

        // if you want to use custom exit animation.
        customSplashAnimator()
        // customIconSplashAnimator()

        // keep splash screen when load data viewModel.
        splashScreenWhenViewModel()

        setContentView(initViewBinding(layoutInflater))

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.apply {
            newGeofence.visibility = View.GONE

            newGeofence.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) runWithPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION) {
                    startGeofenceActivity()
                }
                else startGeofenceActivity()
            }
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        handlePermission()
    }

    private fun startGeofenceActivity() {
        googleMap?.run {
            val intent = AddGeoFenceActivity.newIntent(
                this@MainActivity, cameraPosition.target, cameraPosition.zoom
            )
            addGeoFenceActivityLauncher.launch(intent)
        }
    }


    private fun initViewBinding(inflater: LayoutInflater): View {
        binding = ActivityMainBinding.inflate(inflater)
        return binding.root
    }

    private fun onMapAndPermissionReady() {
        googleMap?.let { map ->
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                map.isMyLocationEnabled = true
                binding.newGeofence.visibility = View.VISIBLE
                showGeoFences()
                centerCamera()
                goToMyPosition()
            }
        }
    }

    private fun handlePermission() = runWithPermissions(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    ) {
        onMapAndPermissionReady()
    }


    private fun centerCamera() {
        intent.extras?.let { extras ->
            if (extras.containsKey(EXTRA_LAT_LNG)) {
                val latLng = extras.parcelable<LatLng>(EXTRA_LAT_LNG) as LatLng
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        }
    }

    private fun goToMyPosition() {
        val location = getLastKnownLocation()
        location?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    private fun showGeoFences() {
        googleMap?.run {
            clear()
            for (geofence in getRepository().getAll()) {
                showGeofenceInMap(this@MainActivity, this, geofence)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap?.run {
            uiSettings.isMyLocationButtonEnabled = true
            uiSettings.isMapToolbarEnabled = false
            //uiSettings.isZoomGesturesEnabled = true
          //  uiSettings.isZoomControlsEnabled = true
            uiSettings.setAllGesturesEnabled(true)
            setOnMarkerClickListener(this@MainActivity)
        }
        handlePermission()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val geofence = getRepository().get(marker.tag as String)
        geofence?.let {
            showRemoveGeofenceAlert(geofence)
        }
        return true
    }

    private fun showRemoveGeofenceAlert(geofenceData: GeofenceData) {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.run {
            setMessage(getString(R.string.geofence_removal_alert))
            setButton(
                AlertDialog.BUTTON_POSITIVE, getString(R.string.geofence_removal_alert_positive)
            ) { dialog, _ ->
                removeGeofence(geofenceData)
                dialog.dismiss()
            }
            setButton(
                AlertDialog.BUTTON_NEGATIVE, getString(R.string.geofence_removal_alert_negative)
            ) { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun removeGeofence(geofenceData: GeofenceData) {
        getRepository().removeGeofence(geofenceData, success = {
            showGeoFences()
            Snackbar.make(
                binding.main, R.string.geofence_removed_success, Snackbar.LENGTH_LONG
            ).show()
        }, failure = {
            Snackbar.make(binding.main, it, Snackbar.LENGTH_LONG).show()
        })
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        val locationManager = this.getSystemService<LocationManager>() ?: return null
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                bestLocation = l
            }
        }
        return bestLocation
    }
    /**
     * Use customize exit animation for splash screen.
     */
    @SuppressLint("ResourceAsColor")
    private fun customSplashAnimator() {
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val customAnimation = CustomScreenAnimator()
            val animation = customAnimation.alphaAnimation(splashScreenView)

            val animatorSet = AnimatorSet()
            animatorSet.duration = TIMER_ANIMATION
            animatorSet.interpolator = AnticipateInterpolator()
            animatorSet.playTogether(animation)

            animatorSet.doOnEnd {
                splashScreenView.remove()
            }
            animatorSet.start()
        }
    }

    /**
     * Keep splash screen on-screen indefinitely. This is useful if you're using a custom Activity
     * for routing.
     */
    private fun keepSplashScreenIndefinitely() {
        splashScreen.setKeepOnScreenCondition { true }
    }

    /**
     * Keep splash screen on-screen for longer period. This is useful if you need to load data when
     * splash screen is appearing.
     */
    private fun splashScreenWhenViewModel() {
        val content: View = findViewById(android.R.id.content)
        val model = ViewModelSplash()
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (model.isDataReady()) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else false
                }
            }
        )
    }
}
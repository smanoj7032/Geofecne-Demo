package com.example.geofencedemo.permissionutils

data class QuickPermissionsOptions(
    var handleRationale: Boolean = false,
    var rationaleMessage: String = "",
    var handlePermanentlyDenied: Boolean = true,
    var permanentlyDeniedMessage: String = "",
    var rationaleMethod: ((QuickPermissionsRequest) -> Unit)? = null,
    var permanentDeniedMethod: ((QuickPermissionsRequest) -> Unit)? = null,
    var permissionsDeniedMethod: ((QuickPermissionsRequest) -> Unit)? = null
)
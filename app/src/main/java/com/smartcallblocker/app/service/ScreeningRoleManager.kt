package com.smartcallblocker.app.service

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

/** Helper for the UI: prompts the user to set us as the default call screening app. */
object ScreeningRoleManager {

    fun isAvailable(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val rm = context.getSystemService(RoleManager::class.java) ?: return false
        return rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)
    }

    fun isHeld(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val rm = context.getSystemService(RoleManager::class.java) ?: return false
        return rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestIntent(context: Context): Intent? {
        val rm = context.getSystemService(RoleManager::class.java) ?: return null
        return rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun launch(activity: Activity, requestCode: Int = ROLE_REQUEST_CODE) {
        requestIntent(activity)?.let { activity.startActivityForResult(it, requestCode) }
    }

    const val ROLE_REQUEST_CODE = 0x1A11
}

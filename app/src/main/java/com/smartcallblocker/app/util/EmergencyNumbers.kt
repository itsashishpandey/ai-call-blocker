package com.smartcallblocker.app.util

import android.content.Context
import android.os.Build
import android.telephony.PhoneNumberUtils
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyNumbers @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val fallback = setOf(
        "911", "112", "100", "101", "102", "108", "999", "000", "110", "119", "120", "118", "113",
    )

    fun isEmergency(raw: String?): Boolean {
        if (raw.isNullOrBlank()) return false
        val digits = raw.filter { it.isDigit() }
        if (digits in fallback) return true
        return try {
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPlatform(digits)
            } else {
                PhoneNumberUtils.isEmergencyNumber(digits)
            }
        } catch (_: Throwable) {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPlatform(number: String): Boolean = try {
        val tm = context.getSystemService(android.telephony.TelephonyManager::class.java)
        tm?.isEmergencyNumber(number) ?: false
    } catch (_: Throwable) {
        false
    }
}

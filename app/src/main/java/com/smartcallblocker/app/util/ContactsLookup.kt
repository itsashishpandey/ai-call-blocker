package com.smartcallblocker.app.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsLookup @Inject constructor(
    @ApplicationContext private val context: Context,
    private val normalizer: PhoneNumberNormalizer,
) {

    private fun hasContactsPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS,
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Returns the display name for a phone number, or null if not in contacts / no permission.
     */
    fun displayName(phoneNumber: String?): String? {
        if (phoneNumber.isNullOrBlank() || !hasContactsPermission()) return null
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber),
        )
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        } catch (_: SecurityException) {
            null
        }
    }

    fun isKnownContact(phoneNumber: String?): Boolean = displayName(phoneNumber) != null

    fun allContactNumbers(): List<Pair<String, String>> {
        if (!hasContactsPermission()) return emptyList()
        val results = mutableListOf<Pair<String, String>>()
        return try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                ),
                null, null, null,
            )?.use { cursor ->
                val nameIdx = cursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                )
                val numIdx = cursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                )
                while (cursor.moveToNext()) {
                    val n = cursor.getString(numIdx) ?: continue
                    val name = cursor.getString(nameIdx) ?: ""
                    results += name to n
                }
            }
            results
        } catch (_: SecurityException) {
            emptyList()
        }
    }
}

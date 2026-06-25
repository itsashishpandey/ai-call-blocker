package com.smartcallblocker.app.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps libphonenumber so the rest of the app gets a uniform E.164 (or best-effort) representation.
 *
 * Private/empty caller IDs are surfaced via PhoneState — they never reach the normalizer.
 */
@Singleton
class PhoneNumberNormalizer @Inject constructor() {

    private val util: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    fun defaultRegion(): String =
        Locale.getDefault().country.takeIf { it.isNotBlank() } ?: "US"

    fun normalize(raw: String?, region: String = defaultRegion()): String {
        if (raw.isNullOrBlank()) return ""
        val cleaned = raw.replace(Regex("[\\s\\-().]"), "")
        return try {
            val parsed: PhoneNumber = util.parse(cleaned, region)
            util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (_: NumberParseException) {
            // Fall back to stripped digits + leading + if present
            val plus = if (cleaned.startsWith("+")) "+" else ""
            plus + cleaned.filter { it.isDigit() }
        }
    }

    fun countryCode(normalized: String): String? {
        return try {
            val parsed = util.parse(normalized, defaultRegion())
            "+${parsed.countryCode}"
        } catch (_: NumberParseException) {
            null
        }
    }

    fun areaCode(normalized: String): String? {
        return try {
            val parsed = util.parse(normalized, defaultRegion())
            val ndcLen = util.getLengthOfNationalDestinationCode(parsed)
            if (ndcLen > 0) {
                parsed.nationalNumber.toString().take(ndcLen)
            } else null
        } catch (_: NumberParseException) {
            null
        }
    }

    fun digitCount(normalized: String): Int =
        normalized.count { it.isDigit() }

    /**
     * Returns every reasonable string form a user might type in a rule pattern.
     *
     *   "+919876543210"  → ["+919876543210", "919876543210", "9876543210"]
     *                       full E.164      digits-only      national portion
     *
     * Rule matching compares the pattern to ALL of these so a user can write
     * a rule as "+91", "91" or "9876" and the engine still does the right thing.
     */
    fun matchableForms(normalized: String): List<String> {
        if (normalized.isEmpty()) return emptyList()
        val results = mutableListOf(normalized)
        if (normalized.startsWith("+")) {
            val withoutPlus = normalized.substring(1)
            if (withoutPlus.isNotEmpty() && withoutPlus !in results) results += withoutPlus
            val cc = countryCode(normalized)?.removePrefix("+")
            if (cc != null && cc.isNotEmpty() && withoutPlus.startsWith(cc)) {
                val national = withoutPlus.substring(cc.length)
                if (national.isNotEmpty() && national !in results) results += national
            }
        }
        return results
    }

    /** Strips whitespace, dashes, parens, and dots while keeping leading + and digits. */
    fun cleanPattern(raw: String): String {
        if (raw.isEmpty()) return raw
        return raw.replace(Regex("[\\s\\-().]"), "")
    }
}

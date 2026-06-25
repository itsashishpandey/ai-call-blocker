package com.smartcallblocker.app.util

/**
 * Masks a phone number when Privacy Mode is on.
 *
 *   +919876543210 → +91 ******3210
 *   9876543210    → ******3210
 *   ""            → "Private"
 *
 * Keeps the country code (if present) and the last 4 digits.
 */
object PhoneNumberMasker {

    fun mask(number: String?, enabled: Boolean): String {
        if (!enabled) return number.orEmpty()
        if (number.isNullOrBlank()) return "Private"

        val digits = number.filter { it.isDigit() }
        if (digits.length <= 4) return "****"

        val keepTail = digits.takeLast(4)
        val plus = if (number.startsWith("+")) "+" else ""

        // If we have a country code (number starts with + and has > 10 digits), keep first 2 country digits
        val head = if (plus.isNotEmpty() && digits.length > 10) digits.take(2) else ""

        val maskedMiddle = "*".repeat((digits.length - keepTail.length - head.length).coerceAtLeast(3))
        return buildString {
            append(plus)
            if (head.isNotEmpty()) {
                append(head)
                append(' ')
            }
            append(maskedMiddle)
            append(keepTail)
        }
    }

    /**
     * Mask a contact name when privacy mode is on:
     *   "John Doe" → "J••• ••• D••"
     */
    fun maskName(name: String?, enabled: Boolean): String {
        if (!enabled) return name.orEmpty()
        if (name.isNullOrBlank()) return ""
        return name.split(" ").joinToString(" ") { word ->
            if (word.length <= 1) word
            else word.first() + "•".repeat((word.length - 1).coerceAtMost(4))
        }
    }
}

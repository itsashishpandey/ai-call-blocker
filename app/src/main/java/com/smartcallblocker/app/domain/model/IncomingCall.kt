package com.smartcallblocker.app.domain.model

/** What the screening service hands to the rule engine. */
data class IncomingCall(
    val rawNumber: String?,
    val normalizedNumber: String,
    val isPrivate: Boolean,
    val isEmpty: Boolean,
    val isKnownContact: Boolean,
    /**
     * True when the carrier / telecom network told us the caller's identity could
     * not be verified — i.e. Android's `Call.Details.callerNumberVerificationStatus`
     * returned `VERIFICATION_STATUS_FAILED` (STIR/SHAKEN failure). This is the
     * standard, cross-OEM signal that a call is likely spoofed/spam.
     */
    val isCarrierFlaggedSpam: Boolean,
    val callerName: String?,
    val timestamp: Long,
)

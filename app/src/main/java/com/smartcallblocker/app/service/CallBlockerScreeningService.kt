package com.smartcallblocker.app.service

import android.net.Uri
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import android.util.Log
import com.smartcallblocker.app.data.db.entities.BlockedCallEntity
import com.smartcallblocker.app.data.preferences.SettingsRepository
import com.smartcallblocker.app.data.repository.BlockedCallRepository
import com.smartcallblocker.app.domain.engine.RuleEngine
import com.smartcallblocker.app.domain.model.BlockAction
import com.smartcallblocker.app.domain.model.CallDecision
import com.smartcallblocker.app.domain.model.IncomingCall
import com.smartcallblocker.app.util.ContactsLookup
import com.smartcallblocker.app.util.PhoneNumberNormalizer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * The OS hands every incoming call here when this app is set as the default screening app.
 *
 * The engine has a hard time budget — but we deliberately give ourselves up to 1.5 s
 * because first-call-after-boot has to wait for Hilt + Room + DataStore to spin up.
 */
@AndroidEntryPoint
class CallBlockerScreeningService : CallScreeningService() {

    @Inject lateinit var ruleEngine: RuleEngine
    @Inject lateinit var blockedRepo: BlockedCallRepository
    @Inject lateinit var settings: SettingsRepository
    @Inject lateinit var normalizer: PhoneNumberNormalizer
    @Inject lateinit var contacts: ContactsLookup

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val outcome: Pair<CallDecision, ServiceSnapshot> = try {
            runBlocking(Dispatchers.IO) {
                withTimeoutOrNull(TIMEOUT_MS) {
                    val snap = ServiceSnapshot(
                        protectionEnabled = settings.protectionEnabledSnapshot(),
                        saveLogs = settings.saveLogsSnapshot(),
                        skipNotification = settings.skipNotificationSnapshot(),
                        skipCallLog = settings.skipCallLogSnapshot(),
                    )
                    if (!snap.protectionEnabled) {
                        CallDecision.Allow to snap
                    } else {
                        val incoming = buildIncomingCall(callDetails)
                        ruleEngine.evaluate(incoming) to snap
                    }
                } ?: (CallDecision.Allow to ServiceSnapshot())
            }
        } catch (t: Throwable) {
            // Never let an unexpected exception crash the OS-bound service —
            // fail safe to Allow and log so the user isn't silently locked out.
            Log.e(TAG, "Screening failed; allowing call", t)
            CallDecision.Allow to ServiceSnapshot()
        }
        applyDecision(callDetails, outcome.first, outcome.second)
    }

    private fun buildIncomingCall(details: Call.Details): IncomingCall {
        val handle: Uri? = details.handle
        val raw: String? = handle?.let { uri ->
            if ("tel".equals(uri.scheme, ignoreCase = true)) Uri.decode(uri.schemeSpecificPart) else null
        }
        val isPrivate = details.handlePresentation == android.telecom.TelecomManager.PRESENTATION_RESTRICTED
        val isEmpty = raw.isNullOrBlank() && !isPrivate
        val normalized = normalizer.normalize(raw)
        val knownName = contacts.displayName(raw)

        // STIR/SHAKEN — Android 11+ exposes the carrier's caller-identity verdict.
        // FAILED is the standard signal that this call is spoofed / very likely spam.
        val carrierFlaggedSpam = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            details.callerNumberVerificationStatus == Connection.VERIFICATION_STATUS_FAILED
        } else {
            false
        }

        return IncomingCall(
            rawNumber = raw,
            normalizedNumber = normalized,
            isPrivate = isPrivate,
            isEmpty = isEmpty,
            isKnownContact = knownName != null,
            isCarrierFlaggedSpam = carrierFlaggedSpam,
            callerName = knownName,
            timestamp = System.currentTimeMillis(),
        )
    }

    private fun applyDecision(
        details: Call.Details,
        decision: CallDecision,
        snapshot: ServiceSnapshot,
    ) {
        val builder = CallResponse.Builder()
        when (decision) {
            is CallDecision.Allow -> {
                respondToCall(details, builder.build())
            }
            is CallDecision.Block -> {
                builder.apply {
                    when (decision.action) {
                        BlockAction.REJECT -> {
                            setDisallowCall(true); setRejectCall(true)
                        }
                        BlockAction.SILENCE -> setSilenceCall(true)
                        BlockAction.DISALLOW -> setDisallowCall(true)
                    }
                    setSkipCallLog(snapshot.skipCallLog)
                    setSkipNotification(snapshot.skipNotification)
                }
                respondToCall(details, builder.build())

                // Log async — don't tie up the OS thread.
                if (snapshot.saveLogs) {
                    val handle = details.handle
                    val raw = if (handle != null && "tel".equals(handle.scheme, ignoreCase = true))
                        Uri.decode(handle.schemeSpecificPart) else null
                    ioScope.launch {
                        val normalized = normalizer.normalize(raw)
                        val name = contacts.displayName(raw)
                        runCatching {
                            blockedRepo.log(
                                BlockedCallEntity(
                                    phoneNumber = raw.orEmpty(),
                                    normalizedNumber = normalized,
                                    callerName = name,
                                    callDateTime = System.currentTimeMillis(),
                                    action = when (decision.action) {
                                        BlockAction.REJECT -> "REJECTED"
                                        BlockAction.SILENCE -> "SILENCED"
                                        BlockAction.DISALLOW -> "BLOCKED"
                                    },
                                    matchedRuleId = decision.matchedRuleId,
                                    matchedRuleName = decision.matchedRuleName ?: decision.reason,
                                    matchedRuleType = decision.matchedRuleType,
                                    countryCode = normalizer.countryCode(normalized),
                                    isKnownContact = name != null,
                                    spamScore = decision.spamScore,
                                    temporaryBlockedUntil = decision.temporaryUntil,
                                    notes = decision.reason,
                                ),
                            )
                        }.onFailure { Log.e(TAG, "Failed to log blocked call", it) }
                    }
                }
            }
        }
    }

    private data class ServiceSnapshot(
        val protectionEnabled: Boolean = false,
        val saveLogs: Boolean = false,
        val skipNotification: Boolean = false,
        val skipCallLog: Boolean = false,
    )

    companion object {
        private const val TAG = "CallScreener"
        // Generous timeout — the OS allows several seconds; first-call-after-boot needs the headroom.
        private const val TIMEOUT_MS = 1500L
    }
}

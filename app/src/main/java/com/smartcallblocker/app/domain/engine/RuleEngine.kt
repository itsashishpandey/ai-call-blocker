package com.smartcallblocker.app.domain.engine

import com.smartcallblocker.app.data.db.dao.BlacklistDao
import com.smartcallblocker.app.data.db.dao.BlockingRuleDao
import com.smartcallblocker.app.data.db.dao.CallEventDao
import com.smartcallblocker.app.data.db.dao.TemporaryBlockDao
import com.smartcallblocker.app.data.db.dao.WhitelistDao
import com.smartcallblocker.app.data.db.entities.BlockingRuleEntity
import com.smartcallblocker.app.data.db.entities.CallEventEntity
import com.smartcallblocker.app.data.db.entities.TemporaryBlockEntity
import com.smartcallblocker.app.data.preferences.SettingsRepository
import com.smartcallblocker.app.domain.model.BlockAction
import com.smartcallblocker.app.domain.model.CallDecision
import com.smartcallblocker.app.domain.model.IncomingCall
import com.smartcallblocker.app.domain.model.RuleType
import com.smartcallblocker.app.util.EmergencyNumbers
import com.smartcallblocker.app.util.PhoneNumberNormalizer
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The brain. Given an IncomingCall, returns Allow or Block.
 *
 * Priority (matches spec §9.1):
 *   1. Emergency numbers → always allow
 *   2. Whitelist          → allow (overrides everything else)
 *   3. Active temporary block → block
 *   4. Blacklist          → block
 *   5. User rules         → block if any match
 *   6. Repeated-call detection → may add a new temp block
 *   7. Default            → allow
 */
@Singleton
class RuleEngine @Inject constructor(
    private val ruleDao: BlockingRuleDao,
    private val whitelistDao: WhitelistDao,
    private val blacklistDao: BlacklistDao,
    private val tempDao: TemporaryBlockDao,
    private val eventDao: CallEventDao,
    private val settings: SettingsRepository,
    private val emergency: EmergencyNumbers,
    private val normalizer: PhoneNumberNormalizer,
) {

    /** Evaluate a call. Side effects (temp block insert / call event insert) happen inside. */
    suspend fun evaluate(call: IncomingCall): CallDecision {
        // 1. Emergency
        if (emergency.isEmergency(call.rawNumber)) return CallDecision.Allow

        // 1b. Safe mode active → bypass everything
        if (settings.safeModeActive()) return CallDecision.Allow

        val key = call.normalizedNumber

        // 2. Whitelist overrides
        if (key.isNotEmpty() && whitelistDao.contains(key)) return CallDecision.Allow

        // Record the event for repeated-call detection (before deciding)
        if (key.isNotEmpty()) {
            eventDao.insert(CallEventEntity(normalizedNumber = key, eventTime = call.timestamp))
        }

        // 2b. Carrier-flagged spam (STIR/SHAKEN failure) — toggleable, off by default.
        // Comes AFTER the whitelist so a trusted contact is never blocked by a verification quirk.
        if (call.isCarrierFlaggedSpam && settings.blockCarrierSpamSnapshot()) {
            return CallDecision.Block(
                action = BlockAction.REJECT,
                reason = "Carrier flagged as spam (verification failed)",
                matchedRuleName = "Carrier spam",
                matchedRuleType = "CARRIER_SPAM",
            )
        }

        // 3. Temporary block in effect?
        if (key.isNotEmpty()) {
            tempDao.expireOld(call.timestamp)
            val active = tempDao.findByNumber(key)
            if (active != null && active.isActive && active.blockedUntil > call.timestamp) {
                return CallDecision.Block(
                    action = BlockAction.REJECT,
                    reason = "Temporary block active",
                    matchedRuleType = RuleType.REPEATED_CALLS.name,
                    temporaryUntil = active.blockedUntil,
                )
            }
        }

        // 4. Blacklist
        if (key.isNotEmpty() && blacklistDao.contains(key)) {
            return CallDecision.Block(
                action = settings.defaultActionSnapshot(),
                reason = "In blacklist",
                matchedRuleName = "Blacklist",
                matchedRuleType = "BLACKLIST",
            )
        }

        // 5. User-defined rules
        val rules = ruleDao.enabledSnapshot()
        val nowCal = Calendar.getInstance().apply { timeInMillis = call.timestamp }
        val match = rules.firstOrNull { it.matches(call, nowCal) }
        if (match != null) {
            // Allow-only-contacts is technically inverted: if rule matches and number is NOT a contact, block.
            return CallDecision.Block(
                action = blockActionFor(match.action),
                reason = match.ruleName,
                matchedRuleId = match.id,
                matchedRuleName = match.ruleName,
                matchedRuleType = match.ruleType,
            )
        }

        // 6. Repeated-call detection (built-in default)
        if (key.isNotEmpty() && settings.repeatedCallsEnabledSnapshot()) {
            val limit = settings.repeatedCallLimitSnapshot()
            val windowMs = settings.repeatedCallWindowMinutesSnapshot() * 60_000L
            val durationMs = settings.repeatedCallBlockMinutesSnapshot() * 60_000L
            val recent = eventDao.countSince(key, call.timestamp - windowMs)
            if (recent >= limit) {
                tempDao.upsert(
                    TemporaryBlockEntity(
                        phoneNumber = call.rawNumber.orEmpty(),
                        normalizedNumber = key,
                        callCount = recent,
                        firstCallTime = call.timestamp - windowMs,
                        lastCallTime = call.timestamp,
                        blockedUntil = call.timestamp + durationMs,
                        ruleId = null,
                        isActive = true,
                    ),
                )
                return CallDecision.Block(
                    action = BlockAction.REJECT,
                    reason = "Repeated calls ($recent in window)",
                    matchedRuleType = RuleType.REPEATED_CALLS.name,
                    temporaryUntil = call.timestamp + durationMs,
                )
            }
        }

        // 7. Default
        return CallDecision.Allow
    }

    private fun blockActionFor(actionString: String): BlockAction = when (actionString.uppercase()) {
        "SILENCE" -> BlockAction.SILENCE
        "DISALLOW" -> BlockAction.DISALLOW
        else -> BlockAction.REJECT
    }

    // ---------- Rule matcher ----------

    private fun BlockingRuleEntity.matches(call: IncomingCall, cal: Calendar): Boolean {
        if (!isEnabled) return false
        if (!withinSchedule(cal)) return false

        val type = RuleType.fromName(ruleType) ?: return false
        val n = call.normalizedNumber

        return when (type) {
            RuleType.UNKNOWN_NUMBERS -> !call.isPrivate && !call.isEmpty && !call.isKnownContact
            RuleType.KNOWN_NUMBERS -> call.isKnownContact
            RuleType.PRIVATE_NUMBERS -> call.isPrivate
            RuleType.EMPTY_CALLER_ID -> call.isEmpty
            RuleType.STARTS_WITH -> {
                val target = normalizer.cleanPattern(ruleValue.trim())
                if (target.isEmpty() || n.isEmpty()) false
                else normalizer.matchableForms(n).any { it.startsWith(target) }
            }
            RuleType.ENDS_WITH -> {
                val target = normalizer.cleanPattern(ruleValue.trim())
                if (target.isEmpty() || n.isEmpty()) false
                else normalizer.matchableForms(n).any { it.endsWith(target) }
            }
            RuleType.CONTAINS -> {
                val target = normalizer.cleanPattern(ruleValue.trim())
                if (target.isEmpty() || n.isEmpty()) false
                else normalizer.matchableForms(n).any { it.contains(target) }
            }
            RuleType.EXACT -> {
                val raw = normalizer.cleanPattern(ruleValue.trim())
                if (raw.isEmpty() || n.isEmpty()) false
                else {
                    val target = normalizer.normalize(raw)
                    val forms = normalizer.matchableForms(n)
                    if (target.isNotEmpty() && (n == target || forms.contains(target))) true
                    else {
                        // Fall back to digit-only equality so "9876543210" matches "+919876543210".
                        val rawDigits = raw.filter { it.isDigit() }
                        if (rawDigits.isEmpty()) false
                        else forms.any { it.filter { c -> c.isDigit() } == rawDigits }
                    }
                }
            }
            RuleType.LESS_THAN_DIGITS -> {
                val limit = ruleValue.trim().toIntOrNull() ?: return false
                n.isNotEmpty() && normalizer.digitCount(n) < limit
            }
            RuleType.GREATER_THAN_DIGITS -> {
                val limit = ruleValue.trim().toIntOrNull() ?: return false
                n.isNotEmpty() && normalizer.digitCount(n) > limit
            }
            RuleType.COUNTRY_CODE -> {
                val target = normalizer.cleanPattern(ruleValue.trim()).removePrefix("+")
                if (target.isEmpty() || n.isEmpty()) false
                else n.startsWith("+$target")
            }
            RuleType.AREA_CODE -> {
                val target = ruleValue.trim()
                if (target.isEmpty()) return false
                val area = normalizer.areaCode(n)
                area != null && area == target
            }
            RuleType.TOLL_FREE -> isTollFreePrefix(n)
            RuleType.PREMIUM -> isPremiumPrefix(n)
            RuleType.ALLOW_ONLY_CONTACTS -> !call.isKnownContact
            RuleType.REGEX -> runCatching { Regex(ruleValue).containsMatchIn(n) }.getOrDefault(false)
            RuleType.SPAM_SCORE -> false // future AI module; rule never matches on its own
            RuleType.SCHEDULE -> true    // schedule rule blocks whenever active
            RuleType.REPEATED_CALLS -> false // handled inline above
        }
    }

    private fun BlockingRuleEntity.withinSchedule(cal: Calendar): Boolean {
        val start = scheduleStart ?: return true
        val end = scheduleEnd ?: return true
        val mask = scheduleDaysMask ?: 0x7F   // every day
        val day = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday
        val dayBit = 1 shl (day - 1)
        if (mask and dayBit == 0) return false
        val mins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        return if (start <= end) mins in start..end else mins >= start || mins <= end
    }

    private fun isTollFreePrefix(n: String): Boolean {
        if (n.isEmpty()) return false
        // US/CA toll-free + India toll-free
        return listOf("+1800", "+1888", "+1877", "+1866", "+1855", "+1844", "+1833",
            "+91180", "+911800").any { n.startsWith(it) }
    }

    private fun isPremiumPrefix(n: String): Boolean {
        if (n.isEmpty()) return false
        return listOf("+1900", "+1976").any { n.startsWith(it) }
    }
}

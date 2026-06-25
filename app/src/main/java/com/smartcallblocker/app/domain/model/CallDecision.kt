package com.smartcallblocker.app.domain.model

/**
 * The decision produced by the rule engine. Translated to CallResponse flags by the screening service.
 */
sealed interface CallDecision {

    data object Allow : CallDecision

    data class Block(
        val action: BlockAction,
        val reason: String,
        val matchedRuleId: Long? = null,
        val matchedRuleName: String? = null,
        val matchedRuleType: String? = null,
        val spamScore: Int = 0,
        val temporaryUntil: Long? = null,
    ) : CallDecision
}

enum class BlockAction {
    REJECT,      // disallow + reject
    SILENCE,     // mute ringer, still logged
    DISALLOW,    // disallow but don't reject (let voicemail catch it)
}

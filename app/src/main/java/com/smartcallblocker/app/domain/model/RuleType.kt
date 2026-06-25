package com.smartcallblocker.app.domain.model

/** Every supported rule kind. Stored as the string name in the DB. */
enum class RuleType(val displayName: String, val needsValue: Boolean) {
    UNKNOWN_NUMBERS("Block unknown numbers", false),
    KNOWN_NUMBERS("Block known contacts (strict)", false),
    PRIVATE_NUMBERS("Block private/hidden numbers", false),
    EMPTY_CALLER_ID("Block empty caller ID", false),
    STARTS_WITH("Number starts with", true),
    ENDS_WITH("Number ends with", true),
    CONTAINS("Number contains", true),
    EXACT("Exact number", true),
    LESS_THAN_DIGITS("Fewer than X digits", true),
    GREATER_THAN_DIGITS("More than X digits", true),
    COUNTRY_CODE("Country code", true),
    AREA_CODE("Area / STD code", true),
    TOLL_FREE("Toll-free numbers", false),
    PREMIUM("Premium numbers", false),
    REPEATED_CALLS("Repeated calls", true),
    ALLOW_ONLY_CONTACTS("Allow only contacts", false),
    REGEX("Regex pattern", true),
    SPAM_SCORE("Spam score threshold", true),
    SCHEDULE("Scheduled blocking", true);

    companion object {
        fun fromName(name: String): RuleType? = entries.firstOrNull { it.name == name }
    }
}

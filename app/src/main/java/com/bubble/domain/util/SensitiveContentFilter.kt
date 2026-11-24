package com.bubble.domain.util

/**
 * Pure Kotlin heuristic detection engine for sensitive 2FA, OTP, and banking communications.
 * Enforces zero leakage of authentication tokens or financial balances to screen overlays.
 */
object SensitiveContentFilter {

    const val MASKED_PLACEHOLDER = "•••••• (Tap to view)"

    // Permanent blacklist of dedicated authenticator and password manager packages
    private val SENSITIVE_PACKAGE_BLACKLIST = setOf(
        "com.google.android.apps.authenticator2", // Google Authenticator
        "com.authy.authy",                        // Twilio Authy
        "com.duosecurity.duomobile",              // Duo Mobile
        "com.microsoft.msa.authenticator",        // MS Authenticator
        "com.bitwarden.authenticator",            // Bitwarden Authenticator
        "com.onepassword.android",                // 1Password
        "com.lastpass.lpandroid"                  // LastPass
    )

    // High-confidence regex signatures for 2FA / OTP verification codes
    private val OTP_PATTERNS = listOf(
        Regex("(?i)\\b(otp|code|verification|passcode|secret|pin)\\b.*\\b\\d{4,8}\\b"),
        Regex("(?i)\\b\\d{4,8}\\b.*\\b(is your verification code|is your OTP|is your login code|is your verification)\\b"),
        Regex("(?i)\\bsecurity code:\\s*\\d{4,8}\\b"),
        Regex("(?i)\\buse\\s+\\d{4,8}\\s+to\\s+verify\\b"),
        Regex("(?i)\\bverification code:\\s*\\d{4,8}\\b"),
        Regex("(?i)\\blogin code:\\s*\\d{4,8}\\b"),
        Regex("(?i)\\byour code is\\s*\\d{4,8}\\b")
    )

    // High-confidence financial transaction alerts
    private val FINANCIAL_PATTERNS = listOf(
        Regex("(?i)\\b(debited|credited|withdrawn|transaction of|balance:)\\b"),
        Regex("(?i)\\b(cvv|card ending in|account ending in|acct ending in)\\b"),
        Regex("(?i)\\b(spent|payment of)\\s+[$€£₹¥]?\\s*\\d+([.,]\\d{2})?\\b")
    )

    /**
     * Checks whether an app is a dedicated authenticator whose alerts must never spawn bubbles.
     */
    fun isBlacklistedPackage(packageName: String): Boolean {
        return SENSITIVE_PACKAGE_BLACKLIST.contains(packageName)
    }

    /**
     * Determines whether notification content carries sensitive OTP or banking data.
     */
    fun isSensitive(packageName: String, title: String?, text: String?): Boolean {
        if (isBlacklistedPackage(packageName)) {
            return true
        }

        val combinedContent = "${title.orEmpty()} ${text.orEmpty()}".trim()
        if (combinedContent.isBlank()) return false

        // Check OTP / 2FA patterns
        if (OTP_PATTERNS.any { it.containsMatchIn(combinedContent) }) {
            return true
        }

        // Check Financial patterns
        if (FINANCIAL_PATTERNS.any { it.containsMatchIn(combinedContent) }) {
            return true
        }

        return false
    }

    /**
     * Replaces sensitive content with privacy masking placeholder.
     */
    fun maskSnippet(): String = MASKED_PLACEHOLDER
}

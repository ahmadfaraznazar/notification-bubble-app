package com.bubble.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating 20 diverse banking, OTP, and conversational patterns against SensitiveContentFilter.
 */
class SensitiveContentFilterTest {

    @Test
    fun authenticatorPackages_areAlwaysIdentifiedAsSensitive() {
        assertTrue(SensitiveContentFilter.isSensitive("com.google.android.apps.authenticator2", "Google", "123456"))
        assertTrue(SensitiveContentFilter.isSensitive("com.authy.authy", "Authy", "Token: 987654"))
        assertTrue(SensitiveContentFilter.isSensitive("com.duosecurity.duomobile", "Duo", "Approve request"))
        assertTrue(SensitiveContentFilter.isSensitive("com.bitwarden.authenticator", "Bitwarden", "Code"))
        assertTrue(SensitiveContentFilter.isSensitive("com.onepassword.android", "1Password", "Vault locked"))
    }

    @Test
    fun otpAndVerificationPatterns_areIdentifiedAsSensitive() {
        // Pattern 1: standard OTP
        assertTrue(SensitiveContentFilter.isSensitive("com.sms", "Bank", "Your OTP is 482910. Do not share."))
        // Pattern 2: verification code
        assertTrue(SensitiveContentFilter.isSensitive("com.sms", "Google", "123456 is your verification code."))
        // Pattern 3: login code
        assertTrue(SensitiveContentFilter.isSensitive("com.sms", "Discord", "Your login code is 849201."))
        // Pattern 4: security code
        assertTrue(SensitiveContentFilter.isSensitive("com.sms", "Security", "Security code: 582104"))
        // Pattern 5: use X to verify
        assertTrue(SensitiveContentFilter.isSensitive("com.sms", "WhatsApp", "Use 938210 to verify your account."))
        // Pattern 6: passcode with 6 digits
        assertTrue(SensitiveContentFilter.isSensitive("com.sms", "Apple", "Your Apple ID passcode is 204918."))
        // Pattern 7: secret pin
        assertTrue(SensitiveContentFilter.isSensitive("com.sms", "Auth", "Your temporary secret pin is 8392."))
    }

    @Test
    fun financialAndBankingPatterns_areIdentifiedAsSensitive() {
        // Pattern 8: account debited
        assertTrue(SensitiveContentFilter.isSensitive("com.sms", "Chase", "Your account was debited $45.20 at Starbucks."))
        // Pattern 9: account credited
        assertTrue(SensitiveContentFilter.isSensitive("com.sms", "WellsFargo", "Your checking account was credited $1,200.00."))
        // Pattern 10: account ending in
        assertTrue(SensitiveContentFilter.isSensitive("com.sms", "Citi", "Alert: Card ending in 4920 spent $182.15."))
        // Pattern 11: transaction of
        assertTrue(SensitiveContentFilter.isSensitive("com.sms", "Bank", "A transaction of $99.00 was authorized."))
        // Pattern 12: balance check
        assertTrue(SensitiveContentFilter.isSensitive("com.sms", "Bank", "Account balance: $3,450.00 as of today."))
        // Pattern 13: CVV keyword
        assertTrue(SensitiveContentFilter.isSensitive("com.sms", "Card", "Do not share CVV with caller."))
    }

    @Test
    fun casualConversationalChats_areNOTIdentifiedAsSensitive() {
        // Pattern 14: Casual plan
        assertFalse(SensitiveContentFilter.isSensitive("com.whatsapp", "Alice", "Hey, are you free for dinner at 7?"))
        // Pattern 15: Coding update
        assertFalse(SensitiveContentFilter.isSensitive("org.telegram.messenger", "Bob", "Pushed the fix to branch feature-123"))
        // Pattern 16: Numbers in conversation
        assertFalse(SensitiveContentFilter.isSensitive("com.Slack", "Elena", "Please check slide 42 in the presentation."))
        // Pattern 17: Normal time and location
        assertFalse(SensitiveContentFilter.isSensitive("com.whatsapp", "David", "Meeting at 10am tomorrow morning."))
        // Pattern 18: Casual punctuation
        assertFalse(SensitiveContentFilter.isSensitive("com.whatsapp", "Mom", "Call me when you arrive home!"))
        // Pattern 19: Price discussion without transaction alert
        assertFalse(SensitiveContentFilter.isSensitive("org.telegram.messenger", "Mark", "That monitor costs 300 dollars."))
        // Pattern 20: Word contains 'pin' or 'code' as part of standard text
        assertFalse(SensitiveContentFilter.isSensitive("com.whatsapp", "Sam", "Did you decode the puzzle?"))
    }

    @Test
    fun maskSnippet_returnsMaskedPlaceholder() {
        assertEquals("•••••• (Tap to view)", SensitiveContentFilter.maskSnippet())
    }
}

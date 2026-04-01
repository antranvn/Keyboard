package com.securekey.sample

import android.app.Application
import com.securekey.sdk.SecureKey
import com.securekey.sdk.SecureKeyConfig
import com.securekey.sdk.security.SecurityLevel
import com.securekey.sdk.ui.ThemePresets

class SampleBankApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = SecureKeyConfig.Builder()
            .theme(ThemePresets.system(this))
            .securityLevel(SecurityLevel.MODERATE)
            .shuffleNumericKeys(false)
            .hapticFeedback(true)
            .keyPreview(true)
            .doneButtonLabel("Done")
            .build()

        SecureKey.init(this, config)
    }
}

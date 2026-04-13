package com.securekey.sample.xml.pin

import android.os.Bundle
import com.securekey.sample.databinding.ActivityPinSetupBinding
import com.securekey.sample.xml.SecureKeyActivity

class PinSetupActivity : SecureKeyActivity() {

    private lateinit var binding: ActivityPinSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, PinSetupFragment())
                .commit()
        }
    }
}

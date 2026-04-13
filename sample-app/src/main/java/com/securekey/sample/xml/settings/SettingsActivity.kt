package com.securekey.sample.xml.settings

import android.os.Bundle
import com.securekey.sample.databinding.ActivitySettingsBinding
import com.securekey.sample.xml.SecureKeyActivity

class SettingsActivity : SecureKeyActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, SettingsFragment())
                .commit()
        }
    }
}

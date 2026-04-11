package com.securekey.sample.xml.otp

import android.content.Intent
import android.os.Bundle
import com.securekey.sample.databinding.ActivityOtpBinding
import com.securekey.sample.xml.SecureKeyActivity
import com.securekey.sample.xml.pin.PinSetupActivity

class OtpActivity : SecureKeyActivity() {

    private lateinit var binding: ActivityOtpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        if (savedInstanceState == null) {
            OtpBottomSheetFragment.newInstance().show(supportFragmentManager, OtpBottomSheetFragment.TAG)
        }
    }

    fun onOtpVerified() {
        startActivity(Intent(this, PinSetupActivity::class.java))
        finish()
    }
}

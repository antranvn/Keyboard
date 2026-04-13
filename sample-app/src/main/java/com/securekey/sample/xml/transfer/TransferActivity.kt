package com.securekey.sample.xml.transfer

import android.os.Bundle
import com.securekey.sample.databinding.ActivityTransferBinding
import com.securekey.sample.xml.SecureKeyActivity

class TransferActivity : SecureKeyActivity() {

    private lateinit var binding: ActivityTransferBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, TransferFragment())
                .commit()
        }
    }
}

package com.securekey.sample.xml.login

import android.os.Bundle
import com.securekey.sample.databinding.ActivityLoginBinding
import com.securekey.sample.xml.SecureKeyActivity

class LoginActivity : SecureKeyActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, LoginFragment())
                .commit()
        }
    }
}

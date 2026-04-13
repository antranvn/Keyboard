package com.securekey.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.securekey.sample.databinding.ActivityEntryBinding
import com.securekey.sample.xml.login.LoginActivity

class EntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCompose.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.btnXml.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}

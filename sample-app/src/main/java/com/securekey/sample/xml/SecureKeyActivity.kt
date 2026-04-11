package com.securekey.sample.xml

import androidx.appcompat.app.AppCompatActivity
import com.securekey.sdk.SecureKey

/**
 * Base Activity for XML sample screens. Wires SecureKey into the standard
 * Activity lifecycle so individual screens don't have to repeat the hooks.
 *
 * Binding runs in onStart so back-navigation to an already-created activity
 * re-attaches the SDK to the correct window.
 */
abstract class SecureKeyActivity : AppCompatActivity() {

    override fun onStart() {
        super.onStart()
        SecureKey.getInstance().bind(this)
    }

    override fun onResume() {
        super.onResume()
        SecureKey.getInstance().onResume()
    }

    override fun onPause() {
        super.onPause()
        SecureKey.getInstance().onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        SecureKey.getInstance().onDestroy()
    }
}

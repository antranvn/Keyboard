package com.securekey.sample.ui.screens

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.widget.LinearLayout
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.securekey.sdk.SecureKey
import com.securekey.sdk.core.KeyboardMode
import com.securekey.sdk.ui.SecureEditText

@Composable
fun OtpScreen(onVerified: () -> Unit) {
    val otpLength = 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "OTP Verification",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter the 6-digit code sent to your phone",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // OTP input row using SecureEditText fields
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER

                    var ignoreTextChanges = false
                    val fields = mutableListOf<SecureEditText>()

                    for (i in 0 until otpLength) {
                        val field = SecureEditText(context).apply {
                            inputType = InputType.TYPE_CLASS_NUMBER
                            gravity = Gravity.CENTER
                            textSize = 24f
                            maxLines = 1
                            val size = (48 * context.resources.displayMetrics.density).toInt()
                            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                                marginStart = (4 * context.resources.displayMetrics.density).toInt()
                                marginEnd = (4 * context.resources.displayMetrics.density).toInt()
                            }
                            setBackgroundResource(android.R.drawable.edit_text)
                            isCursorVisible = false
                        }

                        field.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                            override fun afterTextChanged(s: Editable?) {
                                if (ignoreTextChanges) return
                                val text = s?.toString() ?: ""
                                if (text.length == 1 && i < otpLength - 1) {
                                    fields[i + 1].requestFocus()
                                }
                                val fullOtp = fields.joinToString("") { it.text.toString() }
                                if (fullOtp.length == otpLength) {
                                    onVerified()
                                }
                            }
                        })

                        fields.add(field)
                        addView(field)
                    }

                    // Attach all fields to SecureKey OTP mode
                    fields.forEach { field ->
                        SecureKey.getInstance().attachTo(field, KeyboardMode.NUMERIC_OTP)
                    }

                    // Focus first field
                    fields.firstOrNull()?.requestFocus()
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Uses SecureKey NUMERIC_OTP keyboard with auto-advance",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { /* Resend logic */ }) {
            Text("Resend Code")
        }
    }
}

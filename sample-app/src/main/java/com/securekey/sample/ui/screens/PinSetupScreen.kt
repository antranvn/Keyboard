package com.securekey.sample.ui.screens

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
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
fun PinSetupScreen(onPinSet: () -> Unit) {
    var titleText by remember { mutableStateOf("Set PIN") }
    var subtitleText by remember { mutableStateOf("Enter a 4-digit PIN") }
    var errorMessage by remember { mutableStateOf("") }
    val pinLength = 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitleText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // PIN input — single AndroidView that handles both enter and confirm phases internally
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER

                    var firstPin = ""
                    var isConfirming = false
                    var ignoreTextChanges = false
                    val fields = mutableListOf<SecureEditText>()

                    fun clearFields() {
                        ignoreTextChanges = true
                        fields.forEach { it.setText("") }
                        ignoreTextChanges = false
                        fields.firstOrNull()?.requestFocus()
                    }

                    for (i in 0 until pinLength) {
                        val field = SecureEditText(context).apply {
                            inputType = InputType.TYPE_CLASS_NUMBER
                            transformationMethod = PasswordTransformationMethod.getInstance()
                            gravity = Gravity.CENTER
                            textSize = 24f
                            maxLines = 1
                            val size = (56 * context.resources.displayMetrics.density).toInt()
                            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                                marginStart = (8 * context.resources.displayMetrics.density).toInt()
                                marginEnd = (8 * context.resources.displayMetrics.density).toInt()
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
                                // Auto-advance to next field
                                if (text.length == 1 && i < pinLength - 1) {
                                    fields[i + 1].requestFocus()
                                }
                                val fullPin = fields.joinToString("") { it.text.toString() }
                                if (fullPin.length == pinLength) {
                                    if (isConfirming) {
                                        if (fullPin == firstPin) {
                                            onPinSet()
                                        } else {
                                            errorMessage = "PINs do not match. Try again."
                                            post { clearFields() }
                                        }
                                    } else {
                                        firstPin = fullPin
                                        isConfirming = true
                                        titleText = "Confirm PIN"
                                        subtitleText = "Re-enter your 4-digit PIN"
                                        errorMessage = ""
                                        post { clearFields() }
                                    }
                                }
                            }
                        })

                        fields.add(field)
                        addView(field)
                    }

                    fields.forEach { field ->
                        SecureKey.getInstance().attachTo(field, KeyboardMode.NUMERIC_PIN)
                    }

                    fields.firstOrNull()?.requestFocus()
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Uses SecureKey NUMERIC_PIN keyboard",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

package com.securekey.sample.ui.screens

import android.text.InputType
import android.widget.EditText
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
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SecureKey Bank",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Secure Login",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Username field using SecureEditText
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                SecureEditText(context).apply {
                    hint = "Username"
                    inputType = InputType.TYPE_CLASS_TEXT
                    setPadding(48, 40, 48, 40)
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    SecureKey.getInstance().attachTo(this, KeyboardMode.QWERTY_FULL)
                }
            },
            update = { editText ->
                if (editText.text.toString() != username) {
                    username = editText.text.toString()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password field using SecureEditText
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                SecureEditText(context).apply {
                    hint = "Password"
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    setPadding(48, 40, 48, 40)
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    SecureKey.getInstance().attachTo(this, KeyboardMode.QWERTY_FULL)
                }
            },
            update = { editText ->
                if (editText.text.toString() != password) {
                    password = editText.text.toString()
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLoginSuccess,
            modifier = Modifier.fillMaxWidth(),
            enabled = true
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This login uses SecureKey QWERTY keyboard",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

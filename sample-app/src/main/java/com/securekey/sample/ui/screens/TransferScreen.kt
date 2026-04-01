package com.securekey.sample.ui.screens

import android.text.InputType
import android.widget.LinearLayout
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
fun TransferScreen() {
    var amount by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    val quickAmounts = listOf("100", "500", "1,000", "5,000")
    var amountFieldRef by remember { mutableStateOf<SecureEditText?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Transfer Money",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Recipient field
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                SecureEditText(context).apply {
                    hint = "Recipient Account"
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
                recipient = editText.text.toString()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Amount field
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                SecureEditText(context).apply {
                    hint = "Amount"
                    inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                    setPadding(48, 40, 48, 40)
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    SecureKey.getInstance().attachTo(this, KeyboardMode.AMOUNT_PAD)
                    amountFieldRef = this
                }
            },
            update = { editText ->
                amount = editText.text.toString()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick amount chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickAmounts.forEach { qa ->
                FilterChip(
                    selected = amount == qa.replace(",", ""),
                    onClick = {
                        val value = qa.replace(",", "")
                        amountFieldRef?.setText(value)
                        amount = value
                    },
                    label = { Text("$$qa") }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { /* Transfer logic */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = amount.isNotBlank() && recipient.isNotBlank()
        ) {
            Text("Transfer")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Uses SecureKey AMOUNT_PAD with currency formatting",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

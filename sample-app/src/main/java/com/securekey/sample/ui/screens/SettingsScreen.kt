package com.securekey.sample.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var hapticEnabled by remember { mutableStateOf(true) }
    var keyPreviewEnabled by remember { mutableStateOf(true) }
    var shuffleKeys by remember { mutableStateOf(true) }
    var darkTheme by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(false) }
    var securityLevel by remember { mutableStateOf("STRICT") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Keyboard",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SettingsSwitch("Haptic Feedback", hapticEnabled) { hapticEnabled = it }
        SettingsSwitch("Key Preview Popup", keyPreviewEnabled) { keyPreviewEnabled = it }
        SettingsSwitch("Shuffle Numeric Keys", shuffleKeys) { shuffleKeys = it }
        SettingsSwitch("Sound Feedback", soundEnabled) { soundEnabled = it }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SettingsSwitch("Dark Theme", darkTheme) { darkTheme = it }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Security",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        listOf("STRICT", "MODERATE", "RELAXED").forEach { level ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = securityLevel == level,
                    onClick = { securityLevel = level }
                )
                Text(
                    text = level,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

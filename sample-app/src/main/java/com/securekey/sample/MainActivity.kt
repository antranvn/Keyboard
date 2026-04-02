package com.securekey.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.securekey.sample.navigation.NavGraph
import com.securekey.sample.navigation.Routes
import com.securekey.sample.ui.theme.SecureKeyBankTheme
import com.securekey.sdk.SecureKey

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SecureKey.getInstance().bind(this)
        enableEdgeToEdge()
        setContent {
            SecureKeyBankTheme {
                val navController = rememberNavController()
                val currentRoute by navController.currentBackStackEntryAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        val route = currentRoute?.destination?.route
                        if (route != null && route != Routes.LOGIN) {
                            @OptIn(ExperimentalMaterial3Api::class)
                            TopAppBar(
                                title = {
                                    Text(
                                        when (route) {
                                            Routes.OTP -> "Verify OTP"
                                            Routes.PIN_SETUP -> "Set PIN"
                                            Routes.TRANSFER -> "Transfer"
                                            Routes.SETTINGS -> "Settings"
                                            else -> "SecureKey Bank"
                                        }
                                    )
                                },
                                actions = {
                                    if (route != Routes.SETTINGS) {
                                        IconButton(onClick = {
                                            navController.navigate(Routes.SETTINGS)
                                        }) {
                                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                                        }
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavGraph(navController = navController)
                    }
                }
            }
        }
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

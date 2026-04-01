package com.securekey.sample.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

object Routes {
    const val LOGIN = "login"
    const val OTP = "otp"
    const val PIN_SETUP = "pin_setup"
    const val TRANSFER = "transfer"
    const val SETTINGS = "settings"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            com.securekey.sample.ui.screens.LoginScreen(
                onLoginSuccess = { navController.navigate(Routes.OTP) }
            )
        }
        composable(Routes.OTP) {
            com.securekey.sample.ui.screens.OtpScreen(
                onVerified = { navController.navigate(Routes.PIN_SETUP) }
            )
        }
        composable(Routes.PIN_SETUP) {
            com.securekey.sample.ui.screens.PinSetupScreen(
                onPinSet = { navController.navigate(Routes.TRANSFER) }
            )
        }
        composable(Routes.TRANSFER) {
            com.securekey.sample.ui.screens.TransferScreen()
        }
        composable(Routes.SETTINGS) {
            com.securekey.sample.ui.screens.SettingsScreen()
        }
    }
}

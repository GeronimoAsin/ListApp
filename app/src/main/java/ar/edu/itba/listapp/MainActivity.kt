package ar.edu.itba.listapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import ar.edu.itba.listapp.ui.layouts.AppDestination
import ar.edu.itba.listapp.ui.layouts.BaseLayout
import ar.edu.itba.listapp.ui.screens.*
import ar.edu.itba.listapp.data.network.NetworkModule
import ar.edu.itba.listapp.ui.theme.LightGreen
import ar.edu.itba.listapp.ui.theme.ListappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar NetworkModule con el contexto
        NetworkModule.initialize(applicationContext)

        setContent {
            ListappTheme(dynamicColor = false) {
                ListappApp()
            }
        }
    }
}

enum class AppScreen {
    LOGIN,
    MAIN_APP,
    REGISTER,
    FORGOT_PASSWORD,
    VERIFICATION,
    RESET_PASSWORD,
    CHANGE_PASSWORD
}

@Composable
fun ListappApp() {
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.LOGIN) }
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.LISTS) }
    var userEmail by rememberSaveable { mutableStateOf<String?>(null) }

    when (currentScreen) {
        AppScreen.LOGIN -> {
            Scaffold(
                containerColor = LightGreen
            ) { padding ->
                LoginScreen(
                    padding = padding,
                    onForgotPasswordClick = { currentScreen = AppScreen.FORGOT_PASSWORD },
                    onVerificationClick = { currentScreen = AppScreen.VERIFICATION },
                    onRegisterClick = { currentScreen = AppScreen.REGISTER },
                    onLoginSuccess = { currentScreen = AppScreen.MAIN_APP }
                )
            }
        }
        AppScreen.REGISTER -> {
            Scaffold(
                containerColor = LightGreen
            ) { padding ->
                RegisterScreen(
                    padding = padding,
                    onLoginClick = { currentScreen = AppScreen.LOGIN },
                    onRegisterSuccess = { email ->
                        userEmail = email
                        currentScreen = AppScreen.VERIFICATION
                    }
                )
            }
        }
        AppScreen.FORGOT_PASSWORD -> {
            Scaffold(
                containerColor = LightGreen
            ) { padding ->
                ForgotPasswordScreen(
                    padding = padding,
                    onBackToLogin = { currentScreen = AppScreen.LOGIN },
                    onCodeSent = { email ->
                        userEmail = email
                        currentScreen = AppScreen.RESET_PASSWORD
                    }
                )
            }
        }
        AppScreen.VERIFICATION -> {
            Scaffold(
                containerColor = LightGreen
            ) { padding ->
                VerificationScreen(
                    padding = padding,
                    onVerified = { currentScreen = AppScreen.LOGIN },
                    onBackClick = { currentScreen = AppScreen.LOGIN },
                    userEmail = userEmail
                )
            }
        }
        AppScreen.RESET_PASSWORD -> {
            Scaffold(
                containerColor = LightGreen
            ) { padding ->
                ResetPasswordScreen(
                    padding = padding,
                    onPasswordReset = { currentScreen = AppScreen.LOGIN },
                    onBackClick = { currentScreen = AppScreen.LOGIN }
                )
            }
        }
        AppScreen.CHANGE_PASSWORD -> {
            Scaffold(
                containerColor = LightGreen
            ) { padding ->
                ChangePasswordScreen(
                    padding = padding,
                    onPasswordChanged = { currentScreen = AppScreen.MAIN_APP },
                    onBackClick = { currentScreen = AppScreen.MAIN_APP }
                )
            }
        }
        AppScreen.MAIN_APP -> {
            BaseLayout(
                currentDestination = currentDestination,
                onDestinationChanged = { currentDestination = it }
            ) { innerPadding ->
                when (currentDestination) {
                    AppDestination.LISTS -> ListsScreen(innerPadding)
                    AppDestination.PRODUCTS -> ProductsScreen(innerPadding)
                    AppDestination.PANTRY -> PantryScreen(innerPadding)
                    AppDestination.PROFILE -> ProfileScreen(
                        padding = innerPadding,
                        onChangePassword = { currentScreen = AppScreen.CHANGE_PASSWORD },
                        onLogout = { currentScreen = AppScreen.LOGIN }
                    )
                }
            }
        }
    }
}
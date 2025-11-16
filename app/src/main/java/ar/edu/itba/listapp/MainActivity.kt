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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ar.edu.itba.listapp.data.network.NetworkModule
import ar.edu.itba.listapp.ui.layouts.AppDestination
import ar.edu.itba.listapp.ui.layouts.BaseLayout
import ar.edu.itba.listapp.ui.screens.*
import ar.edu.itba.listapp.ui.theme.LightGreen
import ar.edu.itba.listapp.ui.theme.ListappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize NetworkModule with the context
        NetworkModule.initialize(applicationContext)

        setContent {
            ListappTheme(dynamicColor = false) {
                ListappApp()
            }
        }
    }
}

@Composable
fun ListappApp() {
    val navController = rememberNavController()
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.LISTS) }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            Scaffold(containerColor = LightGreen) { padding ->
                LoginScreen(
                    padding = padding,
                    onForgotPasswordClick = { navController.navigate("forgot_password") },
                    onVerificationClick = { navController.navigate("verification") },
                    onRegisterClick = { navController.navigate("register") },
                    onLoginSuccess = { navController.navigate("main_app") }
                )
            }
        }
        composable("register") {
            Scaffold(containerColor = LightGreen) { padding ->
                RegisterScreen(
                    padding,
                    onLoginClick = { navController.navigate("login") },
                    onRegisterSuccess = { navController.navigate("verification") }
                )
            }
        }
        composable("forgot_password") {
            Scaffold(containerColor = LightGreen) { padding ->
                ForgotPasswordScreen(
                    padding = padding,
                    onBackToLogin = { navController.navigate("login") },
                    onCodeSent = { navController.navigate("reset_password") }
                )
            }
        }
        composable("verification") {
            Scaffold(containerColor = LightGreen) { padding ->
                VerificationScreen(
                    padding = padding,
                    onVerified = { navController.navigate("login") },
                    onBackClick = { navController.navigate("login") },
                    userEmail = null
                )
            }
        }
        composable("reset_password") {
            Scaffold(containerColor = LightGreen) { padding ->
                ResetPasswordScreen(
                    padding = padding,
                    onPasswordReset = { navController.navigate("login") },
                    onBackClick = { navController.navigate("login") }
                )
            }
        }
        composable("change_password") {
            Scaffold(containerColor = LightGreen) { padding ->
                ChangePasswordScreen(
                    padding = padding,
                    onPasswordChanged = { navController.navigate("main_app") },
                    onBackClick = { navController.navigate("main_app") }
                )
            }
        }
        composable("main_app") {
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
                        onChangePassword = { navController.navigate("change_password") },
                        onLogout = { navController.navigate("login") { popUpTo("main_app") { inclusive = true } } }
                    )
                }
            }
        }
    }
}

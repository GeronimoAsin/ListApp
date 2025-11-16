
package ar.edu.itba.listapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ar.edu.itba.listapp.data.network.NetworkModule
import ar.edu.itba.listapp.ui.layouts.AppHistory
import ar.edu.itba.listapp.ui.layouts.BaseLayout
import ar.edu.itba.listapp.ui.screens.*
import ar.edu.itba.listapp.ui.theme.LightGreen
import ar.edu.itba.listapp.ui.theme.ListappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AppHistory.history.clear()

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

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            Scaffold(containerColor = LightGreen) { padding ->
                LoginScreen(
                    padding = padding,
                    onForgotPasswordClick = { navController.navigate("forgot_password") },
                    onVerificationClick = { navController.navigate("verification") },
                    onRegisterClick = { navController.navigate("register") },
                    onLoginSuccess = { navController.navigate("main_app") { popUpTo("login") { inclusive = true } } }
                )
            }
        }
        composable("register") {
            Scaffold(containerColor = LightGreen) { padding ->
                RegisterScreen(
                    padding = padding,
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
            MainApp(navController = navController)
        }
    }
}

@Composable
fun MainApp(navController: androidx.navigation.NavController) {
    val mainNavController = rememberNavController()

    BaseLayout(navController = mainNavController) { innerPadding ->
        NavHost(navController = mainNavController, startDestination = "lists") {
            composable("lists") { ListsScreen(innerPadding) }
            composable("products") { ProductsScreen(innerPadding) }
            composable("pantry") { PantryScreen(innerPadding) }
            composable("profile") {
                ProfileScreen(
                    padding = innerPadding,
                    onChangePassword = { navController.navigate("change_password") },
                    onLogout = {
                        AppHistory.history.clear()
                        navController.navigate("login") { popUpTo("main_app") { inclusive = true } }
                    }
                )
            }
        }
    }
}

package ar.edu.itba.listapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import ar.edu.itba.listapp.ui.layouts.AppDestination
import ar.edu.itba.listapp.ui.layouts.BaseLayout
import ar.edu.itba.listapp.ui.screens.ListsScreen
import ar.edu.itba.listapp.ui.screens.PantryScreen
import ar.edu.itba.listapp.ui.screens.ProductsScreen
import ar.edu.itba.listapp.ui.screens.ProfileScreen
import ar.edu.itba.listapp.ui.theme.ListappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListappTheme(dynamicColor = false) {
                ListappApp()
            }
        }
    }
}

@Composable
fun ListappApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.LISTS) }

    BaseLayout(
        currentDestination = currentDestination,
        onDestinationChanged = { currentDestination = it }
    ) { innerPadding ->
        when (currentDestination) {
            AppDestination.LISTS -> ListsScreen(innerPadding)
            AppDestination.PRODUCTS -> ProductsScreen(innerPadding)
            AppDestination.PANTRY -> PantryScreen(innerPadding)
            AppDestination.PROFILE -> ProfileScreen(innerPadding)
        }
    }
}
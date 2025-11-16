
package ar.edu.itba.listapp.ui.layouts

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.ui.History
import ar.edu.itba.listapp.ui.composables.AdaptiveNavBar
import ar.edu.itba.listapp.ui.theme.LightGreen

object AppHistory {
    val history = History()
}

enum class AppDestination(
    @StringRes val label: Int,
    val icon: ImageVector,
    val route: String
) {
    LISTS(R.string.lists, Icons.Default.List, "lists"),
    PRODUCTS(R.string.products, Icons.Default.ShoppingCart, "products"),
    PANTRY(R.string.pantry, Icons.Default.ShoppingBasket, "pantry"),
    PROFILE(R.string.profile, Icons.Default.Person, "profile");

    fun shouldAddToHistory(): Boolean {
        return this != PROFILE
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseLayout(
    navController: NavController,
    modifier: Modifier = Modifier,
    content: @Composable (innerPadding: PaddingValues) -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentDestination = AppDestination.entries.find { it.route == currentRoute }

    if (currentDestination?.shouldAddToHistory() == true) {
        AppHistory.history.push(currentDestination.route)
    }

    HandleBackPress(navController)

    AdaptiveNavBar(
        navController = navController,
        modifier = modifier
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (currentDestination != null) {
                    TopAppBar(
                        title = { Text(text = stringResource(id = currentDestination.label), style = MaterialTheme.typography.headlineLarge) }
                    )
                }
            },
            containerColor = LightGreen,
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}

@Composable
private fun HandleBackPress(navController: NavController) {
    val history = remember { AppHistory.history }

    BackHandler(enabled = !history.isEmpty()) {
        history.pop()
        val previousRoute = history.peek()
        if (previousRoute != null) {
            navController.navigate(previousRoute) {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }
        }
    }
}

@Preview
@Composable
private fun BaseLayoutPreview() {
    val navController = rememberNavController()
    BaseLayout(
        navController = navController,
    ) { padding ->
        // ...content preview placeholder...
        Text(
            text = "Preview content",
            modifier = Modifier.padding(padding)
        )
    }
}

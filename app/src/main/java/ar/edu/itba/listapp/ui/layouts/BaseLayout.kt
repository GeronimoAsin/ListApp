package ar.edu.itba.listapp.ui.layouts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import ar.edu.itba.listapp.ui.composables.AdaptiveNavBar
import ar.edu.itba.listapp.ui.theme.LightGreen


//destinaciones de la app
enum class AppDestination(
    val label: String,
    val icon: ImageVector,
) {
    LISTS("Listas", Icons.Default.List),
    PRODUCTS("Productos", Icons.Default.ShoppingCart),
    PANTRY("Despensa", Icons.Default.ShoppingBasket),
    PROFILE("Perfil", Icons.Default.Person),
}


@Composable
fun BaseLayout(
    currentDestination: AppDestination,
    onDestinationChanged: (AppDestination) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (innerPadding: androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    AdaptiveNavBar(
        currentDestination = currentDestination,
        onDestinationChanged = onDestinationChanged,
        modifier = modifier
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = LightGreen,
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}

@Preview
@Composable
private fun BaseLayoutPreview() {
    BaseLayout(
        currentDestination = AppDestination.LISTS,
        onDestinationChanged = {},
    ) { padding ->
        // ...content preview placeholder...
        androidx.compose.material3.Text(
            text = "Preview content",
            modifier = Modifier.padding(padding)
        )
    }
}

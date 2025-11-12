package ar.edu.itba.listapp.ui.composables

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ar.edu.itba.listapp.ui.layouts.AppDestination
import ar.edu.itba.listapp.ui.theme.DeeperGreen


@Composable
fun AdaptiveNavBar(
    currentDestination: AppDestination,
    onDestinationChanged: (AppDestination) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestination.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = { onDestinationChanged(destination) }
                )
            }
        },
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = DeeperGreen,
            navigationBarContentColor = Color.White,
            navigationRailContainerColor = DeeperGreen,
            navigationRailContentColor = Color.White,
            navigationDrawerContainerColor = DeeperGreen,
            navigationDrawerContentColor = Color.White
        ),
        modifier = modifier
    ) {
        content()
    }
}


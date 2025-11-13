package ar.edu.itba.listapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.itba.listapp.ui.composables.CollapsibleList
import ar.edu.itba.listapp.ui.composables.SearchBar
import ar.edu.itba.listapp.ui.theme.ListappTheme

@Composable
fun ProductsScreen(innerPadding: PaddingValues) {
    var searchText by remember { mutableStateOf("") }
    var listTitle by remember { mutableStateOf("Frutas") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "Productos",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 36.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        SearchBar(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = "Buscar Productos",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        CollapsibleList(
            title = listTitle,
            items = listOf("🍎" to "Manzana", "🍌" to "Banana"),
            onAddItem = { },
            onTitleChanged = { newListTitle -> listTitle = newListTitle },
            onDeleteList = { },
            onEditItem = { },
            onDeleteItem = { }
        )
    }
}



@Preview(showBackground = true)
@Composable
fun ProductsScreenPreview() {
    ListappTheme {
        ProductsScreen(PaddingValues())
    }
}
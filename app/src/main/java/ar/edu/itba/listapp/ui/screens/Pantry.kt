package ar.edu.itba.listapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.itba.listapp.ui.composables.AddItemDialog
import ar.edu.itba.listapp.ui.composables.CollapsibleList
import ar.edu.itba.listapp.ui.composables.SearchBar

@Composable
fun PantryScreen(padding: PaddingValues) {
    var searchText by remember { mutableStateOf("") }
    val pantryItems = remember {
        mutableStateListOf(
            "🍎" to "Manzana",
            "🍌" to "Banana",
            "🥛" to "Leche"
        )
    }
    var listTitle by remember { mutableStateOf("Mi Despensa") }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AddItemDialog(
            onDismiss = { showDialog = false },
            onConfirm = { emoji, name ->
                pantryItems.add(emoji to name)
                showDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Despensa",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 36.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        SearchBar(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = "Buscar en Despensa",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        CollapsibleList(
            title = listTitle,
            items = pantryItems.filter { it.second.contains(searchText, ignoreCase = true) },
            onAddItem = { showDialog = true },
            onTitleChanged = { newListTitle -> listTitle = newListTitle },
            onDeleteList = {
                pantryItems.clear()
            },
            onEditItem = { item ->
                // TODO: Implementar la edición de un ítem.
            },
            onDeleteItem = { item ->
                pantryItems.remove(item)
            }
        )
    }
}

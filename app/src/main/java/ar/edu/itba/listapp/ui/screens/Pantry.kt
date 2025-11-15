package ar.edu.itba.listapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.ui.composables.AddProductForm
import ar.edu.itba.listapp.ui.composables.CollapsibleList
import ar.edu.itba.listapp.ui.composables.ModifyProductForm
import ar.edu.itba.listapp.ui.composables.NoItemsMessage
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
    val myPantryStr = stringResource(id = R.string.my_pantry)
    var listTitle by remember(myPantryStr) { mutableStateOf(myPantryStr) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showModifyDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    if (showAddDialog) {
        AddProductForm(
            onDismiss = { showAddDialog = false },
            onConfirm = { emoji, name ->
                pantryItems.add(emoji to name)
                showAddDialog = false
            }
        )
    }

    showModifyDialog?.let {
        ModifyProductForm(
            item = it,
            onDismiss = { showModifyDialog = null },
            onConfirm = { emoji, name ->
                val index = pantryItems.indexOf(it)
                if (index != -1) {
                    pantryItems[index] = emoji to name
                }
                showModifyDialog = null
            }
        )
    }

    val filteredItems = pantryItems.filter { it.second.contains(searchText, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.pantry),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 36.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        SearchBar(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = stringResource(id = R.string.search_in_pantry),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredItems.isEmpty()) {
            NoItemsMessage()
        } else {
            CollapsibleList(
                title = listTitle,
                items = filteredItems,
                onAddItem = { showAddDialog = true },
                onTitleChanged = { newListTitle -> listTitle = newListTitle },
                onDeleteList = {
                    pantryItems.clear()
                },
                onEditItem = { item ->
                    showModifyDialog = item
                },
                onDeleteItem = { item ->
                    pantryItems.remove(item)
                }
            )
        }
    }
}

package ar.edu.itba.listapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.ui.composables.CollapsibleList
import ar.edu.itba.listapp.ui.composables.NewCategoryForm
import ar.edu.itba.listapp.ui.composables.NoItemsMessage
import ar.edu.itba.listapp.ui.composables.SearchBar
import ar.edu.itba.listapp.ui.theme.ListappTheme
data class ProductCategory(val id: Long, val title: String, val items: List<Pair<String, String>>)

@Composable
fun ProductsScreen(scaffoldPadding: PaddingValues) {
    var searchText by remember { mutableStateOf("") }
    val categories = remember {
        mutableStateListOf(
            ProductCategory(1L, "Frutas", listOf("🍎" to "Manzana", "🍌" to "Banana"))
        )
    }
    var nextId by remember { mutableStateOf(2L) }
    var showDialog by remember { mutableStateOf(false) }

    val filteredCategories = if (searchText.isBlank()) {
        categories
    } else {
        categories.mapNotNull { category ->
            val filteredItems = category.items.filter { it.second.contains(searchText, ignoreCase = true) }
            if (category.title.contains(searchText, ignoreCase = true)) {
                category
            } else if (filteredItems.isNotEmpty()) {
                category.copy(items = filteredItems)
            } else {
                null
            }
        }
    }

    Scaffold(
        modifier = Modifier.padding(scaffoldPadding),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_category_icon_description)) },
                text = { Text(stringResource(R.string.new_category), fontWeight = FontWeight.Bold) },
                containerColor = Color.White,
                contentColor = Color.Black,
                shape = RoundedCornerShape(50)
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = stringResource(R.string.products),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 36.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                SearchBar(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = stringResource(R.string.search_products),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (filteredCategories.isEmpty()) {
                item {
                    NoItemsMessage()
                }
            } else {
                items(filteredCategories, key = { it.id }) { category ->
                    CollapsibleList(
                        title = category.title,
                        items = category.items,
                        onAddItem = { },
                        onTitleChanged = { newTitle ->
                            val index = categories.indexOfFirst { it.id == category.id }
                            if (index != -1) {
                                categories[index] = categories[index].copy(title = newTitle)
                            }
                        },
                        onDeleteList = { },
                        onEditItem = { },
                        onDeleteItem = { }
                    )
                }
            }
        }

        if (showDialog) {
            NewCategoryForm(
                onDismiss = { showDialog = false },
                onConfirm = { categoryName ->
                    if (categoryName.isNotBlank()) {
                        categories.add(ProductCategory(nextId++, categoryName, emptyList()))
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductsScreenPreview() {
    ListappTheme {
        ProductsScreen(PaddingValues())
    }
}

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
import ar.edu.itba.listapp.ui.composables.AddProductForm
import ar.edu.itba.listapp.ui.composables.CollapsibleList
import ar.edu.itba.listapp.ui.composables.ModifyProductForm
import ar.edu.itba.listapp.ui.composables.NewCategoryForm
import ar.edu.itba.listapp.ui.composables.NoItemsMessage
import ar.edu.itba.listapp.ui.composables.SearchBar
import ar.edu.itba.listapp.ui.theme.ListappTheme
data class ProductCategory(var id: Long, var title: String, var items: List<Pair<String, String>>)

@Composable
fun ProductsScreen(scaffoldPadding: PaddingValues) {
    var searchText by remember { mutableStateOf("") }
    val categories = remember {
        mutableStateListOf(
            ProductCategory(1L, "Frutas", listOf("🍎" to "Manzana", "🍌" to "Banana"))
        )
    }
    var nextId by remember { mutableStateOf(2L) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf<ProductCategory?>(null) }
    var showModifyDialog by remember { mutableStateOf<Pair<ProductCategory, Pair<String, String>>?>(null) }

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

    if (showCategoryDialog) {
        NewCategoryForm(
            onDismiss = { showCategoryDialog = false },
            onConfirm = { categoryName ->
                if (categoryName.isNotBlank()) {
                    categories.add(ProductCategory(nextId++, categoryName, emptyList()))
                }
                showCategoryDialog = false
            }
        )
    }

    showAddDialog?.let { category ->
        AddProductForm(
            onDismiss = { showAddDialog = null },
            onConfirm = { emoji, name ->
                val index = categories.indexOf(category)
                if (index != -1) {
                    val newItems = categories[index].items.toMutableList().apply { add(emoji to name) }
                    categories[index] = categories[index].copy(items = newItems)
                }
                showAddDialog = null
            }
        )
    }

    showModifyDialog?.let {
        ModifyProductForm(
            item = it.second,
            onDismiss = { showModifyDialog = null },
            onConfirm = { emoji, name ->
                val categoryIndex = categories.indexOf(it.first)
                if (categoryIndex != -1) {
                    val itemIndex = categories[categoryIndex].items.indexOf(it.second)
                    if (itemIndex != -1) {
                        val newItems = categories[categoryIndex].items.toMutableList()
                        newItems[itemIndex] = emoji to name
                        categories[categoryIndex] = categories[categoryIndex].copy(items = newItems)
                    }
                }
                showModifyDialog = null
            }
        )
    }

    Scaffold(
        modifier = Modifier.padding(scaffoldPadding),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCategoryDialog = true },
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
                        onAddItem = { showAddDialog = category },
                        onTitleChanged = { newTitle ->
                            val index = categories.indexOfFirst { it.id == category.id }
                            if (index != -1) {
                                categories[index] = categories[index].copy(title = newTitle)
                            }
                        },
                        onDeleteList = {
                            categories.remove(category)
                        },
                        onEditItem = { item ->
                            showModifyDialog = category to item
                        },
                        onDeleteItem = { item ->
                            val index = categories.indexOf(category)
                            if (index != -1) {
                                val newItems = categories[index].items.toMutableList().apply { remove(item) }
                                categories[index] = categories[index].copy(items = newItems)
                            }
                        }
                    )
                }
            }
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

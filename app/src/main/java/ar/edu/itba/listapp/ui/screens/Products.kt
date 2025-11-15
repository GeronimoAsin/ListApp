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
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch
import ar.edu.itba.listapp.data.network.*

private data class ProductUI(var id: Long, var emoji: String, var name: String)
private data class CategoryUI(var id: Long, var title: String, val items: MutableList<ProductUI>)

@Composable
fun ProductsScreen(scaffoldPadding: PaddingValues) {
    val context = LocalContext.current
    val repository = remember { ProductRepository(context, sessionManager = SessionManager(context)) }
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(true) }
    var searchText by remember { mutableStateOf("") }

    // Categories and dialog states
    val categories = remember { mutableStateListOf<CategoryUI>() }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showAddDialogForCategory by remember { mutableStateOf<CategoryUI?>(null) }
    var showModifyDialogProduct by remember { mutableStateOf<ProductUI?>(null) }
    var showModifyDialogCategory by remember { mutableStateOf<CategoryUI?>(null) }

    // Reload categories and products
    fun reload() {
        val snackbarHostState = snackbarHostState // capture
        scope.launch {
            isLoading = true
            categories.clear()
            when (val catRes = repository.getCategories(page = 1, perPage = 100, sortBy = "name", order = "ASC")) {
                is GetCategoriesResult.Success -> {
                    // para cada categoria, obtenemos sus productos
                    for (cat in catRes.categories) {
                        val catUi = CategoryUI(cat.id, cat.name, mutableStateListOf())
                        when (val prodRes = repository.getProducts(categoryId = cat.id, page = 1, perPage = 500, sortBy = "name", order = "ASC")) {
                            is GetProductsResult.Success -> {
                                catUi.items.addAll(prodRes.products.map { p ->
                                    val emoji = p.metadata["emoji"] ?: "\uD83E\uDDC2" //fallback emoji
                                    ProductUI(p.id, emoji, p.name)
                                })
                            }
                            is GetProductsResult.Error -> {
                                // Show products load error but keep category visible
                                snackbarHostState.showSnackbar(prodRes.message)
                            }
                        }
                        categories.add(catUi)
                    }
                }
                is GetCategoriesResult.Error -> snackbarHostState.showSnackbar(catRes.message)
            }
            isLoading = false
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        reload()
    }

    // Filtering for UI - reactive to changes in categories content
    val filteredCategories by remember(searchText) {
        derivedStateOf {
            val base = categories.toList()
            if (searchText.isBlank()) base else {
                base.mapNotNull { category ->
                    val filteredItems = category.items.filter { it.name.contains(searchText, ignoreCase = true) }
                    when {
                        category.title.contains(searchText, ignoreCase = true) -> category
                        filteredItems.isNotEmpty() -> category.copy(items = filteredItems.toMutableList())
                        else -> null
                    }
                }
            }
        }
    }

    // Dialogs
    if (showCategoryDialog) {
        NewCategoryForm(
            onDismiss = { showCategoryDialog = false },
            onConfirm = { categoryName ->
                if (categoryName.isBlank()) {
                    showCategoryDialog = false
                    return@NewCategoryForm
                }
                scope.launch {
                    when (val res = repository.createCategory(categoryName)) {
                        is CreateCategoryResult.Success -> {
                            // Optimistic add for instant feedback (use stateful list for items)
                            categories.add(CategoryUI(res.category.id, res.category.name, mutableStateListOf()))
                            // And reload from server to stay in sync
                            reload()
                        }
                        is CreateCategoryResult.Error -> snackbarHostState.showSnackbar(res.message)
                    }
                    showCategoryDialog = false
                }
            }
        )
    }

    // When adding a product, mutate the backing category by id
    showAddDialogForCategory?.let { category ->
        AddProductForm(
            onDismiss = { showAddDialogForCategory = null },
            onConfirm = { emoji, name ->
                if (name.isBlank()) {
                    showAddDialogForCategory = null
                    return@AddProductForm
                }
                scope.launch {
                    when (val res = repository.createProduct(name = name, categoryId = category.id, metadata = mapOf("emoji" to emoji))) {
                        is CreateProductResult.Success -> {
                            val baseCategory = categories.find { it.id == category.id } ?: category
                            baseCategory.items.add(ProductUI(res.product.id, emoji, name))
                        }
                        is CreateProductResult.Error -> snackbarHostState.showSnackbar(res.message)
                    }
                    showAddDialogForCategory = null
                }
            }
        )
    }

    showModifyDialogProduct?.let { prod ->
        ModifyProductForm(
            item = prod.emoji to prod.name,
            onDismiss = {
                showModifyDialogProduct = null
                showModifyDialogCategory = null
            },
            onConfirm = { emoji, name ->
                val category = showModifyDialogCategory
                if (category == null) {
                    showModifyDialogProduct = null
                    return@ModifyProductForm
                }
                scope.launch {
                    when (val res = repository.updateProduct(id = prod.id, name = name, categoryId = category.id, metadata = mapOf("emoji" to emoji))) {
                        is UpdateProductResult.Success -> {
                            // Replace element to trigger recomposition
                            val idx = category.items.indexOfFirst { it.id == prod.id }
                            if (idx != -1) {
                                category.items[idx] = prod.copy(emoji = emoji, name = name)
                            }
                        }
                        is UpdateProductResult.Error -> snackbarHostState.showSnackbar(res.message)
                    }
                    showModifyDialogProduct = null
                    showModifyDialogCategory = null
                }
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
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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

                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (!isLoading && filteredCategories.isEmpty()) {
                item { NoItemsMessage() }
            } else {
                items(filteredCategories, key = { it.id }) { category ->
                    CollapsibleList(
                        title = category.title,
                        items = category.items.map { it.emoji to it.name },
                        onAddItem = {
                            // Always use the backing category from state list
                            showAddDialogForCategory = categories.find { it.id == category.id } ?: category
                        },
                        onTitleChanged = { newTitle ->
                            val idx = categories.indexOfFirst { it.id == category.id }
                            if (idx != -1) {
                                val old = categories[idx]
                                categories[idx] = old.copy(title = newTitle)
                                scope.launch {
                                    when (val res = repository.updateCategory(id = category.id, name = newTitle)) {
                                        is UpdateCategoryResult.Success -> Unit
                                        is UpdateCategoryResult.Error -> {
                                            categories[idx] = old
                                            snackbarHostState.showSnackbar(res.message)
                                        }
                                    }
                                }
                            }
                        },
                        onDeleteList = {
                            scope.launch {
                                when (val res = repository.deleteCategory(category.id)) {
                                    is DeleteCategoryResult.Success -> {
                                        categories.removeIf { it.id == category.id }
                                    }
                                    is DeleteCategoryResult.Error -> snackbarHostState.showSnackbar(res.message)
                                }
                            }
                        },
                        onEditItem = { item ->
                            val baseCategory = categories.find { it.id == category.id } ?: category
                            val prod = baseCategory.items.find { it.emoji == item.first && it.name == item.second }
                            if (prod != null) {
                                showModifyDialogProduct = prod
                                showModifyDialogCategory = baseCategory
                            }
                        },
                        onDeleteItem = { item ->
                            val baseCategory = categories.find { it.id == category.id } ?: category
                            val prod = baseCategory.items.find { it.emoji == item.first && it.name == item.second }
                            if (prod != null) {
                                scope.launch {
                                    when (val res = repository.deleteProduct(prod.id)) {
                                        is DeleteProductResult.Success -> {
                                            baseCategory.items.remove(prod)
                                        }
                                        is DeleteProductResult.Error -> snackbarHostState.showSnackbar(res.message)
                                    }
                                }
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

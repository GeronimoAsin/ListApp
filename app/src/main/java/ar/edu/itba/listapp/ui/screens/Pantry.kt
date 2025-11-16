package ar.edu.itba.listapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.data.model.Product
import ar.edu.itba.listapp.data.network.*
import ar.edu.itba.listapp.ui.composables.AddToPantryDialog
import ar.edu.itba.listapp.ui.composables.CollapsibleList
import ar.edu.itba.listapp.ui.composables.ModifyProductForm
import ar.edu.itba.listapp.ui.composables.NoItemsMessage
import ar.edu.itba.listapp.ui.composables.SearchBar
import ar.edu.itba.listapp.ui.composables.NewPantryForm
import kotlinx.coroutines.launch

private data class PantryItemUI(
    val id: Long,
    val productId: Long,
    val emoji: String,
    val name: String,
    val quantity: Double,
    val unit: String?
)

private data class PantryUI(
    val id: Long,
    val name: String,
    val items: List<PantryItemUI>
)

@Composable
fun PantryScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val pantryRepository = remember { PantryRepository(context, sessionManager = SessionManager(context)) }
    val productRepository = remember { ProductRepository(context, sessionManager = SessionManager(context)) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(true) }
    var searchText by remember { mutableStateOf("") }

    // State for multiple pantries
    val pantries = remember { mutableStateListOf<PantryUI>() }

    // Available products for adding to pantry
    val availableProducts = remember { mutableStateListOf<Product>() }

    // Track which pantry is being added to
    var selectedPantryIdForAdd by remember { mutableStateOf<Long?>(null) }

    // Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var showModifyDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showCreatePantryDialog by remember { mutableStateOf(false) }

    // Function to load pantry items
    suspend fun loadPantryItems(pantryId: Long): List<PantryItemUI> {
        return when (val itemsResult = pantryRepository.getPantryItems(pantryId, page = 1, perPage = 500)) {
            is GetPantryItemsResult.Success -> {
                itemsResult.items.map { item ->
                    PantryItemUI(
                        id = item.id,
                        productId = item.product.id,
                        emoji = item.product.metadata["emoji"] ?: "📦",
                        name = "${item.product.name}${item.unit?.let { " ($it)" } ?: ""} - ${item.quantity}",
                        quantity = item.quantity,
                        unit = item.unit
                    )
                }
            }
            is GetPantryItemsResult.Error -> {
                snackbarHostState.showSnackbar(itemsResult.message)
                emptyList()
            }
        }
    }

    // Load all pantries
    fun loadAllPantries() {
        scope.launch {
            isLoading = true

            // First, load available products
            when (val productsResult = productRepository.getProducts(page = 1, perPage = 500)) {
                is GetProductsResult.Success -> {
                    availableProducts.clear()
                    availableProducts.addAll(productsResult.products)
                }
                is GetProductsResult.Error -> {
                    snackbarHostState.showSnackbar(productsResult.message)
                }
            }

            // Then load all pantries
            when (val pantriesResult = pantryRepository.getPantries(owner = true, page = 1, perPage = 100)) {
                is GetPantriesResult.Success -> {
                    pantries.clear()

                    // Load each pantry with its items
                    for (pantry in pantriesResult.pantries) {
                        val items = loadPantryItems(pantry.id)
                        pantries.add(
                            PantryUI(
                                id = pantry.id,
                                name = pantry.name,
                                items = items
                            )
                        )
                    }
                }
                is GetPantriesResult.Error -> {
                    snackbarHostState.showSnackbar(pantriesResult.message)
                }
            }

            isLoading = false
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        loadAllPantries()
    }

    // Dialogs
    if (showCreatePantryDialog) {
        NewPantryForm(
            onDismiss = { showCreatePantryDialog = false },
            onConfirm = { name ->
                val trimmedName = name.trim()
                if (trimmedName.isBlank()) {
                    showCreatePantryDialog = false
                    return@NewPantryForm
                }
                scope.launch {
                    when (val result = pantryRepository.createPantry(trimmedName)) {
                        is CreatePantryResult.Success -> {
                            // Reload all pantries to show the new one
                            loadAllPantries()
                        }
                        is CreatePantryResult.Error -> snackbarHostState.showSnackbar(result.message)
                    }
                    showCreatePantryDialog = false
                }
            }
        )
    }

    if (showAddDialog && selectedPantryIdForAdd != null) {
        AddToPantryDialog(
            products = availableProducts,
            onDismiss = {
                showAddDialog = false
                selectedPantryIdForAdd = null
            },
            onConfirm = { product, quantity, unit ->
                scope.launch {
                    when (val result = pantryRepository.addPantryItem(
                        pantryId = selectedPantryIdForAdd!!,
                        productId = product.id,
                        quantity = quantity,
                        unit = unit
                    )) {
                        is AddPantryItemResult.Success -> {
                            // Reload all pantries to show the new item
                            loadAllPantries()
                        }
                        is AddPantryItemResult.Error -> {
                            snackbarHostState.showSnackbar(result.message)
                        }
                    }
                    showAddDialog = false
                    selectedPantryIdForAdd = null
                }
            }
        )
    }

    showModifyDialog?.let {
        ModifyProductForm(
            item = it,
            onDismiss = { showModifyDialog = null },
            onConfirm = { emoji, name ->
                // For now, we'll keep the modify dialog as is
                // In a full implementation, you'd update the pantry item
                showModifyDialog = null
            }
        )
    }


    Scaffold(
        modifier = Modifier.padding(padding),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreatePantryDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_pantry_icon_description)) },
                text = { Text(text = stringResource(id = R.string.new_pantry)) },
                containerColor = Color.White,
                contentColor = Color.Black,
                shape = RoundedCornerShape(50)
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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

            if (isLoading) {
                // Show loading state
                Text("Loading...", modifier = Modifier.padding(16.dp))
            } else if (pantries.isEmpty()) {
                // Only show "no items" when there are no pantries at all
                NoItemsMessage()
            } else {
                // Show all pantries, one below the other
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    pantries.forEach { pantry ->
                        item(key = pantry.id) {
                            val filteredItems = pantry.items
                                .filter { it.name.contains(searchText, ignoreCase = true) }
                                .map { it.emoji to it.name }

                            CollapsibleList(
                                title = pantry.name,
                                items = filteredItems,
                                onAddItem = {
                                    selectedPantryIdForAdd = pantry.id
                                    showAddDialog = true
                                },
                                onTitleChanged = { newTitle ->
                                    scope.launch {
                                        when (val result = pantryRepository.updatePantry(pantry.id, newTitle)) {
                                            is UpdatePantryResult.Success -> {
                                                // Reload all pantries to reflect the change
                                                loadAllPantries()
                                            }
                                            is UpdatePantryResult.Error -> {
                                                snackbarHostState.showSnackbar(result.message)
                                            }
                                        }
                                    }
                                },
                                onDeleteList = {
                                    scope.launch {
                                        when (val result = pantryRepository.deletePantry(pantry.id)) {
                                            is DeletePantryResult.Success -> {
                                                // Reload all pantries after deletion
                                                loadAllPantries()
                                            }
                                            is DeletePantryResult.Error -> {
                                                snackbarHostState.showSnackbar(result.message)
                                            }
                                        }
                                    }
                                },
                                onEditItem = { item ->
                                    showModifyDialog = item
                                },
                                onDeleteItem = { item ->
                                    // Find the pantry item by name to get its ID
                                    val itemToDelete = pantry.items.find { "${it.emoji} ${it.name}" == "${item.first} ${item.second}" }
                                    itemToDelete?.let { pantryItem ->
                                        scope.launch {
                                            when (val result = pantryRepository.deletePantryItem(pantry.id, pantryItem.id)) {
                                                is DeletePantryItemResult.Success -> {
                                                    // Reload all pantries after item deletion
                                                    loadAllPantries()
                                                }
                                                is DeletePantryItemResult.Error -> {
                                                    snackbarHostState.showSnackbar(result.message)
                                                }
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
    }
}

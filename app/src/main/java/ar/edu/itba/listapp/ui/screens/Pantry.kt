package ar.edu.itba.listapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.data.model.Product
import ar.edu.itba.listapp.data.network.*
import ar.edu.itba.listapp.ui.composables.AddToPantryDialog
import ar.edu.itba.listapp.ui.composables.EditPantryItemDialog
import ar.edu.itba.listapp.ui.composables.SearchBar
import ar.edu.itba.listapp.ui.composables.NewPantryForm
import ar.edu.itba.listapp.ui.composables.CollapsibleList
import ar.edu.itba.listapp.ui.composables.NoItemsMessage
import ar.edu.itba.listapp.ui.utils.isTablet
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
    val items: List<PantryItemUI>,
    val owner: ar.edu.itba.listapp.data.model.Owner? = null,
    val sharedWith: List<ar.edu.itba.listapp.data.model.Owner> = emptyList()
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
    val myPantries = remember { mutableStateListOf<PantryUI>() }
    val sharedPantries = remember { mutableStateListOf<PantryUI>() }

    // Available products for adding to pantry
    val availableProducts = remember { mutableStateListOf<Product>() }

    // Track which pantry is being added to
    var selectedPantryIdForAdd by remember { mutableStateOf<Long?>(null) }

    // Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var showModifyDialog by remember { mutableStateOf<PantryItemUI?>(null) }
    var showCreatePantryDialog by remember { mutableStateOf(false) }

    // Share sheet state
    var showShareSheet by remember { mutableStateOf(false) }
    var selectedPantryForShare by remember { mutableStateOf<PantryUI?>(null) }
    var emailToShare by remember { mutableStateOf("") }
    var shareOperationCompleted by remember { mutableStateOf(0) } // Counter to trigger recomposition

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

            // Load my pantries (owner=true)
            when (val myPantriesResult = pantryRepository.getPantries(owner = true, page = 1, perPage = 100)) {
                is GetPantriesResult.Success -> {
                    myPantries.clear()
                    for (pantry in myPantriesResult.pantries) {
                        val items = loadPantryItems(pantry.id)
                        myPantries.add(
                            PantryUI(
                                id = pantry.id,
                                name = pantry.name,
                                items = items,
                                owner = pantry.owner,
                                sharedWith = pantry.sharedWith
                            )
                        )
                    }
                }
                is GetPantriesResult.Error -> {
                    snackbarHostState.showSnackbar(myPantriesResult.message)
                }
            }

            // Load shared pantries (owner=false)
            when (val sharedPantriesResult = pantryRepository.getPantries(owner = false, page = 1, perPage = 100)) {
                is GetPantriesResult.Success -> {
                    sharedPantries.clear()
                    for (pantry in sharedPantriesResult.pantries) {
                        val items = loadPantryItems(pantry.id)
                        sharedPantries.add(
                            PantryUI(
                                id = pantry.id,
                                name = pantry.name,
                                items = items,
                                owner = pantry.owner,
                                sharedWith = pantry.sharedWith
                            )
                        )
                    }
                }
                is GetPantriesResult.Error -> {
                    snackbarHostState.showSnackbar(sharedPantriesResult.message)
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

    showModifyDialog?.let { item ->
        EditPantryItemDialog(
            emoji = item.emoji,
            name = item.name,
            currentQuantity = item.quantity,
            currentUnit = item.unit,
            onDismiss = { showModifyDialog = null },
            onConfirm = { quantity, unit ->
                scope.launch {
                    // Find which pantry this item belongs to
                    val pantryId = myPantries.find { pantry ->
                        pantry.items.any { it.id == item.id }
                    }?.id ?: sharedPantries.find { pantry ->
                        pantry.items.any { it.id == item.id }
                    }?.id

                    if (pantryId != null) {
                        when (val result = pantryRepository.updatePantryItem(
                            pantryId = pantryId,
                            itemId = item.id,
                            quantity = quantity,
                            unit = unit
                        )) {
                            is UpdatePantryItemResult.Success -> {
                                loadAllPantries()
                                showModifyDialog = null
                            }
                            is UpdatePantryItemResult.Error -> {
                                snackbarHostState.showSnackbar(result.message)
                                showModifyDialog = null
                            }
                        }
                    }
                }
            }
        )
    }

    // Share Bottom Sheet
    if (showShareSheet && selectedPantryForShare != null) {
        SharePantryBottomSheet(
            pantry = selectedPantryForShare!!,
            emailToShare = emailToShare,
            shareOperationCompleted = shareOperationCompleted,
            onEmailChange = { emailToShare = it },
            onDismiss = {
                showShareSheet = false
                selectedPantryForShare = null
                emailToShare = ""
            },
            onShare = { email: String, onError: (String?) -> Unit ->
                scope.launch {
                    val pantryId = selectedPantryForShare!!.id
                    when (val result = pantryRepository.sharePantry(pantryId, email)) {
                        is SharePantryResult.Success -> {
                            snackbarHostState.showSnackbar("Pantry shared with ${result.user.email}")
                            when (val pantryResult = pantryRepository.getPantries(owner = true, page = 1, perPage = 100)) {
                                is GetPantriesResult.Success -> {
                                    val updatedPantry = pantryResult.pantries.find { it.id == pantryId }
                                    if (updatedPantry != null) {
                                        val items = loadPantryItems(updatedPantry.id)
                                        selectedPantryForShare = PantryUI(
                                            id = updatedPantry.id,
                                            name = updatedPantry.name,
                                            items = items,
                                            owner = updatedPantry.owner,
                                            sharedWith = updatedPantry.sharedWith
                                        )
                                    }
                                }
                                is GetPantriesResult.Error -> {
                                    snackbarHostState.showSnackbar(pantryResult.message)
                                }
                            }
                            emailToShare = ""
                            loadAllPantries()
                            onError(null) // No error
                        }
                        is SharePantryResult.Error -> {
                            onError(result.message)
                        }
                    }
                    shareOperationCompleted++
                }
            },
            onRemoveUser = { userId: Long ->
                scope.launch {
                    val pantryId = selectedPantryForShare!!.id
                    when (val result = pantryRepository.unsharePantry(pantryId, userId)) {
                        is UnsharePantryResult.Success -> {
                            when (val pantryResult = pantryRepository.getPantries(owner = true, page = 1, perPage = 100)) {
                                is GetPantriesResult.Success -> {
                                    val updatedPantry = pantryResult.pantries.find { it.id == pantryId }
                                    if (updatedPantry != null) {
                                        val items = loadPantryItems(updatedPantry.id)
                                        selectedPantryForShare = PantryUI(
                                            id = updatedPantry.id,
                                            name = updatedPantry.name,
                                            items = items,
                                            owner = updatedPantry.owner,
                                            sharedWith = updatedPantry.sharedWith
                                        )
                                    }
                                }
                                is GetPantriesResult.Error -> {
                                    snackbarHostState.showSnackbar(pantryResult.message)
                                }
                            }
                            loadAllPantries()
                        }
                        is UnsharePantryResult.Error -> {
                            snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
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
            SearchBar(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = stringResource(id = R.string.search_in_pantry),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!isLoading && myPantries.isEmpty() && sharedPantries.isEmpty()) {
                NoItemsMessage()
            } else if (!isLoading) {
                if (isTablet()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (myPantries.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = "My Pantries",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(myPantries, key = { "my_${it.id}" }) { pantry ->
                                RenderPantryItem(
                                    pantry = pantry,
                                    searchText = searchText,
                                    canEdit = true, // Owner can edit
                                    canShare = true, // Owner can share
                                    onAddItem = {
                                        selectedPantryIdForAdd = pantry.id
                                        showAddDialog = true
                                    },
                                    onTitleChanged = { newTitle ->
                                        scope.launch {
                                            when (val result = pantryRepository.updatePantry(pantry.id, newTitle)) {
                                                is UpdatePantryResult.Success -> loadAllPantries()
                                                is UpdatePantryResult.Error -> snackbarHostState.showSnackbar(
                                                    result.message
                                                )
                                            }
                                        }
                                    },
                                    onDeleteList = {
                                        scope.launch {
                                            when (val result = pantryRepository.deletePantry(pantry.id)) {
                                                is DeletePantryResult.Success -> loadAllPantries()
                                                is DeletePantryResult.Error -> snackbarHostState.showSnackbar(
                                                    result.message
                                                )
                                            }
                                        }
                                    },
                                    onEditItem = { item -> showModifyDialog = item },
                                    onDeleteItem = { item ->
                                        val itemToDelete = pantry.items.find { "${it.emoji} ${it.name}" == "${item.first} ${item.second}" }
                                        itemToDelete?.let { pantryItem ->
                                            scope.launch {
                                                when (val result = pantryRepository.deletePantryItem(pantry.id, pantryItem.id)) {
                                                    is DeletePantryItemResult.Success -> loadAllPantries()
                                                    is DeletePantryItemResult.Error -> snackbarHostState.showSnackbar(
                                                        result.message
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onShareList = {
                                        selectedPantryForShare = pantry
                                        showShareSheet = true
                                    }
                                )
                            }
                        }

                        if (sharedPantries.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = "Shared with Me",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            items(sharedPantries, key = { "shared_${it.id}" }) { pantry ->
                                RenderPantryItem(
                                    pantry = pantry,
                                    searchText = searchText,
                                    canEdit = false, // Not owner, can't edit title or delete
                                    canShare = false, // Not owner, can't share
                                    onAddItem = {
                                        selectedPantryIdForAdd = pantry.id
                                        showAddDialog = true
                                    },
                                    onTitleChanged = { }, // Not editable
                                    onDeleteList = { }, // Not deletable
                                    onEditItem = { item -> showModifyDialog = item },
                                    onDeleteItem = { item ->
                                        val itemToDelete = pantry.items.find { "${it.emoji} ${it.name}" == "${item.first} ${item.second}" }
                                        itemToDelete?.let { pantryItem ->
                                            scope.launch {
                                                when (val result = pantryRepository.deletePantryItem(pantry.id, pantryItem.id)) {
                                                    is DeletePantryItemResult.Success -> loadAllPantries()
                                                    is DeletePantryItemResult.Error -> snackbarHostState.showSnackbar(
                                                        result.message
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onShareList = null // Can't share
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (myPantries.isNotEmpty()) {
                            item {
                                Text(
                                    text = "My Pantries",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(myPantries, key = { "my_${it.id}" }) { pantry ->
                                RenderPantryItem(
                                    pantry = pantry,
                                    searchText = searchText,
                                    canEdit = true, // Owner can edit
                                    canShare = true, // Owner can share
                                    onAddItem = {
                                        selectedPantryIdForAdd = pantry.id
                                        showAddDialog = true
                                    },
                                    onTitleChanged = { newTitle ->
                                        scope.launch {
                                            when (val result = pantryRepository.updatePantry(pantry.id, newTitle)) {
                                                is UpdatePantryResult.Success -> loadAllPantries()
                                                is UpdatePantryResult.Error -> snackbarHostState.showSnackbar(
                                                    result.message
                                                )
                                            }
                                        }
                                    },
                                    onDeleteList = {
                                        scope.launch {
                                            when (val result = pantryRepository.deletePantry(pantry.id)) {
                                                is DeletePantryResult.Success -> loadAllPantries()
                                                is DeletePantryResult.Error -> snackbarHostState.showSnackbar(
                                                    result.message
                                                )
                                            }
                                        }
                                    },
                                    onEditItem = { item -> showModifyDialog = item },
                                    onDeleteItem = { item ->
                                        val itemToDelete = pantry.items.find { "${it.emoji} ${it.name}" == "${item.first} ${item.second}" }
                                        itemToDelete?.let { pantryItem ->
                                            scope.launch {
                                                when (val result = pantryRepository.deletePantryItem(pantry.id, pantryItem.id)) {
                                                    is DeletePantryItemResult.Success -> loadAllPantries()
                                                    is DeletePantryItemResult.Error -> snackbarHostState.showSnackbar(
                                                        result.message
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onShareList = {
                                        selectedPantryForShare = pantry
                                        showShareSheet = true
                                    }
                                )
                            }
                        }

                        if (sharedPantries.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Shared with Me",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            items(sharedPantries, key = { "shared_${it.id}" }) { pantry ->
                                RenderPantryItem(
                                    pantry = pantry,
                                    searchText = searchText,
                                    canEdit = false, // Not owner, can't edit title or delete
                                    canShare = false, // Not owner, can't share
                                    onAddItem = {
                                        selectedPantryIdForAdd = pantry.id
                                        showAddDialog = true
                                    },
                                    onTitleChanged = { }, // Not editable
                                    onDeleteList = { }, // Not deletable
                                    onEditItem = { item -> showModifyDialog = item },
                                    onDeleteItem = { item ->
                                        val itemToDelete = pantry.items.find { "${it.emoji} ${it.name}" == "${item.first} ${item.second}" }
                                        itemToDelete?.let { pantryItem ->
                                            scope.launch {
                                                when (val result = pantryRepository.deletePantryItem(pantry.id, pantryItem.id)) {
                                                    is DeletePantryItemResult.Success -> loadAllPantries()
                                                    is DeletePantryItemResult.Error -> snackbarHostState.showSnackbar(
                                                        result.message
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onShareList = null // Can't share
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderPantryItem(
    pantry: PantryUI,
    searchText: String,
    canEdit: Boolean,
    canShare: Boolean,
    onAddItem: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDeleteList: () -> Unit,
    onEditItem: (PantryItemUI) -> Unit,
    onDeleteItem: (Pair<String, String>) -> Unit,
    onShareList: (() -> Unit)?
) {
    val filteredItems = pantry.items
        .filter { it.name.contains(searchText, ignoreCase = true) }

    // Create a map to find PantryItemUI by Pair<emoji, name>
    val itemsMap = filteredItems.associateBy { it.emoji to it.name }

    // Convert to Pairs for CollapsibleList
    val itemPairs = filteredItems.map { it.emoji to it.name }

    if (canEdit) {
        // Full functionality for owned pantries
        CollapsibleList(
            title = pantry.name,
            items = itemPairs,
            onAddItem = onAddItem,
            onTitleChanged = onTitleChanged,
            onDeleteList = onDeleteList,
            onEditItem = { pair ->
                // Find the full PantryItemUI from the pair
                itemsMap[pair]?.let { fullItem ->
                    onEditItem(fullItem)
                }
            },
            onDeleteItem = onDeleteItem,
            onShareList = onShareList
        )
    } else {
        // Limited functionality for shared pantries (no edit title, no delete list, no share)
        CollapsibleList(
            title = pantry.name,
            items = itemPairs,
            onAddItem = onAddItem,
            onTitleChanged = { }, // Disabled
            onDeleteList = { }, // Disabled
            onEditItem = { pair ->
                // Find the full PantryItemUI from the pair
                itemsMap[pair]?.let { fullItem ->
                    onEditItem(fullItem)
                }
            },
            onDeleteItem = onDeleteItem,
            onShareList = null, // Disabled
            subtitle = "Shared by ${pantry.owner?.name ?: "Unknown"}"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SharePantryBottomSheet(
    pantry: PantryUI,
    emailToShare: String,
    shareOperationCompleted: Int,
    onEmailChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onShare: (String, (String?) -> Unit) -> Unit,
    onRemoveUser: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSharing by remember { mutableStateOf(false) }
    var isRemovingUser by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Reset loading states when pantry data changes (after successful operations)
    LaunchedEffect(pantry.sharedWith.size) {
        isRemovingUser = null
        isSharing = false
        errorMessage = null
    }

    // Reset isSharing when share operation completes (success or error)
    LaunchedEffect(shareOperationCompleted) {
        if (shareOperationCompleted > 0) {
            isSharing = false
        }
    }

    // Clear error when email changes
    LaunchedEffect(emailToShare) {
        errorMessage = null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Title
            Text(
                text = "Share \"${pantry.name}\"",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Email input
            OutlinedTextField(
                value = emailToShare,
                onValueChange = onEmailChange,
                label = { Text("Email address") },
                placeholder = { Text("Enter email to share with") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null
            )

            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Share button
            Button(
                onClick = {
                    if (emailToShare.isNotBlank() && !isSharing) {
                        isSharing = true
                        errorMessage = null
                        onShare(emailToShare) { error ->
                            errorMessage = error
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = emailToShare.isNotBlank() && !isSharing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                if (isSharing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sharing...")
                } else {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Shared with section
            if (pantry.sharedWith.isNotEmpty()) {
                Text(
                    text = "Shared with:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    items(pantry.sharedWith) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${user.name} ${user.surname}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                androidx.compose.material3.IconButton(
                                    onClick = {
                                        if (isRemovingUser == null) {
                                            isRemovingUser = user.id
                                            onRemoveUser(user.id)
                                            // Reset will happen after pantries reload
                                        }
                                    },
                                    enabled = isRemovingUser == null
                                ) {
                                    if (isRemovingUser == user.id) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.Red,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove user",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Not shared with anyone yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

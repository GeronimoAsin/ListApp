package ar.edu.itba.listapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    var showModifyDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showCreatePantryDialog by remember { mutableStateOf(false) }

    // Share sheet state
    var showShareSheet by remember { mutableStateOf(false) }
    var selectedPantryForShare by remember { mutableStateOf<PantryUI?>(null) }
    var emailToShare by remember { mutableStateOf("") }

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

    // Share Bottom Sheet
    if (showShareSheet && selectedPantryForShare != null) {
        SharePantryBottomSheet(
            pantry = selectedPantryForShare!!,
            emailToShare = emailToShare,
            onEmailChange = { emailToShare = it },
            onDismiss = {
                showShareSheet = false
                selectedPantryForShare = null
                emailToShare = ""
            },
            onShare = { email ->
                scope.launch {
                    when (val result = pantryRepository.sharePantry(selectedPantryForShare!!.id, email)) {
                        is SharePantryResult.Success -> {
                            snackbarHostState.showSnackbar("Pantry shared with ${result.user.email}")
                            // Reload all pantries to update the sharedWith list
                            loadAllPantries()
                            showShareSheet = false
                            selectedPantryForShare = null
                            emailToShare = ""
                        }
                        is SharePantryResult.Error -> {
                            snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            },
            onRemoveUser = { userId ->
                scope.launch {
                    when (val result = pantryRepository.unsharePantry(selectedPantryForShare!!.id, userId)) {
                        is UnsharePantryResult.Success -> {
                            snackbarHostState.showSnackbar("User access revoked successfully")
                            // Reload all pantries to update the sharedWith list
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
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!isLoading && myPantries.isEmpty() && sharedPantries.isEmpty()) {
                // Only show "no items" when there are no pantries at all
                NoItemsMessage()
            } else if (!isLoading) {
                // Show all pantries, one below the other
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Section: My Pantries
                    if (myPantries.isNotEmpty()) {
                        item {
                            Text(
                                text = "My Pantries",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        myPantries.forEach { pantry ->
                            item(key = "my_${pantry.id}") {
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
                                                is UpdatePantryResult.Success -> {
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
                                        val itemToDelete = pantry.items.find { "${it.emoji} ${it.name}" == "${item.first} ${item.second}" }
                                        itemToDelete?.let { pantryItem ->
                                            scope.launch {
                                                when (val result = pantryRepository.deletePantryItem(pantry.id, pantryItem.id)) {
                                                    is DeletePantryItemResult.Success -> {
                                                        loadAllPantries()
                                                    }
                                                    is DeletePantryItemResult.Error -> {
                                                        snackbarHostState.showSnackbar(result.message)
                                                    }
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
                    }

                    // Section: Shared with Me
                    if (sharedPantries.isNotEmpty()) {
                        item {
                            Text(
                                text = "Shared with Me",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }

                        sharedPantries.forEach { pantry ->
                            item(key = "shared_${pantry.id}") {
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
                                    onEditItem = { item ->
                                        showModifyDialog = item
                                    },
                                    onDeleteItem = { item ->
                                        val itemToDelete = pantry.items.find { "${it.emoji} ${it.name}" == "${item.first} ${item.second}" }
                                        itemToDelete?.let { pantryItem ->
                                            scope.launch {
                                                when (val result = pantryRepository.deletePantryItem(pantry.id, pantryItem.id)) {
                                                    is DeletePantryItemResult.Success -> {
                                                        loadAllPantries()
                                                    }
                                                    is DeletePantryItemResult.Error -> {
                                                        snackbarHostState.showSnackbar(result.message)
                                                    }
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
    onEditItem: (Pair<String, String>) -> Unit,
    onDeleteItem: (Pair<String, String>) -> Unit,
    onShareList: (() -> Unit)?
) {
    val filteredItems = pantry.items
        .filter { it.name.contains(searchText, ignoreCase = true) }
        .map { it.emoji to it.name }

    if (canEdit) {
        // Full functionality for owned pantries
        CollapsibleList(
            title = pantry.name,
            items = filteredItems,
            onAddItem = onAddItem,
            onTitleChanged = onTitleChanged,
            onDeleteList = onDeleteList,
            onEditItem = onEditItem,
            onDeleteItem = onDeleteItem,
            onShareList = onShareList
        )
    } else {
        // Limited functionality for shared pantries (no edit title, no delete list, no share)
        CollapsibleList(
            title = pantry.name,
            items = filteredItems,
            onAddItem = onAddItem,
            onTitleChanged = { }, // Disabled
            onDeleteList = { }, // Disabled
            onEditItem = onEditItem,
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
    onEmailChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit,
    onRemoveUser: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Share button
            Button(
                onClick = {
                    if (emailToShare.isNotBlank()) {
                        onShare(emailToShare)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = emailToShare.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share")
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
                                    onClick = { onRemoveUser(user.id) }
                                ) {
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

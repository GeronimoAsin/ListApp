package ar.edu.itba.listapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.data.model.Product
import ar.edu.itba.listapp.data.network.*
import ar.edu.itba.listapp.ui.composables.*
import ar.edu.itba.listapp.ui.utils.isTablet
import kotlinx.coroutines.launch

private data class ListItemUI(
    val id: Long,
    val productId: Long,
    val emoji: String,
    val name: String,
    val quantity: Double,
    val unit: String?
)

private data class ShoppingListUI(
    val id: Long,
    val name: String,
    val items: List<ListItemUI>,
    val owner: ar.edu.itba.listapp.data.model.Owner? = null,
    val sharedWith: List<ar.edu.itba.listapp.data.model.Owner> = emptyList()
)

@Composable
fun ListsScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val listRepository = remember { ListRepository(context, sessionManager = SessionManager(context)) }
    val productRepository = remember { ProductRepository(context, sessionManager = SessionManager(context)) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(true) }
    var searchText by remember { mutableStateOf("") }

    // State for multiple shopping lists
    val myLists = remember { mutableStateListOf<ShoppingListUI>() }
    val sharedLists = remember { mutableStateListOf<ShoppingListUI>() }

    // Available products for adding to list
    val availableProducts = remember { mutableStateListOf<Product>() }

    // Track which list is being added to
    var selectedListIdForAdd by remember { mutableStateOf<Long?>(null) }

    // Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var showModifyDialog by remember { mutableStateOf<ListItemUI?>(null) }
    var showCreateListDialog by remember { mutableStateOf(false) }

    // Share sheet state
    var showShareSheet by remember { mutableStateOf(false) }
    var selectedListForShare by remember { mutableStateOf<ShoppingListUI?>(null) }
    var emailToShare by remember { mutableStateOf("") }
    var shareOperationCompleted by remember { mutableStateOf(0) }

    // Function to load shopping list items
    suspend fun loadListItems(listId: Long): List<ListItemUI> {
        return when (val itemsResult = listRepository.getShoppingListItems(listId, page = 1, perPage = 500)) {
            is GetShoppingListItemsResult.Success -> {
                itemsResult.items.map { item ->
                    ListItemUI(
                        id = item.id,
                        productId = item.product.id,
                        emoji = item.product.metadata["emoji"] ?: "📦",
                        name = item.product.name,
                        quantity = item.quantity,
                        unit = item.unit
                    )
                }
            }
            is GetShoppingListItemsResult.Error -> {
                scope.launch { snackbarHostState.showSnackbar(itemsResult.message) }
                emptyList()
            }
        }
    }

    // Load all shopping lists
    fun loadAllLists() {
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

            // Load my lists (owner=true)
            when (val myListsResult = listRepository.getShoppingLists(owner = true, page = 1, perPage = 100)) {
                is GetShoppingListsResult.Success -> {
                    myLists.clear()
                    for (list in myListsResult.lists) {
                        val items = loadListItems(list.id)
                        myLists.add(
                            ShoppingListUI(
                                id = list.id,
                                name = list.name,
                                items = items,
                                owner = list.owner,
                                sharedWith = list.sharedWith
                            )
                        )
                    }
                }
                is GetShoppingListsResult.Error -> {
                    snackbarHostState.showSnackbar(myListsResult.message)
                }
            }

            // Load shared lists (owner=false)
            when (val sharedListsResult = listRepository.getShoppingLists(owner = false, page = 1, perPage = 100)) {
                is GetShoppingListsResult.Success -> {
                    sharedLists.clear()
                    for (list in sharedListsResult.lists) {
                        val items = loadListItems(list.id)
                        sharedLists.add(
                            ShoppingListUI(
                                id = list.id,
                                name = list.name,
                                items = items,
                                owner = list.owner,
                                sharedWith = list.sharedWith
                            )
                        )
                    }
                }
                is GetShoppingListsResult.Error -> {
                    snackbarHostState.showSnackbar(sharedListsResult.message)
                }
            }

            isLoading = false
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        loadAllLists()
    }

    // Dialogs
    if (showCreateListDialog) {
        NewShoppingListForm(
            onDismiss = { showCreateListDialog = false },
            onConfirm = { name ->
                scope.launch {
                    when (val result = listRepository.createShoppingList(name)) {
                        is CreateShoppingListResult.Success -> {
                            showCreateListDialog = false
                            loadAllLists()
                            snackbarHostState.showSnackbar(context.getString(R.string.list_created))
                        }
                        is CreateShoppingListResult.Error -> {
                            snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            }
        )
    }

    if (showAddDialog && selectedListIdForAdd != null) {
        AddToListDialog(
            products = availableProducts,
            onDismiss = {
                showAddDialog = false
                selectedListIdForAdd = null
            },
            onConfirm = { product, quantity, unit ->
                scope.launch {
                    when (val result = listRepository.addShoppingListItem(
                        listId = selectedListIdForAdd!!,
                        productId = product.id,
                        quantity = quantity,
                        unit = unit
                    )) {
                        is AddShoppingListItemResult.Success -> {
                            showAddDialog = false
                            selectedListIdForAdd = null
                            loadAllLists()
                            snackbarHostState.showSnackbar(context.getString(R.string.item_added))
                        }
                        is AddShoppingListItemResult.Error -> {
                            snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            }
        )
    }

    showModifyDialog?.let { item ->
        EditListItemDialog(
            emoji = item.emoji,
            name = item.name,
            currentQuantity = item.quantity,
            currentUnit = item.unit,
            onDismiss = { showModifyDialog = null },
            onConfirm = { quantity, unit ->
                scope.launch {
                    // Find the list that contains this item
                    val listId = myLists.find { list -> list.items.any { it.id == item.id } }?.id
                        ?: sharedLists.find { list -> list.items.any { it.id == item.id } }?.id

                    if (listId != null) {
                        when (val result = listRepository.updateShoppingListItem(
                            listId = listId,
                            itemId = item.id,
                            quantity = quantity,
                            unit = unit
                        )) {
                            is UpdateShoppingListItemResult.Success -> {
                                showModifyDialog = null
                                loadAllLists()
                                snackbarHostState.showSnackbar(context.getString(R.string.item_updated))
                            }
                            is UpdateShoppingListItemResult.Error -> {
                                snackbarHostState.showSnackbar(result.message)
                            }
                        }
                    }
                }
            }
        )
    }

    // Share Bottom Sheet
    if (showShareSheet && selectedListForShare != null) {
        ShareListBottomSheet(
            list = selectedListForShare!!,
            emailToShare = emailToShare,
            shareOperationCompleted = shareOperationCompleted,
            onEmailChange = { emailToShare = it },
            onDismiss = {
                showShareSheet = false
                selectedListForShare = null
                emailToShare = ""
            },
            onShare = { email: String, onError: (String?) -> Unit ->
                scope.launch {
                    when (val result = listRepository.shareShoppingList(selectedListForShare!!.id, email)) {
                        is ShareShoppingListResult.Success -> {
                            shareOperationCompleted++
                            emailToShare = ""
                            loadAllLists()
                            snackbarHostState.showSnackbar(context.getString(R.string.list_shared))
                            onError(null)
                        }
                        is ShareShoppingListResult.Error -> {
                            onError(result.message)
                        }
                    }
                }
            },
            onRemoveUser = { userId: Long ->
                scope.launch {
                    when (val result = listRepository.unshareShoppingList(selectedListForShare!!.id, userId)) {
                        is UnshareShoppingListResult.Success -> {
                            loadAllLists()
                            snackbarHostState.showSnackbar(context.getString(R.string.user_removed))
                        }
                        is UnshareShoppingListResult.Error -> {
                            snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.padding(padding),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // New List Button
            Button(
                onClick = { showCreateListDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF78B945),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(id = R.string.new_shopping_list),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SearchBar(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = stringResource(id = R.string.search_lists),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF78B945)
                )
            }

            if (!isLoading && myLists.isEmpty() && sharedLists.isEmpty()) {
                NoItemsMessage()
            } else if (!isLoading) {
                if (isTablet()) {
                    // Grid layout for tablets
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // My Lists section
                        if (myLists.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Text(
                                    text = stringResource(id = R.string.my_lists),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(myLists) { list ->
                                RenderListItem(
                                    list = list,
                                    searchText = searchText,
                                    canEdit = true,
                                    canShare = true,
                                    onAddItem = {
                                        selectedListIdForAdd = list.id
                                        showAddDialog = true
                                    },
                                    onTitleChanged = { newName ->
                                        scope.launch {
                                            when (val result = listRepository.updateShoppingList(list.id, newName)) {
                                                is UpdateShoppingListResult.Success -> {
                                                    loadAllLists()
                                                }
                                                is UpdateShoppingListResult.Error -> {
                                                    snackbarHostState.showSnackbar(result.message)
                                                }
                                            }
                                        }
                                    },
                                    onDeleteList = {
                                        scope.launch {
                                            when (val result = listRepository.deleteShoppingList(list.id)) {
                                                is DeleteShoppingListResult.Success -> {
                                                    loadAllLists()
                                                    snackbarHostState.showSnackbar(context.getString(R.string.list_deleted))
                                                }
                                                is DeleteShoppingListResult.Error -> {
                                                    snackbarHostState.showSnackbar(result.message)
                                                }
                                            }
                                        }
                                    },
                                    onEditItem = { item ->
                                        showModifyDialog = item
                                    },
                                    onDeleteItem = { pair ->
                                        val itemToDelete = list.items.find { it.emoji == pair.first && it.name == pair.second }
                                        itemToDelete?.let { item ->
                                            scope.launch {
                                                when (val result = listRepository.deleteShoppingListItem(list.id, item.id)) {
                                                    is DeleteShoppingListItemResult.Success -> {
                                                        loadAllLists()
                                                        snackbarHostState.showSnackbar(context.getString(R.string.item_deleted))
                                                    }
                                                    is DeleteShoppingListItemResult.Error -> {
                                                        snackbarHostState.showSnackbar(result.message)
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onShareList = {
                                        selectedListForShare = list
                                        showShareSheet = true
                                    }
                                )
                            }
                        }

                        // Shared Lists section
                        if (sharedLists.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Text(
                                    text = stringResource(id = R.string.shared_lists),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(sharedLists) { list ->
                                RenderListItem(
                                    list = list,
                                    searchText = searchText,
                                    canEdit = false,
                                    canShare = false,
                                    onAddItem = {
                                        selectedListIdForAdd = list.id
                                        showAddDialog = true
                                    },
                                    onTitleChanged = { },
                                    onDeleteList = { },
                                    onEditItem = { item ->
                                        showModifyDialog = item
                                    },
                                    onDeleteItem = { pair ->
                                        val itemToDelete = list.items.find { it.emoji == pair.first && it.name == pair.second }
                                        itemToDelete?.let { item ->
                                            scope.launch {
                                                when (val result = listRepository.deleteShoppingListItem(list.id, item.id)) {
                                                    is DeleteShoppingListItemResult.Success -> {
                                                        loadAllLists()
                                                        snackbarHostState.showSnackbar(context.getString(R.string.item_deleted))
                                                    }
                                                    is DeleteShoppingListItemResult.Error -> {
                                                        snackbarHostState.showSnackbar(result.message)
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onShareList = null
                                )
                            }
                        }
                    }
                } else {
                    // List layout for phones
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // My Lists section
                        if (myLists.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(id = R.string.my_lists),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(myLists) { list ->
                                RenderListItem(
                                    list = list,
                                    searchText = searchText,
                                    canEdit = true,
                                    canShare = true,
                                    onAddItem = {
                                        selectedListIdForAdd = list.id
                                        showAddDialog = true
                                    },
                                    onTitleChanged = { newName ->
                                        scope.launch {
                                            when (val result = listRepository.updateShoppingList(list.id, newName)) {
                                                is UpdateShoppingListResult.Success -> {
                                                    loadAllLists()
                                                }
                                                is UpdateShoppingListResult.Error -> {
                                                    snackbarHostState.showSnackbar(result.message)
                                                }
                                            }
                                        }
                                    },
                                    onDeleteList = {
                                        scope.launch {
                                            when (val result = listRepository.deleteShoppingList(list.id)) {
                                                is DeleteShoppingListResult.Success -> {
                                                    loadAllLists()
                                                    snackbarHostState.showSnackbar(context.getString(R.string.list_deleted))
                                                }
                                                is DeleteShoppingListResult.Error -> {
                                                    snackbarHostState.showSnackbar(result.message)
                                                }
                                            }
                                        }
                                    },
                                    onEditItem = { item ->
                                        showModifyDialog = item
                                    },
                                    onDeleteItem = { pair ->
                                        val itemToDelete = list.items.find { it.emoji == pair.first && it.name == pair.second }
                                        itemToDelete?.let { item ->
                                            scope.launch {
                                                when (val result = listRepository.deleteShoppingListItem(list.id, item.id)) {
                                                    is DeleteShoppingListItemResult.Success -> {
                                                        loadAllLists()
                                                        snackbarHostState.showSnackbar(context.getString(R.string.item_deleted))
                                                    }
                                                    is DeleteShoppingListItemResult.Error -> {
                                                        snackbarHostState.showSnackbar(result.message)
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onShareList = {
                                        selectedListForShare = list
                                        showShareSheet = true
                                    }
                                )
                            }
                        }

                        // Shared Lists section
                        if (sharedLists.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(id = R.string.shared_lists),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(sharedLists) { list ->
                                RenderListItem(
                                    list = list,
                                    searchText = searchText,
                                    canEdit = false,
                                    canShare = false,
                                    onAddItem = {
                                        selectedListIdForAdd = list.id
                                        showAddDialog = true
                                    },
                                    onTitleChanged = { },
                                    onDeleteList = { },
                                    onEditItem = { item ->
                                        showModifyDialog = item
                                    },
                                    onDeleteItem = { pair ->
                                        val itemToDelete = list.items.find { it.emoji == pair.first && it.name == pair.second }
                                        itemToDelete?.let { item ->
                                            scope.launch {
                                                when (val result = listRepository.deleteShoppingListItem(list.id, item.id)) {
                                                    is DeleteShoppingListItemResult.Success -> {
                                                        loadAllLists()
                                                        snackbarHostState.showSnackbar(context.getString(R.string.item_deleted))
                                                    }
                                                    is DeleteShoppingListItemResult.Error -> {
                                                        snackbarHostState.showSnackbar(result.message)
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onShareList = null
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
private fun RenderListItem(
    list: ShoppingListUI,
    searchText: String,
    canEdit: Boolean,
    canShare: Boolean,
    onAddItem: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDeleteList: () -> Unit,
    onEditItem: (ListItemUI) -> Unit,
    onDeleteItem: (Pair<String, String>) -> Unit,
    onShareList: (() -> Unit)?
) {
    val filteredItems = list.items
        .filter { it.name.contains(searchText, ignoreCase = true) }

    // Create a map to find ListItemUI by Pair<emoji, name>
    val itemsMap = filteredItems.associateBy { it.emoji to it.name }

    // Convert to Triples (emoji, name with quantity/unit) for CollapsibleList
    val itemPairs = filteredItems.map { 
        val quantityUnit = if (it.unit != null) {
            "${it.quantity} ${it.unit}"
        } else {
            "${it.quantity}"
        }
        it.emoji to "${it.name} - $quantityUnit"
    }

    if (canEdit) {
        // Full functionality for owned lists
        CollapsibleList(
            title = list.name,
            items = itemPairs,
            onAddItem = onAddItem,
            onTitleChanged = onTitleChanged,
            onDeleteList = onDeleteList,
            onEditItem = { pair ->
                // Extract emoji and original name (without quantity)
                val originalItem = filteredItems.find { it.emoji == pair.first }
                originalItem?.let { 
                    val originalPair = it.emoji to it.name
                    itemsMap[originalPair]?.let { item -> onEditItem(item) }
                }
            },
            onDeleteItem = { pair ->
                // Extract emoji and original name (without quantity)
                val originalItem = filteredItems.find { it.emoji == pair.first }
                originalItem?.let { 
                    val originalPair = it.emoji to it.name
                    onDeleteItem(originalPair)
                }
            },
            onShareList = onShareList
        )
    } else {
        // Limited functionality for shared lists
        CollapsibleList(
            title = list.name,
            items = itemPairs,
            onAddItem = onAddItem,
            onTitleChanged = { },
            onDeleteList = { },
            onEditItem = { pair ->
                // Extract emoji and original name (without quantity)
                val originalItem = filteredItems.find { it.emoji == pair.first }
                originalItem?.let { 
                    val originalPair = it.emoji to it.name
                    itemsMap[originalPair]?.let { item -> onEditItem(item) }
                }
            },
            onDeleteItem = { pair ->
                // Extract emoji and original name (without quantity)
                val originalItem = filteredItems.find { it.emoji == pair.first }
                originalItem?.let { 
                    val originalPair = it.emoji to it.name
                    onDeleteItem(originalPair)
                }
            },
            onShareList = null,
            subtitle = "Shared by ${list.owner?.name ?: "Unknown"}"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareListBottomSheet(
    list: ShoppingListUI,
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

    // Reset loading states when list data changes
    LaunchedEffect(list.sharedWith.size) {
        isRemovingUser = null
        isSharing = false
        errorMessage = null
    }

    // Reset isSharing when share operation completes
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
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.share_list),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Share input
            OutlinedTextField(
                value = emailToShare,
                onValueChange = onEmailChange,
                label = { Text(stringResource(id = R.string.email)) },
                placeholder = { Text(stringResource(id = R.string.enter_email)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                isError = errorMessage != null,
                enabled = !isSharing
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (emailToShare.isNotBlank()) {
                        isSharing = true
                        onShare(emailToShare) { error ->
                            errorMessage = error
                            if (error != null) {
                                isSharing = false
                            }
                        }
                    }
                },
                enabled = emailToShare.isNotBlank() && !isSharing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
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
                } else {
                    Text(stringResource(id = R.string.share))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Shared with section
            Text(
                text = stringResource(id = R.string.shared_with),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (list.sharedWith.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.not_shared_yet),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            } else {
                list.sharedWith.forEach { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.name,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = user.email,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Button(
                                onClick = {
                                    isRemovingUser = user.id
                                    onRemoveUser(user.id)
                                },
                                enabled = isRemovingUser != user.id,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red.copy(alpha = 0.1f),
                                    contentColor = Color.Red
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isRemovingUser == user.id) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.Red,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(stringResource(id = R.string.remove))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

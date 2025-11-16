package ar.edu.itba.listapp.data.network

import android.content.Context
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.data.model.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

// Result types for Pantries
sealed interface CreatePantryResult {
    data class Success(val pantry: Pantry) : CreatePantryResult
    data class Error(val message: String) : CreatePantryResult
}

sealed interface GetPantriesResult {
    data class Success(val pantries: List<Pantry>, val pagination: Pagination) : GetPantriesResult
    data class Error(val message: String) : GetPantriesResult
}

sealed interface GetPantryResult {
    data class Success(val pantry: Pantry) : GetPantryResult
    data class Error(val message: String) : GetPantryResult
}

sealed interface UpdatePantryResult {
    data class Success(val pantry: Pantry) : UpdatePantryResult
    data class Error(val message: String) : UpdatePantryResult
}

sealed interface DeletePantryResult {
    data object Success : DeletePantryResult
    data class Error(val message: String) : DeletePantryResult
}

// Result types for Pantry Items
sealed interface AddPantryItemResult {
    data class Success(val item: PantryItem) : AddPantryItemResult
    data class Error(val message: String) : AddPantryItemResult
}

sealed interface GetPantryItemsResult {
    data class Success(val items: List<PantryItem>, val pagination: Pagination) : GetPantryItemsResult
    data class Error(val message: String) : GetPantryItemsResult
}

sealed interface UpdatePantryItemResult {
    data class Success(val item: PantryItem) : UpdatePantryItemResult
    data class Error(val message: String) : UpdatePantryItemResult
}

sealed interface DeletePantryItemResult {
    data object Success : DeletePantryItemResult
    data class Error(val message: String) : DeletePantryItemResult
}

class PantryRepository(
    private val context: Context,
    private val service: PantryService = NetworkModule.pantryService,
    private val sessionManager: SessionManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    init {
        NetworkModule.setAuthTokenProvider { sessionManager.loadAuthToken() }
    }

    // Pantry operations
    suspend fun createPantry(name: String, metadata: Map<String, String>? = null): CreatePantryResult =
        withContext(dispatcher) {
            try {
                val request = CreatePantryRequest(name, metadata)
                val pantry = service.createPantry(request)
                CreatePantryResult.Success(pantry)
            } catch (httpException: HttpException) {
                val message = when (httpException.code()) {
                    400 -> context.getString(R.string.pantry_error_bad_request)
                    401 -> context.getString(R.string.pantry_error_unauthorized)
                    409 -> context.getString(R.string.pantry_error_conflict)
                    500 -> context.getString(R.string.pantry_error_server)
                    else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
                }
                CreatePantryResult.Error(message)
            } catch (ioException: IOException) {
                CreatePantryResult.Error(context.getString(R.string.pantry_error_connection))
            } catch (exception: Exception) {
                CreatePantryResult.Error(context.getString(R.string.pantry_error_generic, exception.message ?: ""))
            }
        }

    suspend fun getPantries(
        owner: Boolean? = null,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String = "createdAt",
        order: String = "ASC"
    ): GetPantriesResult = withContext(dispatcher) {
        try {
            val response = service.getPantries(owner, page, perPage, sortBy, order)
            GetPantriesResult.Success(response.data, response.pagination)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                401 -> context.getString(R.string.pantry_error_unauthorized)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            GetPantriesResult.Error(message)
        } catch (ioException: IOException) {
            GetPantriesResult.Error(context.getString(R.string.pantry_error_connection))
        } catch (exception: Exception) {
            GetPantriesResult.Error(context.getString(R.string.pantry_error_generic, exception.message ?: ""))
        }
    }

    suspend fun getPantry(id: Long): GetPantryResult = withContext(dispatcher) {
        try {
            val pantry = service.getPantry(id)
            GetPantryResult.Success(pantry)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                401 -> context.getString(R.string.pantry_error_unauthorized)
                404 -> context.getString(R.string.pantry_error_not_found)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            GetPantryResult.Error(message)
        } catch (ioException: IOException) {
            GetPantryResult.Error(context.getString(R.string.pantry_error_connection))
        } catch (exception: Exception) {
            GetPantryResult.Error(context.getString(R.string.pantry_error_generic, exception.message ?: ""))
        }
    }

    suspend fun updatePantry(
        id: Long,
        name: String,
        metadata: Map<String, String>? = null
    ): UpdatePantryResult = withContext(dispatcher) {
        try {
            val request = UpdatePantryRequest(name, metadata)
            val pantry = service.updatePantry(id, request)
            UpdatePantryResult.Success(pantry)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.pantry_error_bad_request)
                401 -> context.getString(R.string.pantry_error_unauthorized)
                404 -> context.getString(R.string.pantry_error_not_found)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            UpdatePantryResult.Error(message)
        } catch (ioException: IOException) {
            UpdatePantryResult.Error(context.getString(R.string.pantry_error_connection))
        } catch (exception: Exception) {
            UpdatePantryResult.Error(context.getString(R.string.pantry_error_generic, exception.message ?: ""))
        }
    }

    suspend fun deletePantry(id: Long): DeletePantryResult = withContext(dispatcher) {
        try {
            service.deletePantry(id)
            DeletePantryResult.Success
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                401 -> context.getString(R.string.pantry_error_unauthorized)
                404 -> context.getString(R.string.pantry_error_not_found)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            DeletePantryResult.Error(message)
        } catch (ioException: IOException) {
            DeletePantryResult.Error(context.getString(R.string.pantry_error_connection))
        } catch (exception: Exception) {
            DeletePantryResult.Error(context.getString(R.string.pantry_error_generic, exception.message ?: ""))
        }
    }

    // Pantry Item operations
    suspend fun addPantryItem(
        pantryId: Long,
        productId: Long,
        quantity: Double,
        unit: String? = null,
        metadata: Map<String, String>? = null
    ): AddPantryItemResult = withContext(dispatcher) {
        try {
            val request = CreatePantryItemRequest(
                product = ProductReference(productId),
                quantity = quantity,
                unit = unit,
                metadata = metadata
            )
            val item = service.addPantryItem(pantryId, request)
            AddPantryItemResult.Success(item)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.pantry_item_error_bad_request)
                401 -> context.getString(R.string.pantry_item_error_unauthorized)
                404 -> context.getString(R.string.pantry_item_error_not_found)
                409 -> context.getString(R.string.pantry_item_error_conflict)
                500 -> context.getString(R.string.pantry_item_error_server)
                else -> context.getString(R.string.pantry_item_error_unexpected, httpException.code())
            }
            AddPantryItemResult.Error(message)
        } catch (ioException: IOException) {
            AddPantryItemResult.Error(context.getString(R.string.pantry_item_error_connection))
        } catch (exception: Exception) {
            AddPantryItemResult.Error(context.getString(R.string.pantry_item_error_generic, exception.message ?: ""))
        }
    }

    suspend fun getPantryItems(
        pantryId: Long,
        page: Int = 1,
        perPage: Int = 100,
        sortBy: String? = null,
        order: String = "ASC",
        search: String? = null,
        categoryId: Long? = null
    ): GetPantryItemsResult = withContext(dispatcher) {
        try {
            val response = service.getPantryItems(pantryId, page, perPage, sortBy, order, search, categoryId)
            GetPantryItemsResult.Success(response.data, response.pagination)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                401 -> context.getString(R.string.pantry_item_error_unauthorized)
                404 -> context.getString(R.string.pantry_item_error_not_found)
                500 -> context.getString(R.string.pantry_item_error_server)
                else -> context.getString(R.string.pantry_item_error_unexpected, httpException.code())
            }
            GetPantryItemsResult.Error(message)
        } catch (ioException: IOException) {
            GetPantryItemsResult.Error(context.getString(R.string.pantry_item_error_connection))
        } catch (exception: Exception) {
            GetPantryItemsResult.Error(context.getString(R.string.pantry_item_error_generic, exception.message ?: ""))
        }
    }

    suspend fun updatePantryItem(
        pantryId: Long,
        itemId: Long,
        quantity: Double,
        unit: String? = null,
        metadata: Map<String, String>? = null
    ): UpdatePantryItemResult = withContext(dispatcher) {
        try {
            val request = UpdatePantryItemRequest(quantity, unit, metadata)
            val item = service.updatePantryItem(pantryId, itemId, request)
            UpdatePantryItemResult.Success(item)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.pantry_item_error_bad_request)
                401 -> context.getString(R.string.pantry_item_error_unauthorized)
                404 -> context.getString(R.string.pantry_item_error_not_found)
                500 -> context.getString(R.string.pantry_item_error_server)
                else -> context.getString(R.string.pantry_item_error_unexpected, httpException.code())
            }
            UpdatePantryItemResult.Error(message)
        } catch (ioException: IOException) {
            UpdatePantryItemResult.Error(context.getString(R.string.pantry_item_error_connection))
        } catch (exception: Exception) {
            UpdatePantryItemResult.Error(context.getString(R.string.pantry_item_error_generic, exception.message ?: ""))
        }
    }

    suspend fun deletePantryItem(pantryId: Long, itemId: Long): DeletePantryItemResult =
        withContext(dispatcher) {
            try {
                service.deletePantryItem(pantryId, itemId)
                DeletePantryItemResult.Success
            } catch (httpException: HttpException) {
                val message = when (httpException.code()) {
                    401 -> context.getString(R.string.pantry_item_error_unauthorized)
                    404 -> context.getString(R.string.pantry_item_error_not_found)
                    500 -> context.getString(R.string.pantry_item_error_server)
                    else -> context.getString(R.string.pantry_item_error_unexpected, httpException.code())
                }
                DeletePantryItemResult.Error(message)
            } catch (ioException: IOException) {
                DeletePantryItemResult.Error(context.getString(R.string.pantry_item_error_connection))
            } catch (exception: Exception) {
                DeletePantryItemResult.Error(context.getString(R.string.pantry_item_error_generic, exception.message ?: ""))
            }
        }
}


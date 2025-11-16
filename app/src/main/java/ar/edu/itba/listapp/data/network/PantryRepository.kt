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
sealed interface GetPantriesResult {
    data class Success(val pantries: List<Pantry>, val pagination: Pagination) : GetPantriesResult
    data class Error(val message: String) : GetPantriesResult
}

sealed interface CreatePantryResult {
    data class Success(val pantry: Pantry) : CreatePantryResult
    data class Error(val message: String) : CreatePantryResult
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

sealed interface SharePantryResult {
    data class Success(val user: Owner) : SharePantryResult
    data class Error(val message: String) : SharePantryResult
}

sealed interface GetSharedUsersResult {
    data class Success(val users: List<Owner>) : GetSharedUsersResult
    data class Error(val message: String) : GetSharedUsersResult
}

sealed interface UnsharePantryResult {
    data object Success : UnsharePantryResult
    data class Error(val message: String) : UnsharePantryResult
}

// Result types for Pantry Items
sealed interface GetPantryItemsResult {
    data class Success(val items: List<PantryItem>, val pagination: Pagination) : GetPantryItemsResult
    data class Error(val message: String) : GetPantryItemsResult
}

sealed interface AddPantryItemResult {
    data class Success(val item: PantryItem) : AddPantryItemResult
    data class Error(val message: String) : AddPantryItemResult
}

sealed interface GetPantryItemResult {
    data class Success(val item: PantryItem) : GetPantryItemResult
    data class Error(val message: String) : GetPantryItemResult
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
        // Provide token on each request
        NetworkModule.setAuthTokenProvider { sessionManager.loadAuthToken() }
    }

    // Pantry operations
    suspend fun getPantries(
        owner: Boolean? = null,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String? = null,
        order: String? = null
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
            val message = context.getString(R.string.pantry_error_connection)
            GetPantriesResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.pantry_error_generic, exception.message ?: "")
            GetPantriesResult.Error(message)
        }
    }

    suspend fun createPantry(name: String, metadata: Map<String, String> = emptyMap()): CreatePantryResult =
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
                val message = context.getString(R.string.pantry_error_connection)
                CreatePantryResult.Error(message)
            } catch (exception: Exception) {
                val message = context.getString(R.string.pantry_error_generic, exception.message ?: "")
                CreatePantryResult.Error(message)
            }
        }

    suspend fun getPantry(id: Long): GetPantryResult = withContext(dispatcher) {
        try {
            val pantry = service.getPantry(id)
            GetPantryResult.Success(pantry)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.pantry_error_bad_request)
                401 -> context.getString(R.string.pantry_error_unauthorized)
                404 -> context.getString(R.string.pantry_error_not_found)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            GetPantryResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.pantry_error_connection)
            GetPantryResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.pantry_error_generic, exception.message ?: "")
            GetPantryResult.Error(message)
        }
    }

    suspend fun updatePantry(
        id: Long,
        name: String,
        metadata: Map<String, String> = emptyMap()
    ): UpdatePantryResult = withContext(dispatcher) {
        try {
            val request = UpdatePantryRequest(name, metadata)
            val pantry = service.updatePantry(id, request)
            UpdatePantryResult.Success(pantry)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.pantry_error_bad_request)
                401 -> context.getString(R.string.pantry_error_unauthorized)
                403 -> context.getString(R.string.pantry_error_forbidden)
                404 -> context.getString(R.string.pantry_error_not_found)
                409 -> context.getString(R.string.pantry_error_conflict)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            UpdatePantryResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.pantry_error_connection)
            UpdatePantryResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.pantry_error_generic, exception.message ?: "")
            UpdatePantryResult.Error(message)
        }
    }

    suspend fun deletePantry(id: Long): DeletePantryResult = withContext(dispatcher) {
        try {
            service.deletePantry(id)
            DeletePantryResult.Success
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.pantry_error_bad_request)
                401 -> context.getString(R.string.pantry_error_unauthorized)
                403 -> context.getString(R.string.pantry_error_forbidden)
                404 -> context.getString(R.string.pantry_error_not_found)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            DeletePantryResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.pantry_error_connection)
            DeletePantryResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.pantry_error_generic, exception.message ?: "")
            DeletePantryResult.Error(message)
        }
    }

    suspend fun sharePantry(id: Long, email: String): SharePantryResult = withContext(dispatcher) {
        try {
            val request = SharePantryRequest(email)
            val user = service.sharePantry(id, request)
            SharePantryResult.Success(user)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.pantry_error_bad_request)
                401 -> context.getString(R.string.pantry_error_unauthorized)
                404 -> context.getString(R.string.pantry_error_not_found)
                409 -> context.getString(R.string.pantry_error_conflict)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            SharePantryResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.pantry_error_connection)
            SharePantryResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.pantry_error_generic, exception.message ?: "")
            SharePantryResult.Error(message)
        }
    }

    suspend fun getSharedUsers(id: Long): GetSharedUsersResult = withContext(dispatcher) {
        try {
            val users = service.getSharedUsers(id)
            GetSharedUsersResult.Success(users)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.pantry_error_bad_request)
                401 -> context.getString(R.string.pantry_error_unauthorized)
                404 -> context.getString(R.string.pantry_error_not_found)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            GetSharedUsersResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.pantry_error_connection)
            GetSharedUsersResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.pantry_error_generic, exception.message ?: "")
            GetSharedUsersResult.Error(message)
        }
    }

    suspend fun unsharePantry(id: Long, userId: Long): UnsharePantryResult = withContext(dispatcher) {
        try {
            service.unsharePantry(id, userId)
            UnsharePantryResult.Success
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.pantry_error_bad_request)
                401 -> context.getString(R.string.pantry_error_unauthorized)
                403 -> context.getString(R.string.pantry_error_forbidden)
                404 -> context.getString(R.string.pantry_error_not_found)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            UnsharePantryResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.pantry_error_connection)
            UnsharePantryResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.pantry_error_generic, exception.message ?: "")
            UnsharePantryResult.Error(message)
        }
    }

    // Pantry Item operations
    suspend fun getPantryItems(
        pantryId: Long,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String = "createdAt",
        order: String = "ASC"
    ): GetPantryItemsResult = withContext(dispatcher) {
        try {
            val response = service.getPantryItems(pantryId, page, perPage, sortBy, order)
            GetPantryItemsResult.Success(response.data, response.pagination)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                401 -> context.getString(R.string.pantry_item_error_unauthorized)
                404 -> context.getString(R.string.pantry_error_not_found)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            GetPantryItemsResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.pantry_error_connection)
            GetPantryItemsResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.pantry_error_generic, exception.message ?: "")
            GetPantryItemsResult.Error(message)
        }
    }

    suspend fun addPantryItem(
        pantryId: Long,
        productId: Long,
        quantity: Double,
        unit: String? = null,
        metadata: Map<String, String> = emptyMap()
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
                404 -> context.getString(R.string.pantry_error_not_found)
                409 -> context.getString(R.string.pantry_item_error_conflict)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            AddPantryItemResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.pantry_error_connection)
            AddPantryItemResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.pantry_error_generic, exception.message ?: "")
            AddPantryItemResult.Error(message)
        }
    }

    suspend fun getPantryItem(pantryId: Long, itemId: Long): GetPantryItemResult = withContext(dispatcher) {
        try {
            val item = service.getPantryItem(pantryId, itemId)
            GetPantryItemResult.Success(item)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                401 -> context.getString(R.string.pantry_item_error_unauthorized)
                404 -> context.getString(R.string.pantry_error_not_found)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            GetPantryItemResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.pantry_error_connection)
            GetPantryItemResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.pantry_error_generic, exception.message ?: "")
            GetPantryItemResult.Error(message)
        }
    }

    suspend fun updatePantryItem(
        pantryId: Long,
        itemId: Long,
        quantity: Double,
        unit: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): UpdatePantryItemResult = withContext(dispatcher) {
        try {
            val request = UpdatePantryItemRequest(quantity, unit, metadata)
            val item = service.updatePantryItem(pantryId, itemId, request)
            UpdatePantryItemResult.Success(item)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.pantry_item_error_bad_request)
                401 -> context.getString(R.string.pantry_item_error_unauthorized)
                404 -> context.getString(R.string.pantry_error_not_found)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            UpdatePantryItemResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.pantry_error_connection)
            UpdatePantryItemResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.pantry_error_generic, exception.message ?: "")
            UpdatePantryItemResult.Error(message)
        }
    }

    suspend fun deletePantryItem(pantryId: Long, itemId: Long): DeletePantryItemResult = withContext(dispatcher) {
        try {
            service.deletePantryItem(pantryId, itemId)
            DeletePantryItemResult.Success
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                401 -> context.getString(R.string.pantry_item_error_unauthorized)
                404 -> context.getString(R.string.pantry_error_not_found)
                500 -> context.getString(R.string.pantry_error_server)
                else -> context.getString(R.string.pantry_error_unexpected, httpException.code())
            }
            DeletePantryItemResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.pantry_error_connection)
            DeletePantryItemResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.pantry_error_generic, exception.message ?: "")
            DeletePantryItemResult.Error(message)
        }
    }
}

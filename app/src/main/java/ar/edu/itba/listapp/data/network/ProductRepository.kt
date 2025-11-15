package ar.edu.itba.listapp.data.network

import android.content.Context
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.data.model.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

// Result types for Categories
sealed interface CreateCategoryResult {
    data class Success(val category: Category) : CreateCategoryResult
    data class Error(val message: String) : CreateCategoryResult
}

sealed interface GetCategoriesResult {
    data class Success(val categories: List<Category>, val pagination: Pagination) : GetCategoriesResult
    data class Error(val message: String) : GetCategoriesResult
}

sealed interface GetCategoryResult {
    data class Success(val category: Category) : GetCategoryResult
    data class Error(val message: String) : GetCategoryResult
}

sealed interface UpdateCategoryResult {
    data class Success(val category: Category) : UpdateCategoryResult
    data class Error(val message: String) : UpdateCategoryResult
}

sealed interface DeleteCategoryResult {
    data object Success : DeleteCategoryResult
    data class Error(val message: String) : DeleteCategoryResult
}

// Result types for Products
sealed interface CreateProductResult {
    data class Success(val product: Product) : CreateProductResult
    data class Error(val message: String) : CreateProductResult
}

sealed interface GetProductsResult {
    data class Success(val products: List<Product>, val pagination: Pagination) : GetProductsResult
    data class Error(val message: String) : GetProductsResult
}

sealed interface GetProductResult {
    data class Success(val product: Product) : GetProductResult
    data class Error(val message: String) : GetProductResult
}

sealed interface UpdateProductResult {
    data class Success(/* no payload needed */ val ok: Boolean = true) : UpdateProductResult
    data class Error(val message: String) : UpdateProductResult
}

sealed interface DeleteProductResult {
    data object Success : DeleteProductResult
    data class Error(val message: String) : DeleteProductResult
}

class ProductRepository(
    private val context: Context,
    private val service: ProductService = NetworkModule.productService,
    private val sessionManager: SessionManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    init {
        // Provide token on each request
        NetworkModule.setAuthTokenProvider { sessionManager.loadAuthToken() }
    }

    // Category operations
    suspend fun createCategory(name: String, metadata: Map<String, String> = emptyMap()): CreateCategoryResult =
        withContext(dispatcher) {
            try {
                val request = CreateCategoryRequest(name, metadata)
                val category = service.createCategory(request)
                CreateCategoryResult.Success(category)
            } catch (httpException: HttpException) {
                val message = when (httpException.code()) {
                    400 -> context.getString(R.string.category_error_bad_request)
                    401 -> context.getString(R.string.category_error_unauthorized)
                    409 -> context.getString(R.string.category_error_conflict)
                    500 -> context.getString(R.string.category_error_server)
                    else -> context.getString(R.string.category_error_unexpected, httpException.code())
                }
                CreateCategoryResult.Error(message)
            } catch (ioException: IOException) {
                val message = context.getString(R.string.category_error_connection)
                CreateCategoryResult.Error(message)
            } catch (exception: Exception) {
                val message = context.getString(R.string.category_error_generic, exception.message ?: "")
                CreateCategoryResult.Error(message)
            }
        }

    suspend fun getCategories(
        name: String? = null,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String = "createdAt",
        order: String = "ASC"
    ): GetCategoriesResult = withContext(dispatcher) {
        try {
            val response = service.getCategories(name, page, perPage, sortBy, order)
            GetCategoriesResult.Success(response.data, response.pagination)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.category_error_bad_request)
                401 -> context.getString(R.string.category_error_unauthorized)
                500 -> context.getString(R.string.category_error_server)
                else -> context.getString(R.string.category_error_unexpected, httpException.code())
            }
            GetCategoriesResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.category_error_connection)
            GetCategoriesResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.category_error_generic, exception.message ?: "")
            GetCategoriesResult.Error(message)
        }
    }

    suspend fun getCategory(id: Long): GetCategoryResult = withContext(dispatcher) {
        try {
            val category = service.getCategory(id)
            GetCategoryResult.Success(category)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.category_error_bad_request)
                401 -> context.getString(R.string.category_error_unauthorized)
                404 -> context.getString(R.string.category_error_not_found)
                500 -> context.getString(R.string.category_error_server)
                else -> context.getString(R.string.category_error_unexpected, httpException.code())
            }
            GetCategoryResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.category_error_connection)
            GetCategoryResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.category_error_generic, exception.message ?: "")
            GetCategoryResult.Error(message)
        }
    }

    suspend fun updateCategory(
        id: Long,
        name: String,
        metadata: Map<String, String> = emptyMap()
    ): UpdateCategoryResult = withContext(dispatcher) {
        try {
            val request = UpdateCategoryRequest(name, metadata)
            val category = service.updateCategory(id, request)
            UpdateCategoryResult.Success(category)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.category_error_bad_request)
                401 -> context.getString(R.string.category_error_unauthorized)
                404 -> context.getString(R.string.category_error_not_found)
                409 -> context.getString(R.string.category_error_conflict)
                500 -> context.getString(R.string.category_error_server)
                else -> context.getString(R.string.category_error_unexpected, httpException.code())
            }
            UpdateCategoryResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.category_error_connection)
            UpdateCategoryResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.category_error_generic, exception.message ?: "")
            UpdateCategoryResult.Error(message)
        }
    }

    suspend fun deleteCategory(id: Long): DeleteCategoryResult = withContext(dispatcher) {
        try {
            service.deleteCategory(id)
            DeleteCategoryResult.Success
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.category_error_bad_request)
                401 -> context.getString(R.string.category_error_unauthorized)
                404 -> context.getString(R.string.category_error_not_found)
                500 -> context.getString(R.string.category_error_server)
                else -> context.getString(R.string.category_error_unexpected, httpException.code())
            }
            DeleteCategoryResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.category_error_connection)
            DeleteCategoryResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.category_error_generic, exception.message ?: "")
            DeleteCategoryResult.Error(message)
        }
    }

    // Product operations
    suspend fun createProduct(
        name: String,
        categoryId: Long,
        metadata: Map<String, String> = emptyMap()
    ): CreateProductResult = withContext(dispatcher) {
        try {
            val request = CreateProductRequest(name, CategoryReference(categoryId), metadata)
            val product = service.createProduct(request)
            CreateProductResult.Success(product)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.product_error_bad_request)
                401 -> context.getString(R.string.product_error_unauthorized)
                409 -> context.getString(R.string.product_error_conflict)
                500 -> context.getString(R.string.product_error_server)
                else -> context.getString(R.string.product_error_unexpected, httpException.code())
            }
            CreateProductResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.product_error_connection)
            CreateProductResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.product_error_generic, exception.message ?: "")
            CreateProductResult.Error(message)
        }
    }

    suspend fun getProducts(
        name: String? = null,
        categoryId: Long? = null,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String = "name",
        order: String = "ASC"
    ): GetProductsResult = withContext(dispatcher) {
        try {
            val response = service.getProducts(name, categoryId, page, perPage, sortBy, order)
            GetProductsResult.Success(response.data, response.pagination)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.product_error_bad_request)
                401 -> context.getString(R.string.product_error_unauthorized)
                500 -> context.getString(R.string.product_error_server)
                else -> context.getString(R.string.product_error_unexpected, httpException.code())
            }
            GetProductsResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.product_error_connection)
            GetProductsResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.product_error_generic, exception.message ?: "")
            GetProductsResult.Error(message)
        }
    }

    suspend fun getProduct(id: Long): GetProductResult = withContext(dispatcher) {
        try {
            val product = service.getProduct(id)
            GetProductResult.Success(product)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.product_error_bad_request)
                401 -> context.getString(R.string.product_error_unauthorized)
                404 -> context.getString(R.string.product_error_not_found)
                500 -> context.getString(R.string.product_error_server)
                else -> context.getString(R.string.product_error_unexpected, httpException.code())
            }
            GetProductResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.product_error_connection)
            GetProductResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.product_error_generic, exception.message ?: "")
            GetProductResult.Error(message)
        }
    }

    suspend fun updateProduct(
        id: Long,
        name: String,
        categoryId: Long,
        metadata: Map<String, String> = emptyMap()
    ): UpdateProductResult = withContext(dispatcher) {
        try {
            val request = UpdateProductRequest(name, CategoryReference(categoryId), metadata)
            service.updateProduct(id, request)
            UpdateProductResult.Success()
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.product_error_bad_request)
                401 -> context.getString(R.string.product_error_unauthorized)
                404 -> context.getString(R.string.product_error_not_found)
                409 -> context.getString(R.string.product_error_conflict)
                500 -> context.getString(R.string.product_error_server)
                else -> context.getString(R.string.product_error_unexpected, httpException.code())
            }
            UpdateProductResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.product_error_connection)
            UpdateProductResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.product_error_generic, exception.message ?: "")
            UpdateProductResult.Error(message)
        }
    }

    suspend fun deleteProduct(id: Long): DeleteProductResult = withContext(dispatcher) {
        try {
            service.deleteProduct(id)
            DeleteProductResult.Success
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.product_error_bad_request)
                401 -> context.getString(R.string.product_error_unauthorized)
                404 -> context.getString(R.string.product_error_not_found)
                500 -> context.getString(R.string.product_error_server)
                else -> context.getString(R.string.product_error_unexpected, httpException.code())
            }
            DeleteProductResult.Error(message)
        } catch (ioException: IOException) {
            val message = context.getString(R.string.product_error_connection)
            DeleteProductResult.Error(message)
        } catch (exception: Exception) {
            val message = context.getString(R.string.product_error_generic, exception.message ?: "")
            DeleteProductResult.Error(message)
        }
    }
}

package ar.edu.itba.listapp.data.network

import ar.edu.itba.listapp.data.model.*
import retrofit2.http.*

interface ProductService {

    // Products endpoints
    @POST("products")
    suspend fun createProduct(@Body body: CreateProductRequest): Product

    @GET("products")
    suspend fun getProducts(
        @Query("name") name: String? = null,
        @Query("category_id") categoryId: Long? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("sort_by") sortBy: String = "name",
        @Query("order") order: String = "ASC"
    ): ProductsResponse

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Long): Product

    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body body: UpdateProductRequest
    ): Unit

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Long)

    // Categories endpoints
    @POST("categories")
    suspend fun createCategory(@Body body: CreateCategoryRequest): Category

    @GET("categories")
    suspend fun getCategories(
        @Query("name") name: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("sort_by") sortBy: String = "createdAt",
        @Query("order") order: String = "ASC"
    ): CategoriesResponse

    @GET("categories/{id}")
    suspend fun getCategory(@Path("id") id: Long): Category

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Long,
        @Body body: UpdateCategoryRequest
    ): Category

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long)
}

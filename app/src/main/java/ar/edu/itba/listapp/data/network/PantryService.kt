package ar.edu.itba.listapp.data.network

import ar.edu.itba.listapp.data.model.*
import retrofit2.http.*

interface PantryService {

    // Pantries endpoints
    @POST("pantries")
    suspend fun createPantry(@Body body: CreatePantryRequest): Pantry

    @GET("pantries")
    suspend fun getPantries(
        @Query("owner") owner: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("sort_by") sortBy: String = "createdAt",
        @Query("order") order: String = "ASC"
    ): PantriesResponse

    @GET("pantries/{id}")
    suspend fun getPantry(@Path("id") id: Long): Pantry

    @PUT("pantries/{id}")
    suspend fun updatePantry(
        @Path("id") id: Long,
        @Body body: UpdatePantryRequest
    ): Pantry

    @DELETE("pantries/{id}")
    suspend fun deletePantry(@Path("id") id: Long)

    // Pantry Items endpoints
    @POST("pantries/{id}/items")
    suspend fun addPantryItem(
        @Path("id") pantryId: Long,
        @Body body: CreatePantryItemRequest
    ): PantryItem

    @GET("pantries/{id}/items")
    suspend fun getPantryItems(
        @Path("id") pantryId: Long,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("sort_by") sortBy: String? = null,
        @Query("order") order: String = "ASC",
        @Query("search") search: String? = null,
        @Query("category_id") categoryId: Long? = null
    ): PantryItemsResponse

    @PUT("pantries/{id}/items/{item_id}")
    suspend fun updatePantryItem(
        @Path("id") pantryId: Long,
        @Path("item_id") itemId: Long,
        @Body body: UpdatePantryItemRequest
    ): PantryItem

    @DELETE("pantries/{id}/items/{item_id}")
    suspend fun deletePantryItem(
        @Path("id") pantryId: Long,
        @Path("item_id") itemId: Long
    )
}


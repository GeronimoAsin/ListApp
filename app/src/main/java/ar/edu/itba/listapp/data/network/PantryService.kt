package ar.edu.itba.listapp.data.network

import ar.edu.itba.listapp.data.model.*
import retrofit2.http.*

interface PantryService {

    // Pantries endpoints
    @GET("pantries")
    suspend fun getPantries(
        @Query("owner") owner: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("sort_by") sortBy: String? = null,
        @Query("order") order: String? = null
    ): PantriesResponse

    @POST("pantries")
    suspend fun createPantry(@Body body: CreatePantryRequest): Pantry

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
    @GET("pantries/{pantryId}/items")
    suspend fun getPantryItems(
        @Path("pantryId") pantryId: Long,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("sort_by") sortBy: String = "createdAt",
        @Query("order") order: String = "ASC"
    ): PantryItemsResponse

    @POST("pantries/{pantryId}/items")
    suspend fun addPantryItem(
        @Path("pantryId") pantryId: Long,
        @Body body: CreatePantryItemRequest
    ): PantryItem

    @GET("pantries/{pantryId}/items/{itemId}")
    suspend fun getPantryItem(
        @Path("pantryId") pantryId: Long,
        @Path("itemId") itemId: Long
    ): PantryItem

    @PUT("pantries/{pantryId}/items/{itemId}")
    suspend fun updatePantryItem(
        @Path("pantryId") pantryId: Long,
        @Path("itemId") itemId: Long,
        @Body body: UpdatePantryItemRequest
    ): PantryItem

    @DELETE("pantries/{pantryId}/items/{itemId}")
    suspend fun deletePantryItem(
        @Path("pantryId") pantryId: Long,
        @Path("itemId") itemId: Long
    )
}


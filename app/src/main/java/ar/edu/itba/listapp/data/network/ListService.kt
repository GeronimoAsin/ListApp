package ar.edu.itba.listapp.data.network

import ar.edu.itba.listapp.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ListService {

    // Shopping Lists endpoints
    @GET("shopping-lists")
    suspend fun getShoppingLists(
        @Query("owner") owner: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("sort_by") sortBy: String? = null,
        @Query("order") order: String? = null
    ): ShoppingListsResponse

    @POST("shopping-lists")
    suspend fun createShoppingList(@Body body: CreateShoppingListRequest): ShoppingList

    @GET("shopping-lists/{id}")
    suspend fun getShoppingList(@Path("id") id: Long): ShoppingList

    @PUT("shopping-lists/{id}")
    suspend fun updateShoppingList(
        @Path("id") id: Long,
        @Body body: UpdateShoppingListRequest
    ): ShoppingList

    @DELETE("shopping-lists/{id}")
    suspend fun deleteShoppingList(@Path("id") id: Long)

    @POST("shopping-lists/{id}/share")
    suspend fun shareShoppingList(
        @Path("id") id: Long,
        @Body body: ShareShoppingListRequest
    ): Owner

    @GET("shopping-lists/{id}/shared-users")
    suspend fun getSharedUsers(@Path("id") id: Long): List<Owner>

    @DELETE("shopping-lists/{id}/share/{user_id}")
    suspend fun unshareShoppingList(
        @Path("id") id: Long,
        @Path("user_id") userId: Long
    )

    // Shopping List Items endpoints
    @GET("shopping-lists/{listId}/items")
    suspend fun getShoppingListItems(
        @Path("listId") listId: Long,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("sort_by") sortBy: String = "createdAt",
        @Query("order") order: String = "ASC"
    ): ShoppingListItemsResponse

    @POST("shopping-lists/{listId}/items")
    suspend fun addShoppingListItem(
        @Path("listId") listId: Long,
        @Body body: CreateShoppingListItemRequest
    ): Response<Unit>

    @GET("shopping-lists/{listId}/items/{itemId}")
    suspend fun getShoppingListItem(
        @Path("listId") listId: Long,
        @Path("itemId") itemId: Long
    ): ShoppingListItem

    @PUT("shopping-lists/{listId}/items/{itemId}")
    suspend fun updateShoppingListItem(
        @Path("listId") listId: Long,
        @Path("itemId") itemId: Long,
        @Body body: UpdateShoppingListItemRequest
    ): ShoppingListItem

    @DELETE("shopping-lists/{listId}/items/{itemId}")
    suspend fun deleteShoppingListItem(
        @Path("listId") listId: Long,
        @Path("itemId") itemId: Long
    )

    @PATCH("shopping-lists/{listId}/items/{itemId}")
    suspend fun toggleItemPurchased(
        @Path("listId") listId: Long,
        @Path("itemId") itemId: Long,
        @Body body: TogglePurchasedRequest
    ): ShoppingListItem
}

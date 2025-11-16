package ar.edu.itba.listapp.data.model

import kotlinx.serialization.Serializable

// Pantry models
@Serializable
data class Pantry(
    val id: Long,
    val name: String,
    val metadata: Map<String, String>? = null,
    val createdAt: String,
    val updatedAt: String,
    val owner: Owner? = null,
    val sharedWith: List<Owner> = emptyList()
)

@Serializable
data class CreatePantryRequest(
    val name: String,
    val metadata: Map<String, String>? = null
)

@Serializable
data class UpdatePantryRequest(
    val name: String,
    val metadata: Map<String, String>? = null
)

@Serializable
data class PantriesResponse(
    val data: List<Pantry>,
    val pagination: Pagination
)

// Pantry Item models
@Serializable
data class PantryItem(
    val id: Long,
    val quantity: Double,
    val unit: String? = null,
    val metadata: Map<String, String>? = null,
    val product: Product,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ProductReference(
    val id: Long
)

@Serializable
data class CreatePantryItemRequest(
    val product: ProductReference,
    val quantity: Double,
    val unit: String? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class UpdatePantryItemRequest(
    val quantity: Double,
    val unit: String? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class PantryItemsResponse(
    val data: List<PantryItem>,
    val pagination: Pagination
)


package ar.edu.itba.listapp.data.model

import kotlinx.serialization.Serializable

// Category models
@Serializable
data class Category(
    val id: Long,
    val name: String,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val owner: Owner? = null
)

@Serializable
data class Owner(
    val id: Long,
    val name: String,
    val surname: String,
    val email: String,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class UpdateCategoryRequest(
    val name: String,
    val metadata: Map<String, String> = emptyMap()
)

// Product models
@Serializable
data class Product(
    val id: Long,
    val name: String,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: String,
    val updatedAt: String,
    val category: Category
)

@Serializable
data class CategoryReference(
    val id: Long
)

@Serializable
data class CreateProductRequest(
    val name: String,
    val category: CategoryReference,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class UpdateProductRequest(
    val name: String,
    val category: CategoryReference,
    val metadata: Map<String, String> = emptyMap()
)

// Pagination models
@Serializable
data class Pagination(
    val total: Int,
    val page: Int,
    val per_page: Int,
    val total_pages: Int,
    val has_next: Boolean,
    val has_prev: Boolean
)

@Serializable
data class ProductsResponse(
    val data: List<Product>,
    val pagination: Pagination
)

@Serializable
data class CategoriesResponse(
    val data: List<Category>,
    val pagination: Pagination
)


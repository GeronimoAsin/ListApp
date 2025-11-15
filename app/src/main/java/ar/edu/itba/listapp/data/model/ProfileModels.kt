package ar.edu.itba.listapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: Int,
    val name: String,
    val surname: String,
    val email: String,
    val metadata: Map<String, String>,
    val updatedAt: String,
    val createdAt: String
)


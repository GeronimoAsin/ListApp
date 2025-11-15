package ar.edu.itba.listapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val surname: String,
    val email: String,
    val password: String,
    val metadata: RegisterMetadata
)

@Serializable
data class RegisterMetadata(
    val nickname: String
)

@Serializable
data class RegisterResponse(
    val id: Int,
    val name: String,
    val surname: String,
    val email: String,
    val metadata: RegisterMetadata,
    val updatedAt: String,
    val createdAt: String
)


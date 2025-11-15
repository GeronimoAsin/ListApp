package ar.edu.itba.listapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VerifyAccountRequest(
    val code: String
)

@Serializable
data class VerifyAccountResponse(
    val id: Int,
    val name: String,
    val surname: String,
    val email: String,
    val metadata: Map<String, String>,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ResendVerificationResponse(
    val code: String
)


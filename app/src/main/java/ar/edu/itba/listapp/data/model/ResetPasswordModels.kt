package ar.edu.itba.listapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(
    val code: String,
    val password: String
)

@Serializable
data class ResetPasswordResponse(
    val message: String? = null
)


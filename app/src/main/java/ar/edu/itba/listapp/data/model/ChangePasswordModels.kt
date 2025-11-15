package ar.edu.itba.listapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class ChangePasswordResponse(
    val message: String? = null
)


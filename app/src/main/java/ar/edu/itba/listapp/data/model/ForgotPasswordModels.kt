package ar.edu.itba.listapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordResponse(
    val message: String? = null
)


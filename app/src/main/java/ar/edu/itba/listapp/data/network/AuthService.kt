package ar.edu.itba.listapp.data.network

import ar.edu.itba.listapp.data.model.LoginRequest
import ar.edu.itba.listapp.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("users/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse
}


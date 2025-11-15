package ar.edu.itba.listapp.data.network

import ar.edu.itba.listapp.data.model.LoginRequest
import ar.edu.itba.listapp.data.model.LoginResponse
import ar.edu.itba.listapp.data.model.RegisterRequest
import ar.edu.itba.listapp.data.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("users/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("users/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse
}


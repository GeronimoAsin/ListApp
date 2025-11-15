package ar.edu.itba.listapp.data.network

import ar.edu.itba.listapp.data.model.ForgotPasswordResponse
import ar.edu.itba.listapp.data.model.LoginRequest
import ar.edu.itba.listapp.data.model.LoginResponse
import ar.edu.itba.listapp.data.model.RegisterRequest
import ar.edu.itba.listapp.data.model.RegisterResponse
import ar.edu.itba.listapp.data.model.ResendVerificationResponse
import ar.edu.itba.listapp.data.model.ResetPasswordRequest
import ar.edu.itba.listapp.data.model.ResetPasswordResponse
import ar.edu.itba.listapp.data.model.VerifyAccountRequest
import ar.edu.itba.listapp.data.model.VerifyAccountResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {
    @POST("users/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("users/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST("users/verify-account")
    suspend fun verifyAccount(@Body body: VerifyAccountRequest): VerifyAccountResponse

    @POST("users/send-verification")
    suspend fun resendVerification(@Query("email") email: String): ResendVerificationResponse

    @POST("users/forgot-password")
    suspend fun forgotPassword(@Query("email") email: String): ForgotPasswordResponse

    @POST("users/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): ResetPasswordResponse
}


package ar.edu.itba.listapp.data.network

import android.content.Context
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.data.model.LoginRequest
import ar.edu.itba.listapp.data.model.RegisterMetadata
import ar.edu.itba.listapp.data.model.RegisterRequest
import ar.edu.itba.listapp.data.model.VerifyAccountRequest
import ar.edu.itba.listapp.data.model.ResetPasswordRequest
import ar.edu.itba.listapp.data.model.ChangePasswordRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed interface LoginResult {
    data class Success(val token: String) : LoginResult
    data class Error(val message: String) : LoginResult
}

sealed interface RegisterResult {
    data class Success(val email: String) : RegisterResult
    data class Error(val message: String) : RegisterResult
}

sealed interface VerifyAccountResult {
    data class Success(val email: String) : VerifyAccountResult
    data class Error(val message: String) : VerifyAccountResult
}

sealed interface ResendVerificationResult {
    data object Success : ResendVerificationResult
    data class Error(val message: String) : ResendVerificationResult
}

sealed interface ForgotPasswordResult {
    data class Success(val email: String) : ForgotPasswordResult
    data class Error(val message: String) : ForgotPasswordResult
}

sealed interface ResetPasswordResult {
    data object Success : ResetPasswordResult
    data class Error(val message: String) : ResetPasswordResult
}

sealed interface ProfileResult {
    data class Success(val profile: ar.edu.itba.listapp.data.model.UserProfile) : ProfileResult
    data class Error(val message: String) : ProfileResult
}

sealed interface LogoutResult {
    data object Success : LogoutResult
    data class Error(val message: String) : LogoutResult
}

sealed interface ChangePasswordResult {
    data object Success : ChangePasswordResult
    data class Error(val message: String) : ChangePasswordResult
}

class AuthRepository(
    private val context: Context,
    private val service: AuthService = NetworkModule.authService,
    private val sessionManager: SessionManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun login(email: String, password: String): LoginResult = withContext(dispatcher) {
        try {
            val response = service.login(LoginRequest(email, password))
            sessionManager.saveAuthToken(response.token)
            LoginResult.Success(response.token)
        } catch (httpException: HttpException) {
            val errorBody = httpException.response()?.errorBody()?.string()
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.error_bad_request)
                401 -> {
                    if (errorBody != null && errorBody.isNotEmpty()) {
                        context.getString(R.string.error_unauthorized_detail, errorBody)
                    } else {
                        context.getString(R.string.error_unauthorized)
                    }
                }
                500 -> context.getString(R.string.error_server)
                else -> {
                    if (errorBody != null && errorBody.isNotEmpty()) {
                        context.getString(R.string.error_unexpected_detail, httpException.code(), errorBody)
                    } else {
                        context.getString(R.string.error_unexpected, httpException.code())
                    }
                }
            }
            LoginResult.Error(message)
        } catch (ioException: IOException) {
            val message = ioException.message?.let {
                context.getString(R.string.error_connection, it)
            } ?: context.getString(R.string.error_connection_default)
            LoginResult.Error(message)
        } catch (exception: Exception) {
            val message = exception.message?.let {
                context.getString(R.string.error_generic, it)
            } ?: context.getString(R.string.error_generic_default)
            LoginResult.Error(message)
        }
    }

    suspend fun register(
        name: String,
        surname: String,
        email: String,
        password: String,
        nickname: String
    ): RegisterResult = withContext(dispatcher) {
        try {
            val request = RegisterRequest(
                name = name,
                surname = surname,
                email = email,
                password = password,
                metadata = RegisterMetadata(nickname = nickname)
            )
            val response = service.register(request)
            RegisterResult.Success(response.email)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.register_error_bad_request)
                500 -> context.getString(R.string.register_error_server)
                else -> context.getString(R.string.register_error_unexpected, httpException.code())
            }
            RegisterResult.Error(message)
        } catch (ioException: IOException) {
            val message = ioException.message?.let {
                context.getString(R.string.register_error_connection, it)
            } ?: context.getString(R.string.register_error_connection_default)
            RegisterResult.Error(message)
        } catch (exception: Exception) {
            val message = exception.message?.let {
                context.getString(R.string.register_error_generic, it)
            } ?: context.getString(R.string.register_error_generic_default)
            RegisterResult.Error(message)
        }
    }

    suspend fun verifyAccount(code: String): VerifyAccountResult = withContext(dispatcher) {
        try {
            val request = VerifyAccountRequest(code = code)
            val response = service.verifyAccount(request)
            VerifyAccountResult.Success(response.email)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.verify_error_bad_request)
                409 -> context.getString(R.string.verify_error_conflict)
                500 -> context.getString(R.string.verify_error_server)
                else -> context.getString(R.string.verify_error_unexpected, httpException.code())
            }
            VerifyAccountResult.Error(message)
        } catch (ioException: IOException) {
            val message = ioException.message?.let {
                context.getString(R.string.verify_error_connection, it)
            } ?: context.getString(R.string.verify_error_connection_default)
            VerifyAccountResult.Error(message)
        } catch (exception: Exception) {
            val message = exception.message?.let {
                context.getString(R.string.verify_error_generic, it)
            } ?: context.getString(R.string.verify_error_generic_default)
            VerifyAccountResult.Error(message)
        }
    }

    suspend fun resendVerification(email: String): ResendVerificationResult = withContext(dispatcher) {
        try {
            service.resendVerification(email)
            ResendVerificationResult.Success
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.resend_error_bad_request)
                404 -> context.getString(R.string.resend_error_not_found)
                409 -> context.getString(R.string.resend_error_conflict)
                500 -> context.getString(R.string.resend_error_server)
                else -> context.getString(R.string.resend_error_unexpected, httpException.code())
            }
            ResendVerificationResult.Error(message)
        } catch (ioException: IOException) {
            val message = ioException.message?.let {
                context.getString(R.string.resend_error_connection, it)
            } ?: context.getString(R.string.resend_error_connection_default)
            ResendVerificationResult.Error(message)
        } catch (exception: Exception) {
            val message = exception.message?.let {
                context.getString(R.string.resend_error_generic, it)
            } ?: context.getString(R.string.resend_error_generic_default)
            ResendVerificationResult.Error(message)
        }
    }

    suspend fun forgotPassword(email: String): ForgotPasswordResult = withContext(dispatcher) {
        try {
            service.forgotPassword(email)
            ForgotPasswordResult.Success(email)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.forgot_password_error_bad_request)
                404 -> context.getString(R.string.forgot_password_error_not_found)
                500 -> context.getString(R.string.forgot_password_error_server)
                else -> context.getString(R.string.forgot_password_error_unexpected, httpException.code())
            }
            ForgotPasswordResult.Error(message)
        } catch (ioException: IOException) {
            val message = ioException.message?.let {
                context.getString(R.string.forgot_password_error_connection, it)
            } ?: context.getString(R.string.forgot_password_error_connection_default)
            ForgotPasswordResult.Error(message)
        } catch (exception: Exception) {
            val message = exception.message?.let {
                context.getString(R.string.forgot_password_error_generic, it)
            } ?: context.getString(R.string.forgot_password_error_generic_default)
            ForgotPasswordResult.Error(message)
        }
    }

    suspend fun resetPassword(code: String, password: String): ResetPasswordResult = withContext(dispatcher) {
        try {
            val request = ResetPasswordRequest(code = code, password = password)
            service.resetPassword(request)
            ResetPasswordResult.Success
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.reset_password_error_bad_request)
                404 -> context.getString(R.string.reset_password_error_not_found)
                500 -> context.getString(R.string.reset_password_error_server)
                else -> context.getString(R.string.reset_password_error_unexpected, httpException.code())
            }
            ResetPasswordResult.Error(message)
        } catch (ioException: IOException) {
            val message = ioException.message?.let {
                context.getString(R.string.reset_password_error_connection, it)
            } ?: context.getString(R.string.reset_password_error_connection_default)
            ResetPasswordResult.Error(message)
        } catch (exception: Exception) {
            val message = exception.message?.let {
                context.getString(R.string.reset_password_error_generic, it)
            } ?: context.getString(R.string.reset_password_error_generic_default)
            ResetPasswordResult.Error(message)
        }
    }

    suspend fun getProfile(): ProfileResult = withContext(dispatcher) {
        try {
            val profile = service.getProfile()
            ProfileResult.Success(profile)
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                401 -> context.getString(R.string.profile_error_unauthorized)
                404 -> context.getString(R.string.profile_error_not_found)
                500 -> context.getString(R.string.profile_error_server)
                else -> context.getString(R.string.profile_error_unexpected, httpException.code())
            }
            ProfileResult.Error(message)
        } catch (ioException: IOException) {
            val message = ioException.message?.let {
                context.getString(R.string.profile_error_connection, it)
            } ?: context.getString(R.string.profile_error_connection_default)
            ProfileResult.Error(message)
        } catch (exception: Exception) {
            val message = exception.message?.let {
                context.getString(R.string.profile_error_generic, it)
            } ?: context.getString(R.string.profile_error_generic_default)
            ProfileResult.Error(message)
        }
    }

    suspend fun logout(): LogoutResult = withContext(dispatcher) {
        try {
            service.logout()
            sessionManager.removeAuthToken()
            LogoutResult.Success
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                401 -> context.getString(R.string.logout_error_unauthorized)
                500 -> context.getString(R.string.logout_error_server)
                else -> context.getString(R.string.logout_error_unexpected, httpException.code())
            }
            sessionManager.removeAuthToken()
            LogoutResult.Error(message)
        } catch (ioException: IOException) {
            val message = ioException.message?.let {
                context.getString(R.string.logout_error_connection, it)
            } ?: context.getString(R.string.logout_error_connection_default)
            sessionManager.removeAuthToken()
            LogoutResult.Error(message)
        } catch (exception: Exception) {
            val message = exception.message?.let {
                context.getString(R.string.logout_error_generic, it)
            } ?: context.getString(R.string.logout_error_generic_default)
            sessionManager.removeAuthToken()
            LogoutResult.Error(message)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): ChangePasswordResult = withContext(dispatcher) {
        try {
            val request = ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
            service.changePassword(request)
            ChangePasswordResult.Success
        } catch (httpException: HttpException) {
            val message = when (httpException.code()) {
                400 -> context.getString(R.string.change_password_error_bad_request)
                401 -> context.getString(R.string.change_password_error_unauthorized)
                500 -> context.getString(R.string.change_password_error_server)
                else -> context.getString(R.string.change_password_error_unexpected, httpException.code())
            }
            ChangePasswordResult.Error(message)
        } catch (ioException: IOException) {
            val message = ioException.message?.let {
                context.getString(R.string.change_password_error_connection, it)
            } ?: context.getString(R.string.change_password_error_connection_default)
            ChangePasswordResult.Error(message)
        } catch (exception: Exception) {
            val message = exception.message?.let {
                context.getString(R.string.change_password_error_generic, it)
            } ?: context.getString(R.string.change_password_error_generic_default)
            ChangePasswordResult.Error(message)
        }
    }

    suspend fun updateProfile(name: String, surname: String, metadata: Map<String, String>): Result<Unit> = withContext(dispatcher) {
        try {
            service.updateProfile(ar.edu.itba.listapp.data.model.UpdateProfileRequest(name, surname, metadata))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

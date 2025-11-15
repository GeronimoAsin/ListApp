package ar.edu.itba.listapp.data.network

import android.content.Context
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.data.model.LoginRequest
import ar.edu.itba.listapp.data.model.RegisterMetadata
import ar.edu.itba.listapp.data.model.RegisterRequest
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
}

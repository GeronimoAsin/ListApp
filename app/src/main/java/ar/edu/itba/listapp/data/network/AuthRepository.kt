package ar.edu.itba.listapp.data.network

import ar.edu.itba.listapp.data.model.LoginRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed interface LoginResult {
    data class Success(val token: String) : LoginResult
    data class Error(val message: String) : LoginResult
}

class AuthRepository(
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
            val message = when (httpException.code()) {
                400 -> "Solicitud inválida. Verificá los datos ingresados."
                401 -> "Credenciales incorrectas."
                500 -> "Error del servidor. Intentá más tarde."
                else -> "Error inesperado (${httpException.code()})."
            }
            LoginResult.Error(message)
        } catch (ioException: IOException) {
            val message = "Error de conexión: ${ioException.message ?: "Verificá tu red y que el servidor esté corriendo."}"
            LoginResult.Error(message)
        } catch (exception: Exception) {
            val message = "Error inesperado: ${exception.message ?: "Intentá nuevamente."}"
            LoginResult.Error(message)
        }
    }
}


package ar.edu.itba.listapp.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.data.network.AuthRepository
import ar.edu.itba.listapp.data.network.RegisterResult
import ar.edu.itba.listapp.data.network.SessionManager
import ar.edu.itba.listapp.ui.theme.ListappTheme
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(
        context = application,
        sessionManager = SessionManager(application)
    )

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun onFirstNameChange(value: String) {
        uiState = uiState.copy(firstName = value)
    }

    fun onLastNameChange(value: String) {
        uiState = uiState.copy(lastName = value)
    }

    fun onNicknameChange(value: String) {
        uiState = uiState.copy(nickname = value)
    }

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value)
    }

    fun onRepeatPasswordChange(value: String) {
        uiState = uiState.copy(repeatPassword = value)
    }

    fun register(onRegisterSuccess: (String) -> Unit) {
        if (uiState.isLoading) return

        // Validación básica
        if (uiState.password != uiState.repeatPassword) {
            uiState = uiState.copy(errorMessage = getApplication<Application>().getString(R.string.register_error_password_mismatch))
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = repository.register(
                name = uiState.firstName,
                surname = uiState.lastName,
                email = uiState.email,
                password = uiState.password,
                nickname = uiState.nickname
            )) {
                is RegisterResult.Success -> {
                    uiState = uiState.copy(isLoading = false)
                    onRegisterSuccess(result.email)
                }
                is RegisterResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}

data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = "",
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@Composable
fun RegisterScreen(
    padding: PaddingValues = PaddingValues(),
    onLoginClick: () -> Unit,
    onRegisterSuccess: (String) -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(74.dp)) }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onLoginClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button_description)
                    )
                }
                Spacer(modifier = Modifier.width(40.dp))
                Text(
                    text = stringResource(R.string.register_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 32.sp
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        item {
            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.firstName,
                    onValueChange = { viewModel.onFirstNameChange(it) },
                    label = { Text(stringResource(R.string.first_name_placeholder)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFCFE8B7),
                        unfocusedBorderColor = Color(0xFFCFE8B7),
                        focusedContainerColor = Color(0xFFCFE8B7),
                        unfocusedContainerColor = Color(0xFFCFE8B7)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = uiState.lastName,
                    onValueChange = { viewModel.onLastNameChange(it) },
                    label = { Text(stringResource(R.string.last_name_placeholder)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFCFE8B7),
                        unfocusedBorderColor = Color(0xFFCFE8B7),
                        focusedContainerColor = Color(0xFFCFE8B7),
                        unfocusedContainerColor = Color(0xFFCFE8B7)
                    )
                )
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            OutlinedTextField(
                value = uiState.nickname,
                onValueChange = { viewModel.onNicknameChange(it) },
                label = { Text(stringResource(R.string.nickname_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFCFE8B7),
                    unfocusedBorderColor = Color(0xFFCFE8B7),
                    focusedContainerColor = Color(0xFFCFE8B7),
                    unfocusedContainerColor = Color(0xFFCFE8B7)
                )
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text(stringResource(R.string.email_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFCFE8B7),
                    unfocusedBorderColor = Color(0xFFCFE8B7),
                    focusedContainerColor = Color(0xFFCFE8B7),
                    unfocusedContainerColor = Color(0xFFCFE8B7)
                )
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text(stringResource(R.string.password_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFCFE8B7),
                    unfocusedBorderColor = Color(0xFFCFE8B7),
                    focusedContainerColor = Color(0xFFCFE8B7),
                    unfocusedContainerColor = Color(0xFFCFE8B7)
                )
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            OutlinedTextField(
                value = uiState.repeatPassword,
                onValueChange = { viewModel.onRepeatPasswordChange(it) },
                label = { Text(stringResource(R.string.repeat_password_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFCFE8B7),
                    unfocusedBorderColor = Color(0xFFCFE8B7),
                    focusedContainerColor = Color(0xFFCFE8B7),
                    unfocusedContainerColor = Color(0xFFCFE8B7)
                )
            )
        }

        item { Spacer(modifier = Modifier.height(64.dp)) }

        item {
            Button(
                onClick = { viewModel.register(onRegisterSuccess) },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(20),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8CC94F))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.register_button),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        item {
            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun RegisterScreenPreview() {
    ListappTheme {
        RegisterScreen(
            padding = PaddingValues(),
            onLoginClick = {},
            onRegisterSuccess = {}
        )
    }
}

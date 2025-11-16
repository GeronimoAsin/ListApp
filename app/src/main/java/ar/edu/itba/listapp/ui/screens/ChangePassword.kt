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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.data.network.AuthRepository
import ar.edu.itba.listapp.data.network.ChangePasswordResult
import ar.edu.itba.listapp.data.network.SessionManager
import ar.edu.itba.listapp.ui.theme.ListappTheme
import kotlinx.coroutines.launch

class ChangePasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(
        context = application,
        sessionManager = SessionManager(application)
    )

    var uiState by mutableStateOf(ChangePasswordUiState())
        private set

    fun onCurrentPasswordChange(value: String) {
        uiState = uiState.copy(currentPassword = value)
    }

    fun onNewPasswordChange(value: String) {
        uiState = uiState.copy(newPassword = value)
    }

    fun onRepeatPasswordChange(value: String) {
        uiState = uiState.copy(repeatPassword = value)
    }

    fun changePassword(onPasswordChanged: () -> Unit) {
        if (uiState.isLoading) return

        if (uiState.newPassword != uiState.repeatPassword) {
            uiState = uiState.copy(
                errorMessage = getApplication<Application>().getString(R.string.change_password_error_password_mismatch)
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = repository.changePassword(uiState.currentPassword, uiState.newPassword)) {
                is ChangePasswordResult.Success -> {
                    uiState = uiState.copy(isLoading = false)
                    onPasswordChanged()
                }
                is ChangePasswordResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val repeatPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@Composable
fun ChangePasswordScreen(
    padding: PaddingValues = PaddingValues(),
    onPasswordChanged: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: ChangePasswordViewModel = viewModel()
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
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button_description)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.change_password_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 42.sp
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        item {
            Text(
                text = stringResource(R.string.change_password_prompt),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        item {
            OutlinedTextField(
                value = uiState.currentPassword,
                onValueChange = { viewModel.onCurrentPasswordChange(it) },
                label = { Text(stringResource(R.string.change_password_current_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
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
                value = uiState.newPassword,
                onValueChange = { viewModel.onNewPasswordChange(it) },
                label = { Text(stringResource(R.string.change_password_new_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
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
                label = { Text(stringResource(R.string.change_password_repeat_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFCFE8B7),
                    unfocusedBorderColor = Color(0xFFCFE8B7),
                    focusedContainerColor = Color(0xFFCFE8B7),
                    unfocusedContainerColor = Color(0xFFCFE8B7)
                )
            )
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        item {
            Button(
                onClick = { viewModel.changePassword(onPasswordChanged) },
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
                        text = stringResource(R.string.change_password_button),
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
fun ChangePasswordScreenPreview() {
    ListappTheme {
        ChangePasswordScreen(
            padding = PaddingValues(),
            onPasswordChanged = {},
            onBackClick = {}
        )
    }
}

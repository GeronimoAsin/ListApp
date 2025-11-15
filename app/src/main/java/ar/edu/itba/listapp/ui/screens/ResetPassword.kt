package ar.edu.itba.listapp.ui.screens

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.data.network.AuthRepository
import ar.edu.itba.listapp.data.network.ResetPasswordResult
import ar.edu.itba.listapp.data.network.SessionManager
import ar.edu.itba.listapp.ui.theme.ListappTheme
import kotlinx.coroutines.launch

class ResetPasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(
        context = application,
        sessionManager = SessionManager(application)
    )

    var uiState by mutableStateOf(ResetPasswordUiState())
        private set

    fun onCodeChange(value: String) {
        uiState = uiState.copy(code = value)
    }

    fun onNewPasswordChange(value: String) {
        uiState = uiState.copy(newPassword = value)
    }

    fun onRepeatPasswordChange(value: String) {
        uiState = uiState.copy(repeatPassword = value)
    }

    fun resetPassword() {
        if (uiState.isLoading) return

        // Validación de contraseñas
        if (uiState.newPassword != uiState.repeatPassword) {
            uiState = uiState.copy(
                errorMessage = getApplication<Application>().getString(R.string.reset_password_error_password_mismatch)
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = repository.resetPassword(uiState.code, uiState.newPassword)) {
                is ResetPasswordResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        successMessage = getApplication<Application>().getString(R.string.reset_password_success),
                        isReset = true
                    )
                }
                is ResetPasswordResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}

data class ResetPasswordUiState(
    val code: String = "",
    val newPassword: String = "",
    val repeatPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isReset: Boolean = false
)

@Composable
fun ResetPasswordScreen(
    padding: PaddingValues,
    onPasswordReset: () -> Unit,
    onBackClick: () -> Unit = {},
    viewModel: ResetPasswordViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    // Navegar a login cuando el reset es exitoso
    LaunchedEffect(uiState.isReset) {
        if (uiState.isReset) {
            onPasswordReset()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(74.dp))

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
                text = stringResource(R.string.reset_password_title),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                lineHeight = 42.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.reset_password_prompt),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.code,
            onValueChange = { viewModel.onCodeChange(it) },
            label = { Text(stringResource(R.string.reset_password_code_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !uiState.isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFCFE8B7),
                unfocusedBorderColor = Color(0xFFCFE8B7),
                focusedContainerColor = Color(0xFFCFE8B7),
                unfocusedContainerColor = Color(0xFFCFE8B7)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.newPassword,
            onValueChange = { viewModel.onNewPasswordChange(it) },
            label = { Text(stringResource(R.string.reset_password_new_password_label)) },
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

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.repeatPassword,
            onValueChange = { viewModel.onRepeatPasswordChange(it) },
            label = { Text(stringResource(R.string.reset_password_repeat_password_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFCFE8B7),
                unfocusedBorderColor = Color(0xFFCFE8B7),
                focusedContainerColor = Color(0xFFCFE8B7),
                unfocusedContainerColor = Color(0xFFCFE8B7)
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.resetPassword() },
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
                    text = stringResource(R.string.reset_password_button),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        uiState.errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        uiState.successMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = Color(0xFF2E7D32),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = clickableResendPart(),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.clickable { /* TODO: Resend code */ }
        )
    }
}

@Composable
private fun clickableResendPart(): AnnotatedString {
    return buildAnnotatedString {
        val prompt = stringResource(R.string.reset_password_resend_prompt)
        val link = stringResource(R.string.reset_password_resend_link)
        append(prompt)
        if (prompt.lastOrNull()?.isWhitespace() != true) {
            append(" ")
        }
        val start = length
        append(link)
        val end = length

        addStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            ),
            start,
            end
        )

        addStringAnnotation(
            tag = "resend_reset",
            annotation = "resend_reset",
            start = start,
            end = end
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResetPasswordScreenPreview() {
    ListappTheme {
        ResetPasswordScreen(
            padding = PaddingValues(),
            onPasswordReset = {}
        )
    }
}

package ar.edu.itba.listapp.ui.screens

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.ui.theme.ListappTheme
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.itba.listapp.data.network.AuthRepository
import ar.edu.itba.listapp.data.network.LoginResult
import ar.edu.itba.listapp.data.network.SessionManager
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(
        context = application,
        sessionManager = SessionManager(application)
    )

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value)
    }

    fun dismissError() {
        uiState = uiState.copy(errorMessage = null)
    }

    fun login(onLoginSuccess: () -> Unit) {
        if (uiState.isLoading) return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = repository.login(uiState.email, uiState.password)) {
                is LoginResult.Success -> {
                    uiState = uiState.copy(isLoading = false)
                    onLoginSuccess()
                }
                is LoginResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@Composable
fun LoginScreen(
    padding: PaddingValues = PaddingValues(),
    onForgotPasswordClick: () -> Unit,
    onVerificationClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(74.dp))

        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(64.dp))

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

        Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = clickablePart(
                stringResource(R.string.forgot_password),
                stringResource(R.string.click_here),
                tag = "forgot"
            ),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.clickable {
                onForgotPasswordClick()
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = clickablePart(
                stringResource(R.string.verification_in_progress),
                stringResource(R.string.click_here),
                tag = "verification"
            ),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.clickable {
                onVerificationClick()
            }
        )

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = { viewModel.login(onLoginSuccess) },
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
                    text = stringResource(R.string.login_button),
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

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = clickablePart(
                stringResource(R.string.no_account_register),
                stringResource(R.string.click_here),
                tag = "register"
            ),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.clickable {
                onRegisterClick()
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun clickablePart(
    text: String,
    clickableText: String,
    tag: String
): AnnotatedString {
    return buildAnnotatedString {
        val start = text.indexOf(clickableText)
        val end = start + clickableText.length

        append(text)

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
            tag = tag,
            annotation = tag,
            start = start,
            end = end
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ListappTheme {
        LoginScreen(
            onForgotPasswordClick = {},
            onVerificationClick = {},
            onRegisterClick = {},
            onLoginSuccess = {}
        )
    }
}
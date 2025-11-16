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
import ar.edu.itba.listapp.data.network.ForgotPasswordResult
import ar.edu.itba.listapp.data.network.SessionManager
import ar.edu.itba.listapp.ui.theme.ListappTheme
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(
        context = application,
        sessionManager = SessionManager(application)
    )

    var uiState by mutableStateOf(ForgotPasswordUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value)
    }

    fun sendCode(onCodeSent: (String) -> Unit) {
        if (uiState.isLoading) return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = repository.forgotPassword(uiState.email)) {
                is ForgotPasswordResult.Success -> {
                    uiState = uiState.copy(isLoading = false)
                    onCodeSent(result.email)
                }
                is ForgotPasswordResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@Composable
fun ForgotPasswordScreen(
    padding: PaddingValues = PaddingValues(),
    onBackToLogin: () -> Unit,
    onCodeSent: (String) -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBackToLogin) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button_description)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.forgot_password_title),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                lineHeight = 50.sp
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = stringResource(R.string.forgot_password_prompt),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(64.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text(stringResource(R.string.email_label)) },
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

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.sendCode(onCodeSent) },
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
                    text = stringResource(R.string.send_code_button),
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

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = clickablePart(
                stringResource(R.string.remembered_password_prompt)
            ),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.clickable {
                onBackToLogin()
            }
        )
    }
}

@Composable
private fun clickablePart(
    text: String
): AnnotatedString {
    return buildAnnotatedString {
        append("$text ")
        val start = this.length
        append(stringResource(R.string.login_here_link))
        val end = this.length

        addStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            ),
            start,
            end
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    ListappTheme {
        ForgotPasswordScreen(onBackToLogin = {}, onCodeSent = {})
    }
}

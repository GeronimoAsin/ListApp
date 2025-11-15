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
import ar.edu.itba.listapp.data.network.ResendVerificationResult
import ar.edu.itba.listapp.data.network.SessionManager
import ar.edu.itba.listapp.data.network.VerifyAccountResult
import ar.edu.itba.listapp.ui.theme.ListappTheme
import kotlinx.coroutines.launch

class VerificationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(
        context = application,
        sessionManager = SessionManager(application)
    )

    var uiState by mutableStateOf(VerificationUiState())
        private set

    private var userEmail: String? = null

    fun setUserEmail(email: String) {
        userEmail = email
    }

    fun onCodeChange(value: String) {
        uiState = uiState.copy(code = value)
    }

    fun verify() {
        if (uiState.isLoading) return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = repository.verifyAccount(uiState.code)) {
                is VerifyAccountResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        successMessage = getApplication<Application>().getString(R.string.verify_success),
                        isVerified = true
                    )
                }
                is VerifyAccountResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun resendCode() {
        val email = userEmail
        if (email == null) {
            uiState = uiState.copy(errorMessage = getApplication<Application>().getString(R.string.resend_error_generic_default))
            return
        }

        if (uiState.isResending) return
        viewModelScope.launch {
            uiState = uiState.copy(isResending = true, errorMessage = null, resendMessage = null)
            when (val result = repository.resendVerification(email)) {
                is ResendVerificationResult.Success -> {
                    uiState = uiState.copy(
                        isResending = false,
                        resendMessage = getApplication<Application>().getString(R.string.resend_success)
                    )
                }
                is ResendVerificationResult.Error -> {
                    uiState = uiState.copy(isResending = false, errorMessage = result.message)
                }
            }
        }
    }
}

data class VerificationUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val resendMessage: String? = null,
    val isVerified: Boolean = false
)

@Composable
fun VerificationScreen(
    padding: PaddingValues,
    onVerified: () -> Unit,
    onBackClick: () -> Unit = {},
    userEmail: String? = null,
    viewModel: VerificationViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    // Establecer el email del usuario si está disponible
    LaunchedEffect(userEmail) {
        userEmail?.let { viewModel.setUserEmail(it) }
    }

    // Navegar cuando la verificación es exitosa
    LaunchedEffect(uiState.isVerified) {
        if (uiState.isVerified) {
            onVerified()
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
                text = stringResource(R.string.verification_title),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 32.sp
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = stringResource(R.string.verification_prompt),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(64.dp))

        OutlinedTextField(
            value = uiState.code,
            onValueChange = { viewModel.onCodeChange(it) },
            label = { Text(stringResource(R.string.code_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
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
                stringResource(R.string.did_not_receive_code_prompt),
                stringResource(R.string.resend_code_link),
                tag = "resend"
            ),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.clickable(enabled = !uiState.isResending) {
                viewModel.resendCode()
            }
        )

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = { viewModel.verify() },
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
                    text = stringResource(R.string.verify_button),
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
        uiState.resendMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = Color(0xFF2E7D32),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun clickablePart(
    text: String,
    clickableText: String,
    tag: String
): AnnotatedString {
    return buildAnnotatedString {
        append("$text ")
        val start = this.length
        append(clickableText)
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
fun VerificationScreenPreview() {
    ListappTheme {
        VerificationScreen(padding = PaddingValues(), onVerified = {})
    }
}

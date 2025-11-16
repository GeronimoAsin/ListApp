package ar.edu.itba.listapp.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.data.model.UserProfile
import ar.edu.itba.listapp.data.network.AuthRepository
import ar.edu.itba.listapp.data.network.ProfileResult
import ar.edu.itba.listapp.data.network.LogoutResult
import ar.edu.itba.listapp.data.network.SessionManager
import ar.edu.itba.listapp.ui.composables.EditProfileForm
import ar.edu.itba.listapp.ui.theme.ListappTheme
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(
        context = application,
        sessionManager = SessionManager(application)
    )

    var uiState by mutableStateOf(ProfileUiState())
        private set

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getProfile()) {
                is ProfileResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        profile = result.profile
                    )
                }
                is ProfileResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun logout() {
        if (uiState.isLoggingOut) return
        viewModelScope.launch {
            uiState = uiState.copy(isLoggingOut = true)
            when (repository.logout()) {
                is LogoutResult.Success -> {
                    uiState = uiState.copy(
                        isLoggingOut = false,
                        loggedOut = true
                    )
                }
                is LogoutResult.Error -> {
                    uiState = uiState.copy(
                        isLoggingOut = false,
                        loggedOut = true
                    )
                }
            }
        }
    }
}

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggingOut: Boolean = false,
    val loggedOut: Boolean = false
)

@Composable
fun ProfileScreen(
    padding: PaddingValues,
    onChangePassword: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    var showEditProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) {
            onLogout()
        }
    }

    if (showEditProfileDialog && uiState.profile != null) {
        EditProfileForm(
            profile = uiState.profile,
            onDismiss = { showEditProfileDialog = false },
            onConfirm = {
                viewModel.loadProfile()
                showEditProfileDialog = false
            }
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    ProfileInfo(uiState, viewModel)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ProfileButtons(
                        uiState = uiState,
                        onChangePassword = onChangePassword,
                        onEditProfile = { showEditProfileDialog = true },
                        onLogout = { viewModel.logout() }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(74.dp))
                Text(
                    text = stringResource(R.string.profile_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(48.dp))
                ProfileInfo(uiState, viewModel)
                Spacer(modifier = Modifier.height(64.dp))
                ProfileButtons(
                    uiState = uiState,
                    onChangePassword = onChangePassword,
                    onEditProfile = { showEditProfileDialog = true },
                    onLogout = { viewModel.logout() }
                )
            }
        }
    }
}

@Composable
private fun ProfileInfo(uiState: ProfileUiState, viewModel: ProfileViewModel) {
    if (uiState.isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color(0xFF8CC94F)
        )
    } else if (uiState.errorMessage != null) {
        Text(
            text = uiState.errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.loadProfile() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8CC94F))
        ) {
            Text("Reintentar")
        }
    } else if (uiState.profile != null) {
        val profile = uiState.profile
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.profile_name_label))
                }
                append(" ${profile.name}")
            },
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.profile_surname_label))
                }
                append(" ${profile.surname}")
            },
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.profile_nickname_label))
                }
                append(" ${profile.metadata["nickname"] ?: ""}")
            },
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ProfileButtons(
    uiState: ProfileUiState,
    onChangePassword: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    Button(
        onClick = onEditProfile,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(20),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8CC94F))
    ) {
        Text(
            text = stringResource(R.string.profile_edit_profile_button),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onChangePassword,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(20),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8CC94F))
    ) {
        Text(
            text = stringResource(R.string.profile_change_password_button),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onLogout,
        enabled = !uiState.isLoggingOut,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(20),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
    ) {
        if (uiState.isLoggingOut) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = stringResource(R.string.profile_logout_button),
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun ProfileScreenPreview() {
    ListappTheme {
        ProfileScreen(padding = PaddingValues())
    }
}

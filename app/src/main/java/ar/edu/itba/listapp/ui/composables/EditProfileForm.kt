package ar.edu.itba.listapp.ui.composables

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.itba.listapp.R
import ar.edu.itba.listapp.data.model.UserProfile
import ar.edu.itba.listapp.data.network.AuthRepository
import ar.edu.itba.listapp.data.network.SessionManager
import kotlinx.coroutines.launch

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(
        context = application,
        sessionManager = SessionManager(application)
    )

    var uiState by mutableStateOf(EditProfileUiState())
        private set

    fun initialize(profile: UserProfile) {
        uiState = uiState.copy(
            name = profile.name,
            surname = profile.surname,
            nickname = profile.metadata["nickname"] ?: ""
        )
    }

    fun onNameChange(value: String) {
        uiState = uiState.copy(name = value)
    }

    fun onSurnameChange(value: String) {
        uiState = uiState.copy(surname = value)
    }

    fun onNicknameChange(value: String) {
        uiState = uiState.copy(nickname = value)
    }

    fun updateUser(onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            val result = repository.updateProfile(
                uiState.name,
                uiState.surname,
                mapOf("nickname" to uiState.nickname)
            )
            if (result.isSuccess) {
                uiState = uiState.copy(isLoading = false)
                onSuccess()
            } else {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }
    }
}

data class EditProfileUiState(
    val name: String = "",
    val surname: String = "",
    val nickname: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@Composable
fun EditProfileForm(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    LaunchedEffect(profile) {
        viewModel.initialize(profile)
    }

    val uiState = viewModel.uiState

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEFEF)),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.profile_edit_profile_button),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.profile_name_label))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        placeholder = { Text(stringResource(R.string.first_name_placeholder)) },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.profile_surname_label))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.surname,
                        onValueChange = { viewModel.onSurnameChange(it) },
                        placeholder = { Text(stringResource(R.string.last_name_placeholder)) },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.profile_nickname_label))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.nickname,
                        onValueChange = { viewModel.onNicknameChange(it) },
                        placeholder = { Text(stringResource(R.string.nickname_placeholder)) },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                uiState.errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            viewModel.updateUser {
                                onConfirm()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            }
        }
    }
}


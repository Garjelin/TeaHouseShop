package com.samuelokello.feat.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = uiState.user

    LaunchedEffect(uiState.user) {
        if (uiState.user == null && !uiState.isLoading) {
            navigateToLogin()
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            currentUser != null -> {
                Text(
                    text = "Профиль",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(text = "Имя: ${currentUser.displayName}")
                Text(text = "Email: ${currentUser.email}")
                Button(
                    onClick = viewModel::logout,
                    enabled = !uiState.isLoggingOut,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = if (uiState.isLoggingOut) "Выходим..." else "Выйти")
                }
            }
            else -> {
                Text(
                    text = "Вы не авторизованы",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(
                    onClick = navigateToLogin,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Перейти ко входу")
                }
            }
        }
    }
}
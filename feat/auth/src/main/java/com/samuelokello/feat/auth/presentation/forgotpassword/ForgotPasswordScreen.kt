package com.samuelokello.feat.auth.presentation.forgotpassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    navigateToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            if (event == ForgotPasswordNavigation.Login) {
                navigateToLogin()
            }
        }
    }

    ForgotPasswordScreenContent(
        modifier = modifier,
        uiState = uiState,
        onEmailChange = { viewModel.onEvent(ForgotPasswordEvent.EmailChanged(it)) },
        onNewPasswordChange = { viewModel.onEvent(ForgotPasswordEvent.NewPasswordChanged(it)) },
        onConfirmPasswordChange = { viewModel.onEvent(ForgotPasswordEvent.ConfirmPasswordChanged(it)) },
        onClickResetPassword = { viewModel.onEvent(ForgotPasswordEvent.Submit) },
    )
}

@Composable
private fun ForgotPasswordScreenContent(
    modifier: Modifier = Modifier,
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onClickResetPassword: () -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Column(Modifier.padding(end = 16.dp)) {
                Text(
                    text = "Введите email и новый пароль для локального аккаунта",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text(text = "Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = uiState.emailError != null,
                singleLine = true,
            )
            uiState.emailError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.newPassword,
                onValueChange = onNewPasswordChange,
                label = { Text(text = "Новый пароль") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = uiState.newPasswordError != null,
                singleLine = true,
            )
            uiState.newPasswordError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text(text = "Подтвердите пароль") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = uiState.confirmPasswordError != null,
                singleLine = true,
            )
            uiState.confirmPasswordError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onClickResetPassword,
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    text = "Обновить пароль",
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (uiState.isLoading) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }

        uiState.successMessage?.let { success ->
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = success,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        uiState.error?.let { error ->
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
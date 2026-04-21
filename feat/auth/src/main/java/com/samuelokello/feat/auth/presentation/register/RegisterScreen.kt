package com.samuelokello.feat.auth.presentation.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
private fun ColumnFieldWithError(
    error: String?,
    content: @Composable () -> Unit,
) {
    Column {
        content()
        error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun RegisterScreen(
    navigateToLogin: () -> Unit,
    navigateToHome: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { nav ->
            when (nav) {
                RegisterNavigation.Home -> navigateToHome()
            }
        }
    }

    RegisterScreenContent(
        uiState = uiState,
        onDisplayNameChange = { viewModel.onEvent(RegisterEvent.DisplayNameChanged(it)) },
        onEmailChange = { viewModel.onEvent(RegisterEvent.EmailChanged(it)) },
        onPasswordChange = { viewModel.onEvent(RegisterEvent.PasswordChanged(it)) },
        onConfirmPasswordChange = { viewModel.onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
        onSignUpClick = { viewModel.onEvent(RegisterEvent.Submit) },
        onLoginClick = navigateToLogin,
    )
}

@Composable
private fun RegisterScreenContent(
    uiState: RegisterUiState,
    onDisplayNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Text(
                text = "Регистрация",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(text = "Создайте аккаунт, чтобы пользоваться магазином")
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            ColumnFieldWithError(
                error = uiState.displayNameError,
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.displayName,
                    onValueChange = onDisplayNameChange,
                    label = { Text(text = "Имя") },
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text,
                        ),
                    maxLines = 1,
                    singleLine = true,
                    isError = uiState.displayNameError != null,
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            ColumnFieldWithError(
                error = uiState.emailError,
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = { Text(text = "Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    maxLines = 1,
                    singleLine = true,
                    isError = uiState.emailError != null,
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            ColumnFieldWithError(
                error = uiState.passwordError,
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    label = { Text(text = "Пароль") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    maxLines = 1,
                    singleLine = true,
                    isError = uiState.passwordError != null,
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            ColumnFieldWithError(
                error = uiState.confirmPasswordError,
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text(text = "Повторите пароль") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    maxLines = 1,
                    singleLine = true,
                    isError = uiState.confirmPasswordError != null,
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onSignUpClick,
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    text = "Зарегистрироваться",
                    textAlign = TextAlign.Center,
                )
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                }
            }
        }

        uiState.error?.let { err ->
            item {
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text =
                        buildAnnotatedString {
                            append("Уже есть аккаунт? ")
                            withStyle(
                                style =
                                    SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    ),
                            ) {
                                append("Войти")
                            }
                        },
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

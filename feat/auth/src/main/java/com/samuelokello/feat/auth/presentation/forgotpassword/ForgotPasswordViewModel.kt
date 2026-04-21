package com.samuelokello.feat.auth.presentation.forgotpassword

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.core.domain.usecase.auth.ResetPasswordUseCase
import com.samuelokello.core.domain.util.Result
import com.samuelokello.feat.auth.presentation.AuthValidation
import com.samuelokello.feat.auth.presentation.toDisplayMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase,
) : ViewModel() {
    private val _uiState = mutableStateOf(ForgotPasswordUiState())
    val uiState: State<ForgotPasswordUiState> = _uiState

    private val _navigationEvent = MutableSharedFlow<ForgotPasswordNavigation>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> {
                _uiState.value =
                    _uiState.value.copy(
                        email = event.value,
                        emailError = validateEmail(event.value),
                    )
            }
            is ForgotPasswordEvent.NewPasswordChanged -> {
                val currentState = _uiState.value
                _uiState.value =
                    currentState.copy(
                        newPassword = event.value,
                        newPasswordError = validatePassword(event.value),
                        confirmPasswordError = validateConfirmPassword(event.value, currentState.confirmPassword),
                    )
            }
            is ForgotPasswordEvent.ConfirmPasswordChanged -> {
                _uiState.value =
                    _uiState.value.copy(
                        confirmPassword = event.value,
                        confirmPasswordError =
                            validateConfirmPassword(_uiState.value.newPassword, event.value),
                    )
            }
            ForgotPasswordEvent.Submit -> resetPassword()
        }
    }

    private fun resetPassword() {
        if (!validateAll()) return

        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(isLoading = true, error = null)

            when (
                val result =
                    resetPasswordUseCase(
                        email = currentState.email.trim(),
                        newPassword = currentState.newPassword,
                    )
            ) {
                is Result.Success -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Пароль обновлен. Войдите с новым паролем",
                            error = null,
                        )
                    _navigationEvent.emit(ForgotPasswordNavigation.Login)
                }
                is Result.Error -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            successMessage = null,
                            error = result.error.toDisplayMessage(),
                        )
                }
            }
        }
    }

    private fun validateAll(): Boolean {
        val state = _uiState.value
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.newPassword)
        val confirmError = validateConfirmPassword(state.newPassword, state.confirmPassword)

        _uiState.value =
            state.copy(
                emailError = emailError,
                newPasswordError = passwordError,
                confirmPasswordError = confirmError,
            )

        return emailError == null && passwordError == null && confirmError == null
    }

    private fun validateEmail(email: String): String? =
        when {
            email.isBlank() -> "Введите email"
            !AuthValidation.isValidEmail(email) -> "Некорректный email"
            else -> null
        }

    private fun validatePassword(password: String): String? =
        when {
            password.isBlank() -> "Введите новый пароль"
            !AuthValidation.validatePasswordStrength(password) ->
                "Минимум 8 символов и хотя бы одна цифра"
            else -> null
        }

    private fun validateConfirmPassword(
        password: String,
        confirmPassword: String,
    ): String? =
        when {
            confirmPassword.isBlank() -> "Повторите новый пароль"
            confirmPassword != password -> "Пароли не совпадают"
            else -> null
        }
}

data class ForgotPasswordUiState(
    val email: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null,
)

sealed class ForgotPasswordEvent {
    data class EmailChanged(
        val value: String,
    ) : ForgotPasswordEvent()

    data class NewPasswordChanged(
        val value: String,
    ) : ForgotPasswordEvent()

    data class ConfirmPasswordChanged(
        val value: String,
    ) : ForgotPasswordEvent()

    data object Submit : ForgotPasswordEvent()
}

enum class ForgotPasswordNavigation {
    Login,
}

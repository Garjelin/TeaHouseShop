package com.samuelokello.feat.auth.presentation.register

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.core.domain.usecase.auth.RegisterUseCase
import com.samuelokello.core.domain.util.Result
import com.samuelokello.feat.auth.presentation.AuthValidation
import com.samuelokello.feat.auth.presentation.toDisplayMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {
    private val _uiState = mutableStateOf(RegisterUiState())
    val uiState: State<RegisterUiState> = _uiState

    private val _navigationEvent = MutableSharedFlow<RegisterNavigation>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.DisplayNameChanged -> {
                _uiState.value =
                    _uiState.value.copy(
                        displayName = event.value,
                        displayNameError = validateDisplayName(event.value),
                    )
            }
            is RegisterEvent.EmailChanged -> {
                _uiState.value =
                    _uiState.value.copy(
                        email = event.value,
                        emailError = validateEmail(event.value),
                    )
            }
            is RegisterEvent.PasswordChanged -> {
                val s = _uiState.value
                _uiState.value =
                    s.copy(
                        password = event.value,
                        passwordError = validatePassword(event.value),
                        confirmPasswordError =
                            validateConfirmPassword(event.value, s.confirmPassword),
                    )
            }
            is RegisterEvent.ConfirmPasswordChanged -> {
                _uiState.value =
                    _uiState.value.copy(
                        confirmPassword = event.value,
                        confirmPasswordError =
                            validateConfirmPassword(
                                _uiState.value.password,
                                event.value,
                            ),
                    )
            }
            RegisterEvent.Submit -> register()
        }
    }

    private fun register() {
        if (!validateAll()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val s = _uiState.value
            when (
                val result =
                    registerUseCase(
                        displayName = s.displayName.trim(),
                        email = s.email.trim(),
                        password = s.password,
                    )
            ) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    _navigationEvent.emit(RegisterNavigation.Home)
                }
                is Result.Error -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            error = result.error.toDisplayMessage(),
                        )
                }
            }
        }
    }

    private fun validateAll(): Boolean {
        val s = _uiState.value
        val nameErr = validateDisplayName(s.displayName)
        val emailErr = validateEmail(s.email)
        val passErr = validatePassword(s.password)
        val confirmErr = validateConfirmPassword(s.password, s.confirmPassword)

        _uiState.value =
            s.copy(
                displayNameError = nameErr,
                emailError = emailErr,
                passwordError = passErr,
                confirmPasswordError = confirmErr,
            )

        return nameErr == null && emailErr == null && passErr == null && confirmErr == null
    }

    private fun validateDisplayName(name: String): String? =
        when {
            name.isBlank() -> "Введите имя"
            name.length < 2 -> "Слишком короткое имя"
            else -> null
        }

    private fun validateEmail(email: String): String? =
        when {
            email.isBlank() -> "Введите email"
            !AuthValidation.isValidEmail(email) -> "Некорректный email"
            else -> null
        }

    private fun validatePassword(password: String): String? =
        when {
            password.isBlank() -> "Введите пароль"
            !AuthValidation.validatePasswordStrength(password) ->
                "Минимум 8 символов и хотя бы одна цифра"
            else -> null
        }

    private fun validateConfirmPassword(
        password: String,
        confirm: String,
    ): String? =
        when {
            confirm.isBlank() -> "Повторите пароль"
            confirm != password -> "Пароли не совпадают"
            else -> null
        }
}

data class RegisterUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed class RegisterEvent {
    data class DisplayNameChanged(val value: String) : RegisterEvent()

    data class EmailChanged(val value: String) : RegisterEvent()

    data class PasswordChanged(val value: String) : RegisterEvent()

    data class ConfirmPasswordChanged(val value: String) : RegisterEvent()

    data object Submit : RegisterEvent()
}

enum class RegisterNavigation {
    Home,
}

package com.samuelokello.feat.auth.presentation.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.core.domain.usecase.auth.LoginUseCase
import com.samuelokello.core.domain.util.Result
import com.samuelokello.feat.auth.presentation.AuthValidation
import com.samuelokello.feat.auth.presentation.toDisplayMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {
    private val _uiState = mutableStateOf(LoginUiState())
    val uiState: State<LoginUiState> = _uiState

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _uiState.value =
                    _uiState.value.copy(
                        email = event.value,
                        emailError = validateEmailField(event.value),
                    )
            }
            is LoginEvent.PasswordChanged -> {
                _uiState.value =
                    _uiState.value.copy(
                        password = event.value,
                        passwordError = validatePasswordField(event.value),
                    )
            }
            is LoginEvent.RememberMeChanged -> {
                _uiState.value = _uiState.value.copy(rememberMe = event.value)
            }
            LoginEvent.Submit -> loginUser()
        }
    }

    private fun loginUser() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (
                val result =
                    loginUseCase(
                        email = _uiState.value.email.trim(),
                        password = _uiState.value.password,
                    )
            ) {
                is Result.Success -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            error = null,
                        )
                    _navigationEvent.emit("home")
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

    private fun validateInputs(): Boolean {
        val emailError = validateEmailField(_uiState.value.email)
        val passwordError = validatePasswordField(_uiState.value.password)

        _uiState.value =
            _uiState.value.copy(
                emailError = emailError,
                passwordError = passwordError,
            )

        return emailError == null && passwordError == null
    }

    private fun validateEmailField(email: String): String? =
        when {
            email.isBlank() -> "Введите email"
            !AuthValidation.isValidEmail(email) -> "Некорректный email"
            else -> null
        }

    private fun validatePasswordField(password: String): String? =
        when {
            password.isBlank() -> "Введите пароль"
            password.length < 6 -> "Минимум 6 символов"
            else -> null
        }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed class LoginEvent {
    data class EmailChanged(
        val value: String,
    ) : LoginEvent()

    data class PasswordChanged(
        val value: String,
    ) : LoginEvent()

    data class RememberMeChanged(
        val value: Boolean,
    ) : LoginEvent()

    data object Submit : LoginEvent()
}

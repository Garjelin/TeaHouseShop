package com.samuelokello.feat.auth.presentation

private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

internal object AuthValidation {
    fun isValidEmail(value: String): Boolean = EMAIL_REGEX.matches(value.trim())

    fun validatePasswordStrength(password: String): Boolean =
        password.length >= 8 && password.any { it.isDigit() }
}

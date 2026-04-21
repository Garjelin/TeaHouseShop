package com.samuelokello.feat.auth.presentation

import com.samuelokello.core.domain.util.DataError

internal fun DataError.Auth.toDisplayMessage(): String =
    when (this) {
        DataError.Auth.EMAIL_ALREADY_EXISTS -> "Этот email уже зарегистрирован"
        DataError.Auth.INVALID_CREDENTIALS -> "Неверный email или пароль"
        DataError.Auth.WEAK_PASSWORD ->
            "Пароль: минимум 8 символов и хотя бы одна цифра"
        DataError.Auth.INVALID_EMAIL -> "Некорректный email"
        DataError.Auth.UNKNOWN -> "Не удалось выполнить операцию"
    }

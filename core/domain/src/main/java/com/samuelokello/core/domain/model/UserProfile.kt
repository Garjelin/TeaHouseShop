package com.samuelokello.core.domain.model

/**
 * Профиль пользователя после локальной регистрации/входа (offline-first).
 */
data class UserProfile(
    val id: Long,
    val email: String,
    val displayName: String,
)

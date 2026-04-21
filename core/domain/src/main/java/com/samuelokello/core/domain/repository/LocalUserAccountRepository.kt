package com.samuelokello.core.domain.repository

import com.samuelokello.core.domain.model.UserProfile
import com.samuelokello.core.domain.util.DataError
import com.samuelokello.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Локальные учётные записи (Room) и сессия (DataStore).
 * Без сетевого JWT — при появлении API можно расширить или объединить с [AuthenticationRepository].
 */
interface LocalUserAccountRepository {
    suspend fun register(
        displayName: String,
        email: String,
        password: String,
    ): Result<UserProfile, DataError.Auth>

    suspend fun login(
        email: String,
        password: String,
    ): Result<UserProfile, DataError.Auth>

    suspend fun logout()

    fun observeCurrentUser(): Flow<UserProfile?>
}

package com.samuelokello.data.repository.repository

import com.samuelokello.core.domain.model.UserProfile
import com.samuelokello.core.domain.repository.LocalUserAccountRepository
import com.samuelokello.core.domain.util.DataError
import com.samuelokello.core.domain.util.Result
import com.samuelokello.data.repository.mapper.toUserProfile
import com.samuelokello.datasource.local.entity.user.LocalUserAccountEntity
import com.samuelokello.datasource.local.source.user.LocalUserAccountSource
import com.samuelokello.datasource.local.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class LocalUserAccountRepositoryImpl(
    private val source: LocalUserAccountSource,
) : LocalUserAccountRepository {
    override suspend fun register(
        displayName: String,
        email: String,
        password: String,
    ): Result<UserProfile, DataError.Auth> =
        withContext(Dispatchers.IO) {
            val normalizedEmail = email.trim().lowercase()
            if (normalizedEmail.isBlank()) {
                return@withContext Result.Error(DataError.Auth.INVALID_EMAIL)
            }
            if (source.getByEmail(normalizedEmail) != null) {
                return@withContext Result.Error(DataError.Auth.EMAIL_ALREADY_EXISTS)
            }
            val salt = PasswordHasher.generateSalt()
            val hash = PasswordHasher.hash(password, salt)
            val entity =
                LocalUserAccountEntity(
                    email = normalizedEmail,
                    displayName = displayName.trim(),
                    passwordHash = hash,
                    salt = salt,
                )
            val id = source.insertAccount(entity)
            val profile =
                UserProfile(
                    id = id,
                    email = normalizedEmail,
                    displayName = entity.displayName,
                )
            source.setCurrentUserId(id)
            Result.Success(profile)
        }

    override suspend fun login(
        email: String,
        password: String,
    ): Result<UserProfile, DataError.Auth> =
        withContext(Dispatchers.IO) {
            val normalizedEmail = email.trim().lowercase()
            val entity = source.getByEmail(normalizedEmail)
                ?: return@withContext Result.Error(DataError.Auth.INVALID_CREDENTIALS)
            if (!PasswordHasher.verify(password, entity.salt, entity.passwordHash)) {
                return@withContext Result.Error(DataError.Auth.INVALID_CREDENTIALS)
            }
            source.setCurrentUserId(entity.id)
            Result.Success(entity.toUserProfile())
        }

    override suspend fun resetPassword(
        email: String,
        newPassword: String,
    ): Result<Unit, DataError.Auth> =
        withContext(Dispatchers.IO) {
            val normalizedEmail = email.trim().lowercase()
            if (normalizedEmail.isBlank()) {
                return@withContext Result.Error(DataError.Auth.INVALID_EMAIL)
            }
            if (newPassword.length < 8 || newPassword.none(Char::isDigit)) {
                return@withContext Result.Error(DataError.Auth.WEAK_PASSWORD)
            }
            val account = source.getByEmail(normalizedEmail)
                ?: return@withContext Result.Error(DataError.Auth.USER_NOT_FOUND)

            val newSalt = PasswordHasher.generateSalt()
            val newHash = PasswordHasher.hash(newPassword, newSalt)
            val updated =
                source.updatePasswordByEmail(
                    email = account.email,
                    passwordHash = newHash,
                    salt = newSalt,
                )

            if (!updated) {
                return@withContext Result.Error(DataError.Auth.UNKNOWN)
            }
            Result.Success(Unit)
        }

    override suspend fun logout() {
        withContext(Dispatchers.IO) {
            source.clearSession()
        }
    }

    override fun observeCurrentUser(): Flow<UserProfile?> =
        source.observeCurrentUserId().flatMapLatest { userId ->
            flow {
                if (userId == null) {
                    emit(null)
                } else {
                    emit(source.getById(userId)?.toUserProfile())
                }
            }
        }.flowOn(Dispatchers.IO)
}

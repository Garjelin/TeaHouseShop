package com.samuelokello.core.domain.usecase.auth

import com.samuelokello.core.domain.repository.LocalUserAccountRepository
import com.samuelokello.core.domain.util.DataError
import com.samuelokello.core.domain.util.Result

class ResetPasswordUseCase(
    private val repository: LocalUserAccountRepository,
) {
    suspend operator fun invoke(
        email: String,
        newPassword: String,
    ): Result<Unit, DataError.Auth> = repository.resetPassword(email = email, newPassword = newPassword)
}

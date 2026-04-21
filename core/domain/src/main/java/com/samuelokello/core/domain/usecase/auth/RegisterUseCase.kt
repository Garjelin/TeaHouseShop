package com.samuelokello.core.domain.usecase.auth

import com.samuelokello.core.domain.model.UserProfile
import com.samuelokello.core.domain.repository.LocalUserAccountRepository
import com.samuelokello.core.domain.util.DataError
import com.samuelokello.core.domain.util.Result

class RegisterUseCase(
    private val repository: LocalUserAccountRepository,
) {
    suspend operator fun invoke(
        displayName: String,
        email: String,
        password: String,
    ): Result<UserProfile, DataError.Auth> =
        repository.register(displayName, email, password)
}

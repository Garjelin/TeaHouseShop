package com.samuelokello.core.domain.usecase.auth

import com.samuelokello.core.domain.model.UserProfile
import com.samuelokello.core.domain.repository.LocalUserAccountRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentUserUseCase(
    private val repository: LocalUserAccountRepository,
) {
    operator fun invoke(): Flow<UserProfile?> = repository.observeCurrentUser()
}

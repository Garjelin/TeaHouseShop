package com.samuelokello.core.domain.usecase.auth

import com.samuelokello.core.domain.repository.LocalUserAccountRepository

class LogoutUseCase(
    private val repository: LocalUserAccountRepository,
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}

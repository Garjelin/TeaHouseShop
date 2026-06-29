package com.samuelokello.core.domain.usecase.cart

import com.samuelokello.core.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow

class ClearCartUseCase(
    private val repository: CartRepository,
) {
    suspend operator fun invoke(userId: Int): Flow<Result<Unit>> = repository.clearCart(userId)
}

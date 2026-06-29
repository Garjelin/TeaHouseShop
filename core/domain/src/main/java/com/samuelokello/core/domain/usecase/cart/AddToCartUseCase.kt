package com.samuelokello.core.domain.usecase.cart

import com.samuelokello.core.domain.model.UserCart
import com.samuelokello.core.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow

class AddToCartUseCase(
    private val repository: CartRepository,
) {
    suspend operator fun invoke(
        userId: Int,
        productId: Int,
        quantity: Int,
    ): Flow<Result<UserCart>> = repository.addItemToCart(userId, productId, quantity)
}

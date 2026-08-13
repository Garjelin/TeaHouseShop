package com.samuelokello.core.domain.usecase.order

import com.samuelokello.core.domain.model.Order
import com.samuelokello.core.domain.repository.OrderRepository

class GetUserOrdersUseCase(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(userId: Int): Result<List<Order>> = repository.getOrders(userId)
}

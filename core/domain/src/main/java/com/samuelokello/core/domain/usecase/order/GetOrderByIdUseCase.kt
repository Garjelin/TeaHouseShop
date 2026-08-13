package com.samuelokello.core.domain.usecase.order

import com.samuelokello.core.domain.model.Order
import com.samuelokello.core.domain.repository.OrderRepository

class GetOrderByIdUseCase(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(orderId: String): Result<Order?> = repository.getOrderById(orderId)
}

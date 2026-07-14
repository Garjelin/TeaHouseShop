package com.samuelokello.data.repository.repository

import com.samuelokello.core.domain.model.Order
import com.samuelokello.core.domain.repository.OrderRepository
import com.samuelokello.datasource.local.source.order.OrderLocalSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepositoryImpl(
    private val localSource: OrderLocalSource,
) : OrderRepository {
    override suspend fun createOrder(order: Order): Result<Order> =
        withContext(Dispatchers.IO) {
            runCatching {
                localSource.appendOrder(order)
                order
            }
        }

    override suspend fun getOrders(userId: Int): Result<List<Order>> =
        withContext(Dispatchers.IO) {
            runCatching { localSource.getOrders(userId) }
        }

    override suspend fun getOrderById(orderId: String): Result<Order?> =
        withContext(Dispatchers.IO) {
            runCatching { localSource.getOrderById(orderId) }
        }
}

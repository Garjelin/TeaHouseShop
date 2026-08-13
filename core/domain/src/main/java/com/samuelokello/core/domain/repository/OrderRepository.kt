package com.samuelokello.core.domain.repository

import com.samuelokello.core.domain.model.Order

/**
 * Локальные заказы (offline-first). Отправка на сервер — при появлении API.
 */
interface OrderRepository {
    suspend fun createOrder(order: Order): Result<Order>

    suspend fun getOrders(userId: Int): Result<List<Order>>

    suspend fun getOrderById(orderId: String): Result<Order?>
}

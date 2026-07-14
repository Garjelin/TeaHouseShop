package com.samuelokello.datasource.local.source.order

import androidx.datastore.preferences.core.stringPreferencesKey
import com.samuelokello.core.domain.model.Order
import com.samuelokello.core.domain.model.OrderItem
import com.samuelokello.core.domain.model.PaymentMethod
import com.samuelokello.datasource.local.source.preference.PreferenceHelper
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface OrderLocalSource {
    suspend fun appendOrder(order: Order)

    suspend fun getOrders(userId: Int): List<Order>

    suspend fun getOrderById(orderId: String): Order?
}

class OrderLocalSourceImpl(
    private val preferences: PreferenceHelper,
) : OrderLocalSource {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun appendOrder(order: Order) {
        val existing = readAll().toMutableList()
        existing.add(0, order)
        writeAll(existing)
    }

    override suspend fun getOrders(userId: Int): List<Order> =
        readAll().filter { it.userId == userId }

    override suspend fun getOrderById(orderId: String): Order? =
        readAll().firstOrNull { it.id == orderId }

    private suspend fun readAll(): List<Order> {
        val raw = preferences.get(ORDERS_KEY).first() ?: return emptyList()
        return runCatching {
            json.decodeFromString<OrdersSnapshotDto>(raw).orders.map { it.toDomain() }
        }.getOrElse { emptyList() }
    }

    private suspend fun writeAll(orders: List<Order>) {
        val payload =
            json.encodeToString(
                OrdersSnapshotDto(orders = orders.map { it.toDto() }),
            )
        preferences.save(ORDERS_KEY, payload)
    }

    private fun OrderSnapshotDto.toDomain(): Order =
        Order(
            id = id,
            userId = userId,
            items =
                items.map {
                    OrderItem(
                        productId = it.productId,
                        title = it.title,
                        price = it.price,
                        quantity = it.quantity,
                    )
                },
            address = address,
            paymentMethod =
                runCatching { PaymentMethod.valueOf(paymentMethod) }
                    .getOrDefault(PaymentMethod.CASH),
            deliverySlot = deliverySlot,
            comment = comment,
            totalAmount = totalAmount,
            createdAt = createdAt,
        )

    private fun Order.toDto(): OrderSnapshotDto =
        OrderSnapshotDto(
            id = id,
            userId = userId,
            items =
                items.map {
                    OrderItemSnapshotDto(
                        productId = it.productId,
                        title = it.title,
                        price = it.price,
                        quantity = it.quantity,
                    )
                },
            address = address,
            paymentMethod = paymentMethod.name,
            deliverySlot = deliverySlot,
            comment = comment,
            totalAmount = totalAmount,
            createdAt = createdAt,
        )

    companion object {
        private val ORDERS_KEY = stringPreferencesKey("orders_snapshot")
    }
}

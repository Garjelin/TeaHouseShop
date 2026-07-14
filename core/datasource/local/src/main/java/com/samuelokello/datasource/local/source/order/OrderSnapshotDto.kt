package com.samuelokello.datasource.local.source.order

import kotlinx.serialization.Serializable

@Serializable
internal data class OrdersSnapshotDto(
    val orders: List<OrderSnapshotDto> = emptyList(),
)

@Serializable
internal data class OrderSnapshotDto(
    val id: String,
    val userId: Int,
    val items: List<OrderItemSnapshotDto>,
    val address: String,
    val paymentMethod: String,
    val deliverySlot: String,
    val comment: String,
    val totalAmount: Double,
    val createdAt: Long,
)

@Serializable
internal data class OrderItemSnapshotDto(
    val productId: Int,
    val title: String,
    val price: Double,
    val quantity: Int,
)

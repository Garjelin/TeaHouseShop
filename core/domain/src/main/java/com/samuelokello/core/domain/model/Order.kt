package com.samuelokello.core.domain.model

data class Order(
    val id: String,
    val userId: Int,
    val items: List<OrderItem>,
    val address: String,
    val paymentMethod: PaymentMethod,
    val deliverySlot: String,
    val comment: String,
    val totalAmount: Double,
    val createdAt: Long,
)

data class OrderItem(
    val productId: Int,
    val title: String,
    val price: Double,
    val quantity: Int,
)

enum class PaymentMethod {
    CASH,
    CARD,
}

data class CheckoutDraft(
    val address: String,
    val paymentMethod: PaymentMethod,
    val deliverySlot: String,
    val comment: String,
)

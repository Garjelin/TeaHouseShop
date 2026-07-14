package com.samuelokello.core.domain.usecase.order

import com.samuelokello.core.domain.model.CheckoutDraft
import com.samuelokello.core.domain.model.Order
import com.samuelokello.core.domain.model.OrderItem
import com.samuelokello.core.domain.repository.CartRepository
import com.samuelokello.core.domain.repository.OrderRepository
import com.samuelokello.core.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

class CreateOrderUseCase(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
) {
    suspend operator fun invoke(
        userId: Int,
        draft: CheckoutDraft,
        shippingFee: Double = DEFAULT_SHIPPING_FEE,
    ): Result<Order> {
        val address = draft.address.trim()
        if (address.length < 5) {
            return Result.failure(IllegalArgumentException("Укажите адрес доставки"))
        }

        val cartsResult = cartRepository.getUserCarts(userId).first()
        val cart =
            cartsResult.getOrElse { return Result.failure(it) }
                .maxByOrNull { it.date }
                ?: return Result.failure(IllegalStateException("Корзина пуста"))

        if (cart.products.isEmpty()) {
            return Result.failure(IllegalStateException("Корзина пуста"))
        }

        val items = mutableListOf<OrderItem>()
        var goodsTotal = 0.0
        for (cartProduct in cart.products) {
            val product =
                productRepository.getProductById(cartProduct.productId)
                    ?: return Result.failure(
                        IllegalStateException("Товар #${cartProduct.productId} не найден"),
                    )
            items +=
                OrderItem(
                    productId = product.id,
                    title = product.title,
                    price = product.price,
                    quantity = cartProduct.quantity,
                )
            goodsTotal += product.price * cartProduct.quantity
        }

        val order =
            Order(
                id = UUID.randomUUID().toString(),
                userId = userId,
                items = items,
                address = address,
                paymentMethod = draft.paymentMethod,
                deliverySlot = draft.deliverySlot.trim(),
                comment = draft.comment.trim(),
                totalAmount = goodsTotal + shippingFee,
                createdAt = System.currentTimeMillis(),
            )

        val created = orderRepository.createOrder(order)
        if (created.isFailure) return created

        cartRepository.clearCart(userId).first()
        return created
    }

    companion object {
        const val DEFAULT_SHIPPING_FEE = 60.0
    }
}

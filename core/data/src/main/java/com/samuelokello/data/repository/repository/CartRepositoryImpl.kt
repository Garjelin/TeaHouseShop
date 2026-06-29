package com.samuelokello.data.repository.repository

import com.samuelokello.core.domain.model.CartProduct
import com.samuelokello.core.domain.model.UserCart
import com.samuelokello.core.domain.repository.CartRepository
import com.samuelokello.datasource.local.source.cart.CartLocalSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap

/**
 * Корзина в памяти процесса + снимок в DataStore (переживает перезапуск приложения).
 */
class CartRepositoryImpl(
    private val localSource: CartLocalSource,
) : CartRepository {
    private val cartsByUser = ConcurrentHashMap<Int, UserCart>()

    override suspend fun getUserCarts(userId: Int): Flow<Result<List<UserCart>>> =
        flow {
            emit(Result.success(listOfNotNull(loadCart(userId))))
        }

    override suspend fun addItemToCart(
        userId: Int,
        productId: Int,
        quantity: Int,
    ): Flow<Result<UserCart>> =
        flow {
            if (quantity <= 0) {
                emit(Result.failure(IllegalArgumentException("Количество должно быть больше 0")))
                return@flow
            }
            val existing = loadCart(userId)
            val products = existing?.products?.toMutableList() ?: mutableListOf()
            val idx = products.indexOfFirst { it.productId == productId }
            if (idx >= 0) {
                val old = products[idx]
                products[idx] = old.copy(quantity = old.quantity + quantity)
            } else {
                products.add(CartProduct(productId = productId, quantity = quantity))
            }
            val cart =
                UserCart(
                    id = 1,
                    date = System.currentTimeMillis().toString(),
                    products = products,
                    userId = userId,
                    v = 1,
                )
            persistCart(cart)
            emit(Result.success(cart))
        }

    override suspend fun removeItemFromCart(
        userId: Int,
        productId: Int,
    ): Flow<Result<UserCart>> =
        flow {
            val existing =
                loadCart(userId) ?: run {
                    emit(Result.failure(NoSuchElementException("Корзина пуста")))
                    return@flow
                }
            val products = existing.products.filter { it.productId != productId }
            val cart =
                existing.copy(
                    products = products,
                    date = System.currentTimeMillis().toString(),
                )
            persistCart(cart)
            emit(Result.success(cart))
        }

    override suspend fun updateItemQuantity(
        userId: Int,
        productId: Int,
        quantity: Int,
    ): Flow<Result<UserCart>> =
        flow {
            val existing =
                loadCart(userId) ?: run {
                    emit(Result.failure(NoSuchElementException("Корзина пуста")))
                    return@flow
                }
            if (quantity <= 0) {
                emit(Result.failure(IllegalArgumentException("Количество должно быть больше 0")))
                return@flow
            }
            if (existing.products.none { it.productId == productId }) {
                emit(Result.failure(NoSuchElementException("Товара нет в корзине")))
                return@flow
            }
            val products =
                existing.products.map { item ->
                    if (item.productId == productId) item.copy(quantity = quantity) else item
                }
            val cart =
                existing.copy(
                    products = products,
                    date = System.currentTimeMillis().toString(),
                )
            persistCart(cart)
            emit(Result.success(cart))
        }

    override suspend fun clearCart(userId: Int): Flow<Result<Unit>> =
        flow {
            cartsByUser.remove(userId)
            localSource.clearCart(userId)
            emit(Result.success(Unit))
        }

    override suspend fun refreshCarts(userId: Int): Flow<Result<List<UserCart>>> =
        flow {
            cartsByUser.remove(userId)
            emit(Result.success(listOfNotNull(loadCart(userId))))
        }

    private suspend fun loadCart(userId: Int): UserCart? {
        cartsByUser[userId]?.let { return it }
        val fromDisk = localSource.loadCart(userId)
        if (fromDisk != null) {
            cartsByUser[userId] = fromDisk
        }
        return fromDisk
    }

    private suspend fun persistCart(cart: UserCart) {
        if (cart.products.isEmpty()) {
            cartsByUser.remove(cart.userId)
            localSource.clearCart(cart.userId)
        } else {
            cartsByUser[cart.userId] = cart
            localSource.saveCart(cart)
        }
    }
}

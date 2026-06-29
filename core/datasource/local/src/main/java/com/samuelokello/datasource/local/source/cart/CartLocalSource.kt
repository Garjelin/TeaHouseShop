package com.samuelokello.datasource.local.source.cart

import androidx.datastore.preferences.core.stringPreferencesKey
import com.samuelokello.core.domain.model.CartProduct
import com.samuelokello.core.domain.model.UserCart
import com.samuelokello.datasource.local.source.preference.PreferenceHelper
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface CartLocalSource {
    suspend fun loadCart(userId: Int): UserCart?

    suspend fun saveCart(cart: UserCart)

    suspend fun clearCart(userId: Int)
}

class CartLocalSourceImpl(
    private val preferences: PreferenceHelper,
) : CartLocalSource {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun loadCart(userId: Int): UserCart? {
        val raw =
            preferences
                .get(cartKey(userId))
                .first()
                ?: return null
        return runCatching {
            json.decodeFromString<CartSnapshotDto>(raw).toDomain()
        }.getOrNull()
    }

    override suspend fun saveCart(cart: UserCart) {
        val payload = json.encodeToString(cart.toDto())
        preferences.save(cartKey(cart.userId), payload)
    }

    override suspend fun clearCart(userId: Int) {
        preferences.delete(cartKey(userId))
    }

    private fun cartKey(userId: Int) = stringPreferencesKey("cart_snapshot_$userId")

    private fun CartSnapshotDto.toDomain(): UserCart =
        UserCart(
            id = 1,
            date = date,
            products = products.map { CartProduct(productId = it.productId, quantity = it.quantity) },
            userId = userId,
            v = 1,
        )

    private fun UserCart.toDto(): CartSnapshotDto =
        CartSnapshotDto(
            userId = userId,
            date = date,
            products = products.map { CartProductSnapshotDto(productId = it.productId, quantity = it.quantity) },
        )
}

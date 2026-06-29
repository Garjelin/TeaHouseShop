package com.samuelokello.datasource.local.source.cart

import kotlinx.serialization.Serializable

@Serializable
internal data class CartSnapshotDto(
    val userId: Int,
    val date: String,
    val products: List<CartProductSnapshotDto>,
)

@Serializable
internal data class CartProductSnapshotDto(
    val productId: Int,
    val quantity: Int,
)

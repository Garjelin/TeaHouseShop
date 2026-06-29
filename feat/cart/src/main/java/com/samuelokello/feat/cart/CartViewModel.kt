package com.samuelokello.feat.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.core.domain.model.CartItem
import com.samuelokello.core.domain.model.UserCart
import com.samuelokello.core.domain.usecase.cart.ClearCartUseCase
import com.samuelokello.core.domain.usecase.auth.GetCurrentUserUseCase
import com.samuelokello.core.domain.repository.CartRepository
import com.samuelokello.core.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CartViewModel(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val clearCartUseCase: ClearCartUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    private var cartUserId: Int = 0

    init {
        viewModelScope.launch {
            getCurrentUserUseCase()
                .map { profile -> profile?.id?.toInt() ?: 0 }
                .distinctUntilChanged()
                .collect { userId ->
                    cartUserId = userId
                    fetchCartItems(userId)
                }
        }
    }

    private fun fetchCartItems(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = CartUiState.Loading
            cartRepository
                .getUserCarts(userId)
                .collect { result ->
                    result.fold(
                        onSuccess = { carts ->
                            val latestCart = carts.maxByOrNull { it.date }
                            if (latestCart != null) {
                                loadCartWithProducts(latestCart)
                            } else {
                                _cartItems.value = emptyList()
                                _uiState.value = CartUiState.Success(emptyList())
                            }
                        },
                        onFailure = { exception ->
                            _uiState.value = CartUiState.Error("Failed to load cart: ${exception.message}")
                        },
                    )
                }
        }
    }

    private fun loadCartWithProducts(cart: UserCart) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cartItems =
                    cart.products.mapNotNull { cartProduct ->
                        val product = productRepository.getProductById(cartProduct.productId)
                        product?.let { CartItem(it, cartProduct.quantity) }
                    }
                _cartItems.value = cartItems
                calculateTotal()
                _uiState.value = CartUiState.Success(cartItems)
            } catch (e: Exception) {
                _uiState.value = CartUiState.Error("Failed to load product details: ${e.message}")
            }
        }
    }

    fun updateQuantity(
        productId: Int,
        increase: Boolean,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = _cartItems.value.find { it.product.id == productId } ?: return@launch
            val newQuantity = if (increase) item.quantity + 1 else (item.quantity - 1).coerceAtLeast(1)

            cartRepository
                .updateItemQuantity(cartUserId, productId, newQuantity)
                .collect { result ->
                    result.fold(
                        onSuccess = { updatedCart ->
                            // Refresh the entire cart to ensure consistency
                            loadCartWithProducts(updatedCart)
                        },
                        onFailure = { exception ->
                            _uiState.value = CartUiState.Error("Failed to update quantity: ${exception.message}")
                        },
                    )
                }
        }
    }

    fun removeItem(productId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            cartRepository
                .removeItemFromCart(cartUserId, productId)
                .collect { result ->
                    result.fold(
                        onSuccess = { updatedCart ->
                            // Refresh the entire cart to ensure consistency
                            loadCartWithProducts(updatedCart)
                        },
                        onFailure = { exception ->
                            _uiState.value = CartUiState.Error("Failed to remove item: ${exception.message}")
                        },
                    )
                }
        }
    }

    private fun calculateTotal() {
        _totalPrice.value = _cartItems.value.sumOf { it.product.price * it.quantity }
    }

    fun refreshCart() {
        viewModelScope.launch(Dispatchers.IO) {
            cartRepository
                .refreshCarts(cartUserId)
                .collect { result ->
                    result.fold(
                        onSuccess = { carts ->
                            val latestCart = carts.maxByOrNull { it.date }
                            if (latestCart != null) {
                                loadCartWithProducts(latestCart)
                            } else {
                                _cartItems.value = emptyList()
                                calculateTotal()
                                _uiState.value = CartUiState.Success(emptyList())
                            }
                        },
                        onFailure = { exception ->
                            _uiState.value = CartUiState.Error("Failed to refresh cart: ${exception.message}")
                        },
                    )
                }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            clearCartUseCase(cartUserId).collect { result ->
                    result.fold(
                        onSuccess = {
                            _cartItems.value = emptyList()
                            calculateTotal()
                            _uiState.value = CartUiState.Success(emptyList())
                        },
                        onFailure = { exception ->
                            _uiState.value = CartUiState.Error("Failed to clear cart: ${exception.message}")
                        },
                    )
                }
        }
    }

}

sealed interface CartUiState {
    data object Loading : CartUiState

    data class Success(
        val items: List<CartItem>,
    ) : CartUiState

    data class Error(
        val message: String,
    ) : CartUiState
}
package com.samuelokello.feat.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.core.domain.model.Product
import com.samuelokello.core.domain.repository.CartRepository
import com.samuelokello.core.domain.usecase.auth.GetCurrentUserUseCase
import com.samuelokello.core.domain.usecase.product.GetProductByIdUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val cartRepository: CartRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProductDetailEvent>()
    val events = _events.asSharedFlow()

    fun getProductById(productId: Int) {
        viewModelScope.launch {
            try {
                _state.value = ProductDetailUiState.Loading
                val result = getProductByIdUseCase(productId)

                if (result != null) {
                    _state.value = ProductDetailUiState.Success(product = result)
                } else {
                    _state.value = ProductDetailUiState.Error(message = "Товар не найден")
                }
            } catch (e: Exception) {
                _state.value = ProductDetailUiState.Error(message = e.message ?: "Произошла ошибка")
            }
        }
    }

    fun addToCart(
        product: Product,
        quantity: Int,
    ) {
        viewModelScope.launch {
            val maxCount = product.count.coerceAtLeast(1)
            val qty = quantity.coerceIn(1, maxCount)
            val userId =
                getCurrentUserUseCase()
                    .first()
                    ?.id
                    ?.toInt()
                    ?: 0
            cartRepository.addItemToCart(userId, product.id, qty).collect { result ->
                result.fold(
                    onSuccess = {
                        _events.emit(ProductDetailEvent.AddedToCart(product.title))
                    },
                    onFailure = { e ->
                        _events.emit(
                            ProductDetailEvent.Error(
                                e.message ?: "Не удалось добавить товар в корзину",
                            ),
                        )
                    },
                )
            }
        }
    }
}

sealed interface ProductDetailUiState {
    data object Loading : ProductDetailUiState

    data class Success(
        val product: Product,
    ) : ProductDetailUiState

    data class Error(
        val message: String,
    ) : ProductDetailUiState
}

sealed interface ProductDetailEvent {
    data class AddedToCart(val productName: String) : ProductDetailEvent

    data class Error(val message: String) : ProductDetailEvent
}

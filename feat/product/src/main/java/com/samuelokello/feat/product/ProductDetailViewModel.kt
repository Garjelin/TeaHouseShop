package com.samuelokello.feat.product

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.core.domain.model.Product
import com.samuelokello.core.domain.usecase.product.GetProductByIdUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    // TODO: Добавить CartRepository в Спринте 5
    // private val cartRepository: CartRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val state = _state.asStateFlow()
    
    // События для UI (например, показать Snackbar)
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

    fun addToCart(product: Product) {
        viewModelScope.launch {
            // TODO: Реализовать в Спринте 5 с CartRepository
            Log.d("ProductDetailViewModel", "Добавление в корзину: ${product.title}")
            _events.emit(ProductDetailEvent.AddedToCart(product.title))
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

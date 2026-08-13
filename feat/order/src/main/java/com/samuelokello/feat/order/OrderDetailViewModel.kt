package com.samuelokello.feat.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.core.domain.model.Order
import com.samuelokello.core.domain.usecase.order.GetOrderByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrderDetailViewModel(
    private val getOrderByIdUseCase: GetOrderByIdUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    fun load(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getOrderByIdUseCase(orderId).fold(
                onSuccess = { order ->
                    if (order == null) {
                        _uiState.update {
                            it.copy(isLoading = false, order = null, error = "Заказ не найден")
                        }
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, order = order, error = null)
                        }
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Не удалось загрузить заказ",
                        )
                    }
                },
            )
        }
    }
}

data class OrderDetailUiState(
    val order: Order? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

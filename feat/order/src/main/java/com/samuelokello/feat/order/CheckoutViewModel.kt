package com.samuelokello.feat.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.core.domain.model.CheckoutDraft
import com.samuelokello.core.domain.model.PaymentMethod
import com.samuelokello.core.domain.usecase.auth.GetCurrentUserUseCase
import com.samuelokello.core.domain.usecase.order.CreateOrderUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val createOrderUseCase: CreateOrderUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CheckoutEvent>()
    val events: SharedFlow<CheckoutEvent> = _events.asSharedFlow()

    fun onAddressChange(value: String) {
        _uiState.update { it.copy(address = value, addressError = null, error = null) }
    }

    fun onCommentChange(value: String) {
        _uiState.update { it.copy(comment = value) }
    }

    fun onPaymentSelected(method: PaymentMethod) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun onDeliverySlotSelected(slot: String) {
        _uiState.update { it.copy(deliverySlot = slot) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.address.trim().length < 5) {
            _uiState.update { it.copy(addressError = "Введите полный адрес доставки") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val userId =
                getCurrentUserUseCase()
                    .first()
                    ?.id
                    ?.toInt()
                    ?: 0

            val draft =
                CheckoutDraft(
                    address = state.address,
                    paymentMethod = state.paymentMethod,
                    deliverySlot = state.deliverySlot,
                    comment = state.comment,
                )

            createOrderUseCase(userId, draft).fold(
                onSuccess = { order ->
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.emit(CheckoutEvent.OrderCreated(order.id))
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = e.message ?: "Не удалось оформить заказ",
                        )
                    }
                },
            )
        }
    }

    companion object {
        val DELIVERY_SLOTS =
            listOf(
                "Как можно скорее",
                "Сегодня, 12:00–15:00",
                "Сегодня, 15:00–18:00",
                "Сегодня, 18:00–21:00",
                "Завтра, 12:00–18:00",
            )
    }
}

data class CheckoutUiState(
    val address: String = "",
    val comment: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val deliverySlot: String = CheckoutViewModel.DELIVERY_SLOTS.first(),
    val addressError: String? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

sealed interface CheckoutEvent {
    data class OrderCreated(val orderId: String) : CheckoutEvent
}

package com.samuelokello.feat.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.core.domain.model.Order
import com.samuelokello.core.domain.model.UserProfile
import com.samuelokello.core.domain.usecase.auth.GetCurrentUserUseCase
import com.samuelokello.core.domain.usecase.auth.LogoutUseCase
import com.samuelokello.core.domain.usecase.order.GetUserOrdersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getUserOrdersUseCase: GetUserOrdersUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            logoutUseCase()
            _uiState.update { it.copy(isLoggingOut = false, orders = emptyList()) }
        }
    }

    fun refreshOrders() {
        val user = _uiState.value.user ?: return
        loadOrders(user.id.toInt())
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.update {
                    it.copy(
                        user = user,
                        isLoading = false,
                    )
                }
                if (user != null) {
                    loadOrders(user.id.toInt())
                } else {
                    _uiState.update { it.copy(orders = emptyList(), ordersError = null) }
                }
            }
        }
    }

    private fun loadOrders(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOrdersLoading = true, ordersError = null) }
            getUserOrdersUseCase(userId).fold(
                onSuccess = { orders ->
                    _uiState.update {
                        it.copy(
                            orders = orders,
                            isOrdersLoading = false,
                            ordersError = null,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isOrdersLoading = false,
                            ordersError = e.message ?: "Не удалось загрузить заказы",
                        )
                    }
                },
            )
        }
    }
}

data class ProfileUiState(
    val user: UserProfile? = null,
    val isLoading: Boolean = true,
    val isLoggingOut: Boolean = false,
    val orders: List<Order> = emptyList(),
    val isOrdersLoading: Boolean = false,
    val ordersError: String? = null,
)

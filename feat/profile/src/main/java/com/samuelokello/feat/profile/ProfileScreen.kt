package com.samuelokello.feat.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samuelokello.core.domain.model.Order
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navigateToLogin: () -> Unit,
    navigateToOrderDetails: (String) -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = uiState.user

    LaunchedEffect(uiState.user) {
        if (uiState.user == null && !uiState.isLoading) {
            navigateToLogin()
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            currentUser != null -> {
                Text(
                    text = "Профиль",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(text = "Имя: ${currentUser.displayName}")
                Text(text = "Email: ${currentUser.email}")

                Button(
                    onClick = viewModel::logout,
                    enabled = !uiState.isLoggingOut,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = if (uiState.isLoggingOut) "Выходим..." else "Выйти")
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "История заказов",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    TextButton(onClick = viewModel::refreshOrders) {
                        Text("Обновить")
                    }
                }

                when {
                    uiState.isOrdersLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    uiState.ordersError != null -> {
                        Text(
                            text = uiState.ordersError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                        )
                        OutlinedButton(onClick = viewModel::refreshOrders) {
                            Text("Повторить")
                        }
                    }
                    uiState.orders.isEmpty() -> {
                        Text(
                            text = "Пока нет заказов. Оформите покупку из корзины.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    else -> {
                        uiState.orders.forEach { order ->
                            OrderHistoryItem(
                                order = order,
                                onClick = { navigateToOrderDetails(order.id) },
                            )
                        }
                    }
                }
            }
            else -> {
                Text(
                    text = "Вы не авторизованы",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(
                    onClick = navigateToLogin,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Перейти ко входу")
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryItem(
    order: Order,
    onClick: () -> Unit,
) {
    val dateText =
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru", "RU"))
            .format(Date(order.createdAt))
    val itemsCount = order.items.sumOf { it.quantity }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "№ ${order.id.take(8).uppercase()}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = String.format(Locale("ru", "RU"), "%.2f ₽", order.totalAmount),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Text(
            text = dateText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "$itemsCount поз. · ${order.address}",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

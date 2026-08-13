package com.samuelokello.feat.order

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samuelokello.core.domain.model.Order
import com.samuelokello.core.domain.model.PaymentMethod
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderDetailScreen(
    orderId: String,
    viewModel: OrderDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.load(orderId)
    }

    when {
        uiState.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null && uiState.order == null -> {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = uiState.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        uiState.order != null -> {
            OrderDetailContent(order = uiState.order!!)
        }
    }
}

@Composable
private fun OrderDetailContent(order: Order) {
    val dateText =
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru", "RU"))
            .format(Date(order.createdAt))

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Заказ ${order.id.take(8).uppercase()}",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = dateText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Адрес", style = MaterialTheme.typography.titleMedium)
        Text(text = order.address)

        Text(text = "Доставка", style = MaterialTheme.typography.titleMedium)
        Text(text = order.deliverySlot.ifBlank { "—" })

        Text(text = "Оплата", style = MaterialTheme.typography.titleMedium)
        Text(
            text =
                when (order.paymentMethod) {
                    PaymentMethod.CASH -> "Наличными при получении"
                    PaymentMethod.CARD -> "Картой при получении"
                },
        )

        if (order.comment.isNotBlank()) {
            Text(text = "Комментарий", style = MaterialTheme.typography.titleMedium)
            Text(text = order.comment)
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Товары", style = MaterialTheme.typography.titleMedium)
        order.items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${item.title} × ${item.quantity}",
                    modifier = Modifier.weight(1f),
                )
                Text(text = formatRub(item.price * item.quantity))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "Итого", style = MaterialTheme.typography.titleLarge)
            Text(text = formatRub(order.totalAmount), style = MaterialTheme.typography.titleLarge)
        }
    }
}

internal fun formatRub(value: Double): String =
    String.format(Locale("ru", "RU"), "%.2f ₽", value)

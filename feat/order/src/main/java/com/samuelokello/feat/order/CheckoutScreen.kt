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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.samuelokello.core.domain.model.PaymentMethod
import org.koin.androidx.compose.koinViewModel

@Composable
fun CheckoutScreen(
    navigateToOrderPlaced: (orderId: String) -> Unit,
    viewModel: CheckoutViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CheckoutEvent.OrderCreated -> navigateToOrderPlaced(event.orderId)
            }
        }
    }

    CheckoutContent(
        uiState = uiState,
        onAddressChange = viewModel::onAddressChange,
        onCommentChange = viewModel::onCommentChange,
        onPaymentSelected = viewModel::onPaymentSelected,
        onDeliverySlotSelected = viewModel::onDeliverySlotSelected,
        onSubmit = viewModel::submit,
    )
}

@Composable
private fun CheckoutContent(
    uiState: CheckoutUiState,
    onAddressChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onPaymentSelected: (PaymentMethod) -> Unit,
    onDeliverySlotSelected: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Оформление заказа",
            style = MaterialTheme.typography.headlineSmall,
        )

        OutlinedTextField(
            value = uiState.address,
            onValueChange = onAddressChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Адрес доставки") },
            isError = uiState.addressError != null,
            supportingText = {
                uiState.addressError?.let { Text(it) }
            },
            minLines = 2,
        )

        Text(
            text = "Время доставки",
            style = MaterialTheme.typography.titleMedium,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CheckoutViewModel.DELIVERY_SLOTS.forEach { slot ->
                FilterChip(
                    selected = uiState.deliverySlot == slot,
                    onClick = { onDeliverySlotSelected(slot) },
                    label = { Text(slot) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Text(
            text = "Способ оплаты",
            style = MaterialTheme.typography.titleMedium,
        )
        PaymentOption(
            title = "Наличными при получении",
            selected = uiState.paymentMethod == PaymentMethod.CASH,
            onClick = { onPaymentSelected(PaymentMethod.CASH) },
        )
        PaymentOption(
            title = "Картой при получении",
            selected = uiState.paymentMethod == PaymentMethod.CARD,
            onClick = { onPaymentSelected(PaymentMethod.CARD) },
        )

        OutlinedTextField(
            value = uiState.comment,
            onValueChange = onCommentChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Комментарий к заказу (необязательно)") },
            minLines = 2,
        )

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSubmit,
            enabled = !uiState.isSubmitting,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp),
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.height(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Подтвердить заказ")
            }
        }
    }
}

@Composable
private fun PaymentOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.RadioButton,
                )
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(text = title)
    }
}

package com.example.blatplat.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val ScreenBackground = Color(0xFF222222)
private val AccentTeal = Color(0xFF17C0C3)
private val TextWhite = Color(0xFFFFFFFF)
private val TextCaption = Color(0xFFAAAAAA)
private val SheetSurface = Color(0xFF2E2E2E)
private val MethodCardBackground = Color(0xFF333333)

private data class PaymentMethod(
    val emoji: String,
    val title: String,
    val subtitle: String,
)

private val paymentMethods = listOf(
    PaymentMethod(
        emoji = "💳",
        title = "Банковская карта",
        subtitle = "Visa / Mastercard / Мир",
    ),
    PaymentMethod(
        emoji = "⚡",
        title = "СБП",
        subtitle = "Система быстрых платежей",
    ),
    PaymentMethod(
        emoji = "📱",
        title = "Баланс мобильного",
        subtitle = "Списание с баланса оператора",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    balanceRubles: Int = 1_500,
    modifier: Modifier = Modifier,
    onPaymentMethodSelected: (String) -> Unit = {},
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = SheetSurface,
            contentColor = TextWhite,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(TextCaption.copy(alpha = 0.5f), RoundedCornerShape(2.dp)),
                )
            },
        ) {
            PaymentMethodsContent(
                modifier = Modifier.animateContentSize(),
                onMethodSelected = { method ->
                    onPaymentMethodSelected(method.title)
                    scope.launch {
                        sheetState.hide()
                        showBottomSheet = false
                    }
                },
            )
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ScreenBackground,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Текущий баланс",
                    color = TextCaption,
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = formatBalance(balanceRubles),
                    color = TextWhite,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .animateContentSize(),
            ) {
                Button(
                    onClick = { showBottomSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentTeal,
                        contentColor = Color(0xFF111111),
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = "Пополнить",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodsContent(
    onMethodSelected: (PaymentMethod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = "Выберите способ оплаты",
            color = TextWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(20.dp))
        paymentMethods.forEach { method ->
            PaymentMethodRow(
                method = method,
                onClick = { onMethodSelected(method) },
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PaymentMethodRow(
    method: PaymentMethod,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MethodCardBackground, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = method.emoji,
            fontSize = 28.sp,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = method.title,
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = method.subtitle,
                color = TextCaption,
                fontSize = 13.sp,
            )
        }
    }
}

private fun formatBalance(amount: Int): String {
    val digits = amount.toString()
    val grouped = digits
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()
    return "$grouped ₽"
}

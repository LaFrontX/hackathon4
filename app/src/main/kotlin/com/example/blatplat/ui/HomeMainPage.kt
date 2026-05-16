package com.example.blatplat.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blatplat.R
import com.example.blatplat.data.local.DemoProfileStore
import com.example.blatplat.ui.common.AsyncState
import com.example.blatplat.ui.common.formatRubles
import com.example.blatplat.ui.home.HomeViewModel
import com.example.blatplat.ui.home.SubscriptionOfferDialog
import com.example.blatplat.ui.pager.PagerPageColors
@Composable
fun HomeMainPage(
    demoProfileStore: DemoProfileStore,
    homeFactory: HomeViewModel.Factory,
    colors: PagerPageColors,
    onLogoutClick: () -> Unit,
    onTestNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(factory = homeFactory),
) {
    val haptics = LocalHapticFeedback.current
    val cabinet by demoProfileStore.profile.collectAsStateWithLifecycle()
    val offerState by homeViewModel.offerState.collectAsStateWithLifecycle()
    val offerLoading by homeViewModel.offerLoading.collectAsStateWithLifecycle()
    val purchaseBusy by homeViewModel.purchaseBusy.collectAsStateWithLifecycle()
    val finesCount by homeViewModel.finesCount.collectAsStateWithLifecycle()
    val showOfferDialog by homeViewModel.showOfferDialog.collectAsStateWithLifecycle()

    val currentOffer = (offerState as? AsyncState.Success)?.value
    if (showOfferDialog && currentOffer != null) {
        SubscriptionOfferDialog(
            offer = currentOffer,
            purchaseBusy = purchaseBusy,
            onPurchase = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                homeViewModel.purchaseCurrentOffer()
            },
            onDismiss = { homeViewModel.dismissOfferDialogOnly() },
        )
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "Добро пожаловать",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )
            Text(
                text = "Помощник на платных дорогах",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 6.dp),
            )

            finesCount?.let { count ->
                Text(
                    text = "Штрафов (с сервера): $count",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (count > 30) Color(0xFFE57373) else colors.textSecondary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
            ) {
                Text(
                    text = "Баланс",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textSecondary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatRubles(cabinet.balanceRub),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent,
                )
                Text(
                    text = "${cabinet.lastName} ${cabinet.firstName}".trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            PagerActionButton(
                text = if (offerLoading) "Загрузка…" else "Получить предложение",
                container = colors.accent.copy(alpha = 0.9f),
                content = colors.onAccent,
                enabled = !offerLoading,
                size = PagerButtonSize.Large,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    homeViewModel.fetchOffer()
                },
            )

            when (val state = offerState) {
                is AsyncState.Success -> Unit
                is AsyncState.Error -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        color = Color(0xFFE57373),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedButton(
                        onClick = state.retry,
                        modifier = Modifier.padding(top = 4.dp),
                    ) { Text("Повторить") }
                }
                AsyncState.Loading -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    CircularProgressIndicator(
                        color = colors.accent,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
                null -> Unit
            }

            Spacer(modifier = Modifier.height(24.dp))

            PagerActionButton(
                text = "Выйти",
                container = colors.surface,
                content = colors.textPrimary,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLogoutClick()
                },
            )
            Spacer(modifier = Modifier.height(10.dp))
            PagerActionButton(
                text = "Тест: уведомление",
                container = colors.surface.copy(alpha = 0.7f),
                content = colors.textSecondary,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTestNotificationClick()
                },
            )
        }

        Box(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight()
                .padding(end = 4.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Image(
                painter = painterResource(id = R.drawable.car),
                contentDescription = "Автомобиль",
                modifier = Modifier
                    .fillMaxHeight(0.72f)
                    .widthIn(max = 200.dp)
                    .align(Alignment.CenterEnd),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

internal enum class PagerButtonSize(val height: androidx.compose.ui.unit.Dp, val fontSize: androidx.compose.ui.unit.TextUnit) {
    Normal(height = 52.dp, fontSize = 15.sp),
    Large(height = 72.dp, fontSize = 20.sp),
}

@Composable
internal fun PagerActionButton(
    text: String,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    size: PagerButtonSize = PagerButtonSize.Normal,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(size.height),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = container.copy(alpha = 0.5f),
            disabledContentColor = content.copy(alpha = 0.7f),
        ),
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = size.fontSize)
    }
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.blatplat.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blatplat.domain.model.DriverProfile
import com.example.blatplat.ui.common.ScreenTitleText
import com.example.blatplat.ui.common.formatRubles
import androidx.compose.foundation.background
import com.example.blatplat.ui.common.AsyncStateView
import com.example.blatplat.ui.pager.PagerPageColors
import com.example.blatplat.ui.pager.PagerThemes
import com.example.blatplat.ui.payment.PaymentViewModel
import com.example.blatplat.ui.theme.MtAccent
import com.example.blatplat.ui.theme.MtBackground
import com.example.blatplat.ui.theme.MtOnPrimary
import com.example.blatplat.ui.theme.MtSurface
import com.example.blatplat.ui.theme.MtTextPrimary
import com.example.blatplat.ui.theme.MtTextSecondary
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private enum class MockPayMethod {
    Card,
    Sbp,
}

private fun MockPayMethod.labelRu(): String = when (this) {
    MockPayMethod.Card -> "Карта"
    MockPayMethod.Sbp -> "СБП"
}



private val CheckoutAmountPresets = listOf(500, 1000, 2000, 3500)

@Composable
fun PaymentScreen(
    initialAmount: String,
    factory: PaymentViewModel.Factory,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    colors: PagerPageColors = PagerThemes.Payment,
    showTopBarBack: Boolean = true,
    viewModel: PaymentViewModel = viewModel(factory = factory),
) {
    val haptics = LocalHapticFeedback.current
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val topUpBusy by viewModel.topUpBusy.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val topUpFromLink = remember(initialAmount) { initialAmount.toIntOrNull() ?: 0 }
    val scope = rememberCoroutineScope()
    var showCheckoutSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val sheetOpenLatest = rememberUpdatedState(showCheckoutSheet)

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(Unit) {
        viewModel.snackMessages.collectLatest { msg ->
            if (msg.contains("пополнен", ignoreCase = true)) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                if (sheetOpenLatest.value) {
                    scope.launch {
                        sheetState.hide()
                        showCheckoutSheet = false
                    }
                }
            }
            snack.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { ScreenTitleText(text = "Пополнение баланса", color = colors.textPrimary) },
                navigationIcon = {
                    if (showTopBarBack) {
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateBack()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
                                tint = colors.accent,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snack) },
    ) { inner ->
        Surface(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            color = colors.background,
        ) {
            AsyncStateView(
                state = profile,
                loading = { BalanceSkeletonCard() },
                success = { p ->
                    PaymentContent(
                        profile = p,
                        colors = colors,
                        topUpFromLink = topUpFromLink,
                        onOpenCheckout = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            showCheckoutSheet = true
                        },
                    )
                },
            )
        }

        if (showCheckoutSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCheckoutSheet = false },
                sheetState = sheetState,
                containerColor = colors.surface,
                contentColor = colors.textPrimary,
            ) {
                CheckoutSheet(
                    colors = colors,
                    deeplinkSuggested = topUpFromLink,
                    topUpBusy = topUpBusy,
                    onDismiss = {
                        scope.launch {
                            sheetState.hide()
                            showCheckoutSheet = false
                        }
                    },
                    onConfirmPay = { method, amountRub ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        val hint = "${method.labelRu()}, ${formatRubles(amountRub)}"
                        viewModel.topUp(amountRub = amountRub, paymentHint = hint)
                    },
                )
            }
        }
    }
}

/** Вращение иконки во время имитации/запроса пополнения. */
@Composable
private fun CheckoutProgressIcon(
    busy: Boolean,
    tint: Color = MtOnPrimary,
    modifier: Modifier = Modifier,
) {
    if (!busy) {
        Icon(
            imageVector = Icons.Filled.Sync,
            contentDescription = null,
            modifier = modifier.size(22.dp),
            tint = tint,
        )
        return
    }
    val infinite = rememberInfiniteTransition(label = "topup_spin")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(560, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rot",
    )
    Icon(
        imageVector = Icons.Filled.Sync,
        contentDescription = null,
        modifier = modifier
            .size(22.dp)
            .rotate(rotation),
        tint = tint,
    )
}

@Composable
private fun CheckoutSheet(
    colors: PagerPageColors,
    deeplinkSuggested: Int,
    topUpBusy: Boolean,
    onDismiss: () -> Unit,
    onConfirmPay: (MockPayMethod, Int) -> Unit,
) {
    var method by remember { mutableStateOf(MockPayMethod.Card) }
    var amountRub by remember {
        mutableIntStateOf(
            if (deeplinkSuggested in CheckoutAmountPresets) deeplinkSuggested else 1000,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
    ) {
        Text(
            text = "Способ оплаты",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.textPrimary,
        )
        Text(
            text = "Выберите карту или СБП и сумму. Пополнение только на устройстве, без отправки на сервер.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            MethodChoiceCard(
                colors = colors,
                title = "Карта",
                subtitle = "Visa / Mastercard / Мир",
                icon = Icons.Filled.Payment,
                selected = method == MockPayMethod.Card,
                onClick = { method = MockPayMethod.Card },
            )
            MethodChoiceCard(
                colors = colors,
                title = "СБП",
                subtitle = "Система быстрых платежей",
                icon = Icons.Filled.Smartphone,
                selected = method == MockPayMethod.Sbp,
                onClick = { method = MockPayMethod.Sbp },
            )
        }

        Spacer(modifier = Modifier.height(22.dp))
        Text("Сумма", color = colors.textSecondary, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CheckoutAmountPresets.forEach { preset ->
                val selected = preset == amountRub
                FilterChip(
                    selected = selected,
                    onClick = { amountRub = preset },
                    enabled = !topUpBusy,
                    label = {
                        Text(
                            text = formatRubles(preset),
                            fontSize = 13.sp,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.accent,
                        selectedLabelColor = colors.onAccent,
                        containerColor = colors.surface,
                        labelColor = colors.textPrimary,
                        disabledSelectedContainerColor = colors.accent.copy(alpha = 0.45f),
                    ),
                    border = BorderStroke(
                        width = if (selected) 1.dp else 0.dp,
                        color = if (selected) colors.accent else Color.Transparent,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        Button(
            onClick = { onConfirmPay(method, amountRub) },
            enabled = !topUpBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = colors.onAccent,
                disabledContainerColor = colors.accent.copy(alpha = 0.45f),
            ),
            shape = RoundedCornerShape(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CheckoutProgressIcon(busy = topUpBusy, tint = colors.onAccent)
                Text(
                    text = if (topUpBusy) "Обрабатываем оплату…" else "Подтвердить пополнение",
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Text(
            text = "Отмена",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .clickable(enabled = !topUpBusy, onClick = onDismiss),
            color = if (!topUpBusy) colors.accent else colors.textSecondary,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun RowScope.MethodChoiceCard(
    colors: PagerPageColors,
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) colors.accent.copy(alpha = 0.22f) else colors.surface

    Surface(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(104.dp),
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = if (selected) BorderStroke(2.dp, colors.accent) else null,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(icon, contentDescription = null, tint = colors.accent)
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun BalanceSkeletonCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(20.dp))
                .shimmer()
                .background(Color.White.copy(alpha = 0.06f)),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .shimmer()
                .background(Color.White.copy(alpha = 0.06f)),
        )
    }
}

@Composable
private fun PaymentContent(
    profile: DriverProfile,
    colors: PagerPageColors,
    topUpFromLink: Int,
    onOpenCheckout: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Баланс",
                color = colors.textSecondary,
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = formatRubles(profile.balanceRub),
                color = colors.textPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
            )
            if (topUpFromLink > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "По ссылке: ${formatRubles(topUpFromLink)}",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .animateContentSize(),
        ) {
            Button(
                onClick = onOpenCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.onAccent,
                ),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    text = "Пополнить баланс",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

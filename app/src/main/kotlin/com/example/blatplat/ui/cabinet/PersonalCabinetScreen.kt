package com.example.blatplat.ui.cabinet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blatplat.data.local.DemoProfileStore
import com.example.blatplat.ui.common.ScreenTitleText
import com.example.blatplat.ui.pager.PagerPageColors
import com.example.blatplat.ui.pager.PagerThemes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalCabinetScreen(
    demoProfileStore: DemoProfileStore,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    colors: PagerPageColors = PagerThemes.Cabinet,
    showBackButton: Boolean = true,
) {
    val haptics = LocalHapticFeedback.current
    val cabinet by demoProfileStore.profile.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { ScreenTitleText(text = "Личный кабинет", color = colors.textPrimary) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateBack()
                            },
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
                                tint = colors.accent,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
            )
        },
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            CabinetFieldCard(label = "Фамилия", value = cabinet.lastName, colors = colors)
            CabinetFieldCard(label = "Имя", value = cabinet.firstName, colors = colors)
            CabinetFieldCard(
                label = "Госномер ТС",
                value = cabinet.vehicleRegNumber,
                colors = colors,
                highlight = true,
            )
            CabinetFieldCard(
                label = "Поездок (учётный период)",
                value = cabinet.tripsCompleted.toString(),
                colors = colors,
            )
            CabinetFieldCard(
                label = "Тариф",
                value = formatTariffField(cabinet),
                colors = colors,
                highlight = cabinet.activeTariffName != null,
            )
        }
    }
}

private fun formatTariffField(cabinet: com.example.blatplat.domain.model.DemoCabinetProfile): String {
    val name = cabinet.activeTariffName
    val percent = cabinet.tariffDiscountPercent
    val until = cabinet.tariffDiscountUntil
    if (name.isNullOrBlank()) return "Не выбран — получите предложение на главной"
    return buildString {
        append(name)
        if (percent != null) append(", скидка $percent%")
        if (!until.isNullOrBlank()) append(" до $until")
    }
}

@Composable
private fun CabinetFieldCard(
    label: String,
    value: String,
    colors: PagerPageColors,
    highlight: Boolean = false,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) colors.accent.copy(alpha = 0.12f) else colors.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (highlight) colors.accent else colors.textPrimary,
                fontSize = if (highlight) 22.sp else 20.sp,
            )
        }
    }
}

package com.example.blatplat.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.blatplat.domain.model.TripRecord
import com.example.blatplat.ui.common.ScreenTitleText
import com.example.blatplat.ui.pager.PagerPageColors
import com.example.blatplat.ui.pager.PagerThemes
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripHistoryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    colors: PagerPageColors = PagerThemes.History,
    showBackButton: Boolean = true,
) {
    val haptics = LocalHapticFeedback.current
    var items by remember { mutableStateOf(paidRoadTrips()) }
    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()

    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(900)
            items = paidRoadTrips()
            refreshing = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    ScreenTitleText(text = "История поездок", color = colors.textPrimary)
                },
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
        Surface(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize(),
            color = colors.background,
        ) {
            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    refreshing = true
                },
                state = state,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    item {
                        Text(
                            text = "Только платные трассы и участки",
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    items(items, key = { "${it.route}_${it.whenText}" }) { trip ->
                        ListItem(
                            headlineContent = { Text(trip.route, color = colors.textPrimary) },
                            supportingContent = { Text(trip.whenText, color = colors.textSecondary) },
                            trailingContent = {
                                Text(
                                    "${trip.fareRub} ₽",
                                    color = colors.accent,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = colors.background),
                        )
                    }
                }
            }
        }
    }
}

/** Поездки только по платным трассам / улицам. */
private fun paidRoadTrips(): List<TripRecord> = listOf(
    TripRecord("М-11 «Нева» (платный участок)", 320, "Сегодня, 09:12"),
    TripRecord("М-4 «Дон» — обход Туллы", 285, "Вчера, 18:40"),
    TripRecord("ЦКАД — съезд на Ленинградское ш.", 410, "15 мая, 07:55"),
    TripRecord("Северный дублёр Кутузовского проспекта", 95, "14 мая, 20:18"),
    TripRecord("Платная полоса Волоколамского шоссе", 78, "12 мая, 11:30"),
    TripRecord("М-12 «Восток» — участок до Орехово-Зуево", 360, "10 мая, 16:02"),
)

package com.example.blatplat.ui.pager

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

data class PagerPageColors(
    val background: Color,
    val surface: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val onAccent: Color,
    val title: String,
)

fun lerpPagerColors(start: PagerPageColors, end: PagerPageColors, fraction: Float): PagerPageColors {
    val t = fraction.coerceIn(0f, 1f)
    return PagerPageColors(
        background = lerp(start.background, end.background, t),
        surface = lerp(start.surface, end.surface, t),
        accent = lerp(start.accent, end.accent, t),
        textPrimary = lerp(start.textPrimary, end.textPrimary, t),
        textSecondary = lerp(start.textSecondary, end.textSecondary, t),
        onAccent = lerp(start.onAccent, end.onAccent, t),
        title = if (t < 0.5f) start.title else end.title,
    )
}

object PagerThemes {
    val Main = PagerPageColors(
        background = Color(0xFF1A2428),
        surface = Color(0xFF2A3438),
        accent = Color(0xFF17C0C3),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFAAB4B8),
        onAccent = Color(0xFF111111),
        title = "Главная",
    )

    val Payment = PagerPageColors(
        background = Color(0xFF0F1B2E),
        surface = Color(0xFF1A2A42),
        accent = Color(0xFF5B9BFF),
        textPrimary = Color(0xFFF0F4FF),
        textSecondary = Color(0xFF9EB0D4),
        onAccent = Color(0xFF0A1020),
        title = "Пополнение",
    )

    val History = PagerPageColors(
        background = Color(0xFF231A2E),
        surface = Color(0xFF322438),
        accent = Color(0xFFC77DFF),
        textPrimary = Color(0xFFF5EEFF),
        textSecondary = Color(0xFFB8A8C8),
        onAccent = Color(0xFF1A1020),
        title = "История",
    )

    val Cabinet = PagerPageColors(
        background = Color(0xFF1A2820),
        surface = Color(0xFF253528),
        accent = Color(0xFF6BCB77),
        textPrimary = Color(0xFFEEF8F0),
        textSecondary = Color(0xFFA8C4AE),
        onAccent = Color(0xFF102018),
        title = "Кабинет",
    )

    val all: List<PagerPageColors> = listOf(Main, Payment, History, Cabinet)
}

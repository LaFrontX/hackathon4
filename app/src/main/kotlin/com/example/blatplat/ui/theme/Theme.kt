package com.example.blatplat.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Material 3 с фиксированной тёмной палитрой хакатона (акцент #17C0C3). Dynamic Color отключён по умолчанию. */
private val MtDarkScheme = darkColorScheme(
    primary = MtAccent,
    onPrimary = MtOnPrimary,
    secondary = MtTextSecondary,
    onSecondary = MtTextPrimary,
    background = MtBackground,
    onBackground = MtTextPrimary,
    surface = MtSurface,
    onSurface = MtTextPrimary,
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = MtTextSecondary,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
)

@Composable
fun BlatPlatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }
        darkTheme -> MtDarkScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

// Алиас для явного использования бренд-схемы на всех экранах хакатона.
@Composable
fun MoscowTransportTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MtDarkScheme,
        typography = Typography,
        content = content,
    )
}

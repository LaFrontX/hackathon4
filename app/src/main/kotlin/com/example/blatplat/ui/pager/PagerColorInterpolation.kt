package com.example.blatplat.ui.pager

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlin.math.abs

@Composable
fun rememberInterpolatedPagerColors(
    pagerState: PagerState,
    themes: List<PagerPageColors> = PagerThemes.all,
): PagerPageColors {
    val colors by remember(pagerState) {
        derivedStateOf {
            val page = pagerState.currentPage.coerceIn(0, themes.lastIndex)
            val offset = pagerState.currentPageOffsetFraction
            val targetPage = when {
                offset > 0f -> (page + 1).coerceAtMost(themes.lastIndex)
                offset < 0f -> (page - 1).coerceAtLeast(0)
                else -> page
            }
            val fraction = abs(offset).coerceIn(0f, 1f)
            if (fraction == 0f || page == targetPage) {
                themes[page]
            } else {
                lerpPagerColors(themes[page], themes[targetPage], fraction)
            }
        }
    }
    return colors
}

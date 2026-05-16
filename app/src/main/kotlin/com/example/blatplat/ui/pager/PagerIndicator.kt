package com.example.blatplat.ui.pager

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun PagerIndicator(
    pagerState: PagerState,
    themes: List<PagerPageColors>,
    activeColors: PagerPageColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        themes.forEachIndexed { index, theme ->
            val selected = pagerState.currentPage == index
            val dotColor by animateColorAsState(
                targetValue = if (selected) activeColors.accent else activeColors.textSecondary.copy(alpha = 0.35f),
                animationSpec = tween(350),
                label = "dot_color",
            )
            val width by animateFloatAsState(
                targetValue = if (selected) 28f else 8f,
                animationSpec = tween(350),
                label = "dot_width",
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width.dp)
                    .clip(if (selected) RoundedCornerShape(4.dp) else CircleShape)
                    .background(dotColor),
            )
        }
    }
}

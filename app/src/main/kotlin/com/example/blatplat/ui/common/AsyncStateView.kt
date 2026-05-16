package com.example.blatplat.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> AsyncStateView(
    state: AsyncState<T>,
    modifier: Modifier = Modifier,
    loading: @Composable () -> Unit = {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    },
    success: @Composable (T) -> Unit,
) {
    Crossfade(
        targetState = state,
        animationSpec = tween(durationMillis = 280),
        label = "async_state",
    ) { s ->
        when (s) {
            AsyncState.Loading -> Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { loading() }

            is AsyncState.Error -> Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = s.message,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Button(
                        onClick = s.retry,
                        modifier = Modifier.padding(top = 16.dp),
                    ) { Text("Повторить") }
                }
            }

            is AsyncState.Success -> Box(modifier = modifier.fillMaxSize()) {
                success(s.value)
            }
        }
    }
}

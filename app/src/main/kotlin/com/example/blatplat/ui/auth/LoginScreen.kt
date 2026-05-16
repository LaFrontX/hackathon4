package com.example.blatplat.ui.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blatplat.ui.theme.MtAccent
import com.example.blatplat.ui.theme.MtBackground
import com.example.blatplat.ui.theme.MtOnPrimary
import com.example.blatplat.ui.theme.MtTextPrimary
import com.example.blatplat.ui.theme.MtTextSecondary
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    factory: AuthViewModel.Factory,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(factory = factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current
    val shake = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        viewModel.shakeSignal.collectLatest {
            repeat(4) { i ->
                val dir = if (i % 2 == 0) 1f else -1f
                val mag = (14f - i * 2f).coerceAtLeast(4f) * dir
                shake.animateTo(mag, tween(durationMillis = 38))
            }
            shake.animateTo(0f, tween(durationMillis = 60))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackMessages.collectLatest { msg ->
            snack.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MtBackground,
        snackbarHost = { SnackbarHost(snack) },
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .fillMaxSize()
                .graphicsLayer { translationX = shake.value },
        ) {
            Text(
                text = "Московский транспорт",
                style = MaterialTheme.typography.headlineMedium,
                color = MtTextPrimary,
            )
            Text(
                text = "Войдите, чтобы продолжить",
                style = MaterialTheme.typography.bodyLarge,
                color = MtTextSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "Демо для жюри: номер 8 800 555 35 35, пароль admin",
                style = MaterialTheme.typography.bodyMedium,
                color = MtAccent,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Телефон") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MtTextPrimary,
                    unfocusedTextColor = MtTextPrimary,
                    focusedBorderColor = MtAccent,
                    unfocusedBorderColor = MtTextSecondary,
                    cursorColor = MtAccent,
                    focusedLabelColor = MtTextSecondary,
                    unfocusedLabelColor = MtTextSecondary,
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Пароль") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MtTextPrimary,
                    unfocusedTextColor = MtTextPrimary,
                    focusedBorderColor = MtAccent,
                    unfocusedBorderColor = MtTextSecondary,
                    cursorColor = MtAccent,
                    focusedLabelColor = MtTextSecondary,
                    unfocusedLabelColor = MtTextSecondary,
                ),
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.login()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MtAccent,
                    contentColor = MtOnPrimary,
                ),
            ) {
                Text(if (state.isLoading) "Вход…" else "Войти")
            }
        }
    }
}

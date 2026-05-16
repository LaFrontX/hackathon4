package com.example.blatplat.data.remote

import com.example.blatplat.BuildConfig

/**
 * Базовый URL задаётся в [gradle.properties] как API_BASE_URL.
 * Эмулятор → localhost ПК: http://10.0.2.2:8000/
 * Устройство в той же сети → LAN IP компьютера с Docker.
 */
object ApiConfig {
    val baseUrl: String = BuildConfig.API_BASE_URL
}

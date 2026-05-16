package com.example.blatplat.domain.model

/** Результат входа: доменная модель без привязки к JSON. */
data class AuthResult(
    val token: String,
    val driverId: String,
)

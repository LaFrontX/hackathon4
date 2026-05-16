package com.example.blatplat.domain.model

/** Профиль водителя для экрана баланса. */
data class DriverProfile(
    val driverId: String,
    val displayName: String,
    val balanceRub: Int,
)

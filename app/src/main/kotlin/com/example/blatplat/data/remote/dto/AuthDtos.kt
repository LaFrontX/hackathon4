package com.example.blatplat.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    val phone: String,
    val password: String,
)

data class AuthResponse(
    val token: String,
    @SerializedName("driver_id") val driverId: String,
)

data class ProfileResponse(
    @SerializedName("driver_id") val driverId: String? = null,
    val name: String? = null,
    val balance: Int,
)

data class TopUpRequest(
    val amount: Int,
)

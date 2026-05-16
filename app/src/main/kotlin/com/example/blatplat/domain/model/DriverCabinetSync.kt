package com.example.blatplat.domain.model

data class DriverCabinetSync(
    val firstName: String,
    val lastName: String,
    val vehicleRegNumber: String?,
    val tripsCompleted: Int?,
    val balanceRub: Int,
    val rating: Double? = null,
    val offeredSubscription: String? = null,
)

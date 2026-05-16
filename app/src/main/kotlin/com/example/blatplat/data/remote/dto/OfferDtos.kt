package com.example.blatplat.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Статус водителя с бэкенда (штрафы и пр.). */
data class DriverStatusResponse(
    @SerializedName("driver_id") val driverId: String? = null,
    @SerializedName("fines_count") val finesCount: Int = 0,
)

/** Текущее предложение / тариф с платной дороги. */
data class OfferResponse(
    @SerializedName("offer_id") val offerId: String,
    @SerializedName("tariff_name") val tariffName: String,
    val title: String,
    val description: String? = null,
    @SerializedName("price_rub") val priceRub: Int,
    @SerializedName("valid_until") val validUntil: String? = null,
)

data class PurchaseOfferRequest(
    @SerializedName("offer_id") val offerId: String,
)

data class PurchaseOfferResponse(
    val success: Boolean,
    val message: String? = null,
    @SerializedName("new_balance") val newBalance: Int? = null,
)

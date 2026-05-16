package com.example.blatplat.data.remote.dto

import com.google.gson.annotations.SerializedName

/** GET /mobile/driver/{id} */
data class DriverResponse(
    val id: Int,
    val name: String,
    val balance: Double,
    val rating: Double,
    @SerializedName("fines_count") val finesCount: Int,
    @SerializedName("trips_per_month") val tripsPerMonth: Int,
    @SerializedName("offered_subscription") val offeredSubscription: String,
)

/** GET /mobile/prediction/{id} */
data class PredictionResponse(
    @SerializedName("driver_id") val driverId: Int,
    @SerializedName("predicted_expenses") val predictedExpenses: Double,
    val confidence: Double,
    val recommendations: List<String>,
    @SerializedName("offered_subscription") val offeredSubscription: String?,
    @SerializedName("discount_percent") val discountPercent: Int,
)

data class TripRequest(
    @SerializedName("distance_km") val distanceKm: Double,
    @SerializedName("avg_speed") val avgSpeed: Double,
    @SerializedName("fuel_cost") val fuelCost: Double,
)

data class BalanceResponse(
    @SerializedName("new_balance") val newBalance: Double,
)

data class FineResponse(
    @SerializedName("fines_count") val finesCount: Int,
    val rating: Double,
)

data class TripResponse(
    @SerializedName("trips_per_month") val tripsPerMonth: Int,
    val balance: Double,
)

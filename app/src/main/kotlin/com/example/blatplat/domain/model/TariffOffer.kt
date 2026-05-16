package com.example.blatplat.domain.model

data class TariffTier(
    val discountPercent: Int,
    val name: String,
)

data class TariffOffer(
    val offerId: String,
    val tariffName: String,
    val title: String,
    val description: String?,
    val priceRub: Int,
    val discountValidUntil: String,
    val recommendedTier: TariffTier,
    val tiers: List<TariffTier> = SubscriptionTiers.all,
)

object SubscriptionTiers {
    val all = listOf(
        TariffTier(15, "Роскошный максимум"),
        TariffTier(10, "sasa-lele"),
        TariffTier(5, "базовый минимум"),
        TariffTier(0, "нишиша"),
    )

    val recommended: TariffTier = all.first()
}

data class DriverStatus(
    val driverId: String?,
    val finesCount: Int,
)

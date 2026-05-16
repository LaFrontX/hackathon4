package com.example.blatplat.domain.model

/**
 * Локальный профиль водителя для демо: не привязан к API.
 */
data class DemoCabinetProfile(
    val firstName: String,
    val lastName: String,
    /** Госномер ТС в отображаемом виде. */
    val vehicleRegNumber: String,
    /** Число завершённых поездок (имитация). */
    val tripsCompleted: Int,
    val balanceRub: Int,
    val activeTariffName: String? = null,
    val tariffDiscountPercent: Int? = null,
    val tariffDiscountUntil: String? = null,
) {
    fun toDriverProfile(driverId: String = "demo_local"): DriverProfile =
        DriverProfile(
            driverId = driverId,
            displayName = "$lastName $firstName".trim(),
            balanceRub = balanceRub,
        )
}

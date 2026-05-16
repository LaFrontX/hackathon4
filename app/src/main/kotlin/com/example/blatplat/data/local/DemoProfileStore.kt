package com.example.blatplat.data.local

import com.example.blatplat.domain.model.DemoCabinetProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Имитация личного кабинета и баланса в памяти процесса: без DataStore/API.
 * Баланс главного экрана и экран пополнения читаются из одного потока.
 */
class DemoProfileStore {

    private val initial = DemoCabinetProfile(
        firstName = "Алексей",
        lastName = "Иванов",
        vehicleRegNumber = "А 123 ВС 977",
        tripsCompleted = 143,
        balanceRub = 1_520,
    )

    private val _profile = MutableStateFlow(initial)
    val profile: StateFlow<DemoCabinetProfile> = _profile.asStateFlow()

    fun addBalanceRub(amountRub: Int) {
        if (amountRub <= 0) return
        _profile.update { it.copy(balanceRub = it.balanceRub + amountRub) }
    }

    fun tryDeductBalanceRub(amountRub: Int): Boolean {
        if (amountRub <= 0) return false
        val current = _profile.value
        if (current.balanceRub < amountRub) return false
        _profile.update { it.copy(balanceRub = it.balanceRub - amountRub) }
        return true
    }

    fun setBalanceRub(balanceRub: Int) {
        _profile.update { it.copy(balanceRub = balanceRub.coerceAtLeast(0)) }
    }

    fun applyFromServer(
        balanceRub: Int? = null,
        firstName: String? = null,
        lastName: String? = null,
        vehicleRegNumber: String? = null,
        tripsCompleted: Int? = null,
    ) {
        _profile.update { current ->
            current.copy(
                balanceRub = balanceRub ?: current.balanceRub,
                firstName = firstName?.takeIf { it.isNotBlank() } ?: current.firstName,
                lastName = lastName?.takeIf { it.isNotBlank() } ?: current.lastName,
                vehicleRegNumber = vehicleRegNumber ?: current.vehicleRegNumber,
                tripsCompleted = tripsCompleted ?: current.tripsCompleted,
            )
        }
    }

    fun applyTariffFromOffer(tariffName: String, discountPercent: Int, validUntil: String) {
        _profile.update {
            it.copy(
                activeTariffName = tariffName,
                tariffDiscountPercent = discountPercent,
                tariffDiscountUntil = validUntil,
            )
        }
    }
}

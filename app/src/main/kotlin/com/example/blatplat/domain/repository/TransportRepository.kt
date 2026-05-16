package com.example.blatplat.domain.repository

import com.example.blatplat.domain.model.AuthResult
import com.example.blatplat.domain.model.DriverCabinetSync
import com.example.blatplat.domain.model.DriverProfile
import com.example.blatplat.domain.model.DriverStatus
import com.example.blatplat.domain.model.TariffOffer

/** Контракт репозитория: UI/ViewModel зависят только от domain, не от Retrofit DTO. */
interface TransportRepository {
    suspend fun login(phone: String, password: String): Result<AuthResult>
    suspend fun getProfile(): Result<DriverProfile>
    suspend fun syncDriverFromApi(): Result<DriverCabinetSync>
    suspend fun topUp(amountRub: Int): Result<Int>
    suspend fun getDriverStatus(): Result<DriverStatus>
    suspend fun getCurrentOffer(): Result<TariffOffer>
    suspend fun purchaseOffer(offerId: String): Result<Int?>
}

package com.example.blatplat.data.repository

import com.example.blatplat.data.auth.DemoAuth
import com.example.blatplat.data.remote.ApiService
import com.example.blatplat.data.remote.BackendConfig
import com.example.blatplat.domain.model.AuthResult
import com.example.blatplat.domain.model.DriverCabinetSync
import com.example.blatplat.domain.model.DriverProfile
import com.example.blatplat.domain.model.DriverStatus
import com.example.blatplat.domain.model.SubscriptionTiers
import com.example.blatplat.domain.model.TariffOffer
import com.example.blatplat.domain.repository.TransportRepository
import retrofit2.HttpException
import java.util.Calendar
import kotlin.math.roundToInt

class TransportRepositoryImpl(
    private val api: ApiService,
    private val driverId: Int = BackendConfig.DEFAULT_DRIVER_ID,
) : TransportRepository {

    override suspend fun login(phone: String, password: String): Result<AuthResult> =
        DemoAuth.tryLogin(phoneRaw = phone, password = password)

    override suspend fun getProfile(): Result<DriverProfile> =
        runCatching {
            val d = api.getDriver(driverId)
            DriverProfile(
                driverId = d.id.toString(),
                displayName = d.name,
                balanceRub = d.balance.roundToInt(),
            )
        }.mapNetworkErrors()

    override suspend fun syncDriverFromApi(): Result<DriverCabinetSync> =
        runCatching {
            val d = api.getDriver(driverId)
            val (lastName, firstName) = splitDriverName(d.name)
            DriverCabinetSync(
                firstName = firstName,
                lastName = lastName,
                vehicleRegNumber = null,
                tripsCompleted = d.tripsPerMonth,
                balanceRub = d.balance.roundToInt(),
                rating = d.rating,
                offeredSubscription = d.offeredSubscription,
            )
        }.mapNetworkErrors()

    override suspend fun topUp(amountRub: Int): Result<Int> =
        runCatching {
            api.addBalance(driverId, amountRub.toDouble()).newBalance.roundToInt()
        }.mapNetworkErrors()

    override suspend fun getDriverStatus(): Result<DriverStatus> =
        runCatching {
            val d = api.getDriver(driverId)
            DriverStatus(
                driverId = d.id.toString(),
                finesCount = d.finesCount,
            )
        }.mapNetworkErrors()

    override suspend fun getCurrentOffer(): Result<TariffOffer> =
        runCatching {
            val p = api.getPrediction(driverId)
            predictionToOffer(p)
        }.mapNetworkErrors()

    override suspend fun purchaseOffer(offerId: String): Result<Int?> =
        runCatching {
            val p = api.getPrediction(driverId)
            val price = p.predictedExpenses.roundToInt()
            val driver = api.getDriver(driverId)
            if (driver.balance < price) error("Недостаточно средств на балансе")
            api.addBalance(driverId, -price.toDouble()).newBalance.roundToInt()
        }.mapNetworkErrors()
}

private fun discountValidUntilOneMonth(): String {
    val c = Calendar.getInstance()
    c.add(Calendar.MONTH, 1)
    return "%02d.%02d.%04d".format(
        c.get(Calendar.DAY_OF_MONTH),
        c.get(Calendar.MONTH) + 1,
        c.get(Calendar.YEAR),
    )
}

private fun splitDriverName(fullName: String): Pair<String, String> {
    val parts = fullName.trim().split(Regex("\\s+"), limit = 2)
    return when (parts.size) {
        0 -> "Иванов" to "Алексей"
        1 -> parts[0] to parts[0]
        else -> parts[0] to parts[1]
    }
}

private fun predictionToOffer(p: com.example.blatplat.data.remote.dto.PredictionResponse): TariffOffer {
    val recommended = SubscriptionTiers.recommended
    val validUntil = discountValidUntilOneMonth()
    return TariffOffer(
        offerId = recommended.name,
        tariffName = recommended.name,
        title = "Месячный абонемент",
        description = null,
        priceRub = p.predictedExpenses.roundToInt(),
        discountValidUntil = validUntil,
        recommendedTier = recommended,
        tiers = SubscriptionTiers.all,
    )
}

private fun <T> Result<T>.mapNetworkErrors(): Result<T> {
    val ex = exceptionOrNull() ?: return this
    return Result.failure(mapException(ex))
}

private fun mapException(ex: Throwable): Throwable = when (ex) {
    is HttpException -> {
        val body = ex.response()?.errorBody()?.string().orEmpty()
        IllegalStateException("HTTP ${ex.code()}: ${body.ifBlank { ex.message.orEmpty() }}")
    }
    else -> ex
}

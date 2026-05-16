package com.example.blatplat.data.remote

import com.example.blatplat.data.remote.dto.BalanceResponse
import com.example.blatplat.data.remote.dto.DriverResponse
import com.example.blatplat.data.remote.dto.FineResponse
import com.example.blatplat.data.remote.dto.PredictionResponse
import com.example.blatplat.data.remote.dto.TripRequest
import com.example.blatplat.data.remote.dto.TripResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Контракт API из инструкции бэкенд-разработчика.
 * Базовый URL: [NetworkModule] → `http://10.0.2.2:8000/` (эмулятор).
 */
interface ApiService {

    @GET("mobile/driver/{id}")
    suspend fun getDriver(@Path("id") id: Int): DriverResponse

    @GET("mobile/prediction/{id}")
    suspend fun getPrediction(@Path("id") id: Int): PredictionResponse

    @POST("drivers/{id}/add-balance")
    suspend fun addBalance(
        @Path("id") id: Int,
        @Query("amount") amount: Double,
    ): BalanceResponse

    @POST("drivers/{id}/add-fine")
    suspend fun addFine(@Path("id") id: Int): FineResponse

    @POST("drivers/{id}/update-trip")
    suspend fun updateTrip(
        @Path("id") id: Int,
        @Body tripData: TripRequest,
    ): TripResponse
}

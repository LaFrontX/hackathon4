package com.example.blatplat.data.remote

import com.example.blatplat.data.session.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/** См. [ApiConfig] и `API_BASE_URL` в gradle.properties. */
object NetworkModule {
    val BASE_URL: String = ApiConfig.baseUrl

    fun createOkHttpClient(sessionManager: SessionManager): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .connectTimeout(30.seconds.toJavaDuration())
            .readTimeout(30.seconds.toJavaDuration())
            .addInterceptor(TokenInterceptor(sessionManager))
            .addInterceptor(logging)
            .build()
    }

    fun createApiService(client: OkHttpClient): ApiService =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .createService()

}

/** Обертка над Retrofit.create: платформенный `Class<T>` передаётся через реифицированный тип. */
private inline fun <reified T> Retrofit.createService(): T = create(T::class.java)

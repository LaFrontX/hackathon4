package com.example.blatplat.data.remote

import com.example.blatplat.data.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/** Подставляет Authorization только если токен уже есть (после логина). */
class TokenInterceptor(
    private val sessionManager: SessionManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.bearerHeader
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", token)
                .build()
        }
        return chain.proceed(request)
    }
}

package com.example.blatplat.data.auth

import com.example.blatplat.domain.model.AuthResult

/**
 * Демо-авторизация для хакатона: без обращения к БД/серверу.
 * Успех только при номере 8 800 555 35 35 (или +7 800 … — те же цифры), пароль: admin.
 */
object DemoAuth {

    private const val DEMO_PASSWORD = "admin"
    private const val DEMO_TOKEN = "demo-session-token"
    private const val DEMO_DRIVER_ID = "driver_demo_8800"
    private val DEMO_DIGITS_ACCEPTED = setOf("88005553535", "78005553535")

    fun tryLogin(phoneRaw: String, password: String): Result<AuthResult> {
        val digits = phoneRaw.filter { it.isDigit() }
        val okPhone = digits in DEMO_DIGITS_ACCEPTED
        val okPass = password == DEMO_PASSWORD
        return if (okPhone && okPass) {
            Result.success(
                AuthResult(token = DEMO_TOKEN, driverId = DEMO_DRIVER_ID),
            )
        } else {
            Result.failure(
                IllegalStateException("Неверный телефон или пароль"),
            )
        }
    }
}

package com.example.blatplat.data.session

/** Состояние сессии для условной навигации: Loading устраняет мигание экрана логина до чтения DataStore. */
sealed interface SessionState {
    data object Loading : SessionState
    data object Unauthenticated : SessionState
    data class Authenticated(val rawToken: String) : SessionState
}

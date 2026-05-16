package com.example.blatplat.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "mt_session")

/**
 * SessionManager инкапсулирует DataStore и держит в памяти Bearer для OkHttp:
 * Interceptor не должен читать DataStore на каждый запрос (I/O), поэтому синхронизируем `bearerHeader`
 * при save/logout и при каждом изменении preferences.
 */
class SessionManager(
    private val appContext: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val tokenKey = stringPreferencesKey("jwt_token")
    private val driverIdKey = stringPreferencesKey("driver_id")

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    @Volatile
    var bearerHeader: String? = null
        private set

    init {
        scope.launch {
            appContext.sessionDataStore.data.collect { prefs ->
                val token = prefs[tokenKey]
                bearerHeader = token?.let { "Bearer $it" }
                _sessionState.value =
                    if (token.isNullOrBlank()) SessionState.Unauthenticated else SessionState.Authenticated(token)
            }
        }
    }

    /** Текущее состояние после первой эмиссии DataStore; до этого — Loading. */
    fun isAuthorized(): Boolean = _sessionState.value is SessionState.Authenticated

    suspend fun saveSession(token: String, driverId: String) {
        bearerHeader = "Bearer $token"
        appContext.sessionDataStore.edit { prefs ->
            prefs[tokenKey] = token
            prefs[driverIdKey] = driverId
        }
    }

    suspend fun logout() {
        bearerHeader = null
        appContext.sessionDataStore.edit { prefs ->
            prefs.remove(tokenKey)
            prefs.remove(driverIdKey)
        }
    }
}

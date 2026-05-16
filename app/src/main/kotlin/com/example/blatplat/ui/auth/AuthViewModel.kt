package com.example.blatplat.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.blatplat.data.session.SessionManager
import com.example.blatplat.domain.repository.TransportRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val phone: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
)

class AuthViewModel(
    private val repository: TransportRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _ui.asStateFlow()

    /** Сигнал UI: встряхнуть поля (ошибка сети/учётки). */
    private val _shake = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val shakeSignal = _shake.asSharedFlow()

    private val _snack = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackMessages = _snack.asSharedFlow()

    fun onPhoneChange(v: String) {
        _ui.value = _ui.value.copy(phone = v)
    }

    fun onPasswordChange(v: String) {
        _ui.value = _ui.value.copy(password = v)
    }

    fun login() {
        val phone = _ui.value.phone.trim()
        val password = _ui.value.password
        if (phone.isBlank() || password.isBlank()) {
            viewModelScope.launch {
                _shake.emit(Unit)
                _snack.emit("Заполните телефон и пароль")
            }
            return
        }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true)
            repository.login(phone, password)
                .onSuccess { auth ->
                    sessionManager.saveSession(token = auth.token, driverId = auth.driverId)
                }
                .onFailure { e ->
                    _shake.emit(Unit)
                    _snack.emit(e.message ?: "Не удалось войти")
                }
            _ui.value = _ui.value.copy(isLoading = false)
        }
    }

    class Factory(
        private val repository: TransportRepository,
        private val sessionManager: SessionManager,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(repository, sessionManager) as T
    }
}

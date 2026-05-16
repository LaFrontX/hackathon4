package com.example.blatplat.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.blatplat.data.local.DemoProfileStore
import com.example.blatplat.domain.model.DriverProfile
import com.example.blatplat.ui.common.AsyncState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Оплата полностью имитируется: запросов к серверу нет,
 * баланс обновляет [DemoProfileStore] (виден на главном экране).
 */
class PaymentViewModel(
    private val demoProfileStore: DemoProfileStore,
) : ViewModel() {

    private val _profile = MutableStateFlow<AsyncState<DriverProfile>>(AsyncState.Loading)
    val profileState: StateFlow<AsyncState<DriverProfile>> = _profile.asStateFlow()

    private val _topUpBusy = MutableStateFlow(false)
    val topUpBusy: StateFlow<Boolean> = _topUpBusy.asStateFlow()

    private val _snacks = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackMessages = _snacks.asSharedFlow()

    fun loadProfile() {
        viewModelScope.launch {
            val p = demoProfileStore.profile.value.toDriverProfile()
            _profile.value = AsyncState.Success(p)
        }
    }

    fun topUp(amountRub: Int, paymentHint: String? = null) {
        viewModelScope.launch {
            _topUpBusy.value = true
            delay(650)
            demoProfileStore.addBalanceRub(amountRub)
            val p = demoProfileStore.profile.value.toDriverProfile()
            _profile.value = AsyncState.Success(p)
            val msg = if (paymentHint.isNullOrBlank()) {
                "Баланс пополнен (демо, без сервера)"
            } else {
                "Баланс пополнен · $paymentHint · демо"
            }
            _snacks.tryEmit(msg)
            _topUpBusy.value = false
        }
    }

    class Factory(
        private val demoProfileStore: DemoProfileStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PaymentViewModel(demoProfileStore) as T
    }
}

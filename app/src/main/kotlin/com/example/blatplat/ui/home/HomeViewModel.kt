package com.example.blatplat.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.blatplat.data.local.DemoProfileStore
import com.example.blatplat.domain.model.TariffOffer
import com.example.blatplat.domain.repository.TransportRepository
import com.example.blatplat.notifications.FinesAlertCoordinator
import com.example.blatplat.ui.common.AsyncState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: TransportRepository,
    private val demoProfileStore: DemoProfileStore,
    private val onFinesAlert: (finesCount: Int) -> Unit,
) : ViewModel() {

    private val _offerState = MutableStateFlow<AsyncState<TariffOffer>?>(null)
    val offerState: StateFlow<AsyncState<TariffOffer>?> = _offerState.asStateFlow()

    private val _offerLoading = MutableStateFlow(false)
    val offerLoading: StateFlow<Boolean> = _offerLoading.asStateFlow()

    private val _purchaseBusy = MutableStateFlow(false)
    val purchaseBusy: StateFlow<Boolean> = _purchaseBusy.asStateFlow()

    private val _finesCount = MutableStateFlow<Int?>(null)
    val finesCount: StateFlow<Int?> = _finesCount.asStateFlow()

    private val _snacks = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackMessages = _snacks.asSharedFlow()

    private val _showOfferDialog = MutableStateFlow(false)
    val showOfferDialog: StateFlow<Boolean> = _showOfferDialog.asStateFlow()

    init {
        refreshDriverStatus()
    }

    fun refreshDriverStatus() {
        viewModelScope.launch {
            repository.syncDriverFromApi()
                .onSuccess { sync ->
                    demoProfileStore.applyFromServer(
                        balanceRub = sync.balanceRub,
                        firstName = sync.firstName,
                        lastName = sync.lastName,
                        vehicleRegNumber = sync.vehicleRegNumber,
                        tripsCompleted = sync.tripsCompleted,
                    )
                }
            repository.getDriverStatus()
                .onSuccess { status ->
                    _finesCount.value = status.finesCount
                    if (FinesAlertCoordinator.shouldNotify(status.finesCount)) {
                        FinesAlertCoordinator.markNotified()
                        onFinesAlert(status.finesCount)
                    }
                }
                .onFailure {
                    // Сервер ещё не поднят — не блокируем главный экран
                    _finesCount.value = null
                }
        }
    }

    fun fetchOffer() {
        viewModelScope.launch {
            _offerLoading.value = true
            _offerState.value = AsyncState.Loading
            repository.getCurrentOffer()
                .onSuccess { offer ->
                    demoProfileStore.applyTariffFromOffer(
                        tariffName = offer.recommendedTier.name,
                        discountPercent = offer.recommendedTier.discountPercent,
                        validUntil = offer.discountValidUntil,
                    )
                    _offerState.value = AsyncState.Success(offer)
                    _showOfferDialog.value = true
                }
                .onFailure { e ->
                    _offerState.value = AsyncState.Error(
                        message = e.message ?: "Не удалось получить предложение",
                        retry = { fetchOffer() },
                    )
                }
            _offerLoading.value = false
        }
    }

    fun purchaseCurrentOffer() {
        val offer = (_offerState.value as? AsyncState.Success)?.value ?: return
        viewModelScope.launch {
            _purchaseBusy.value = true
            repository.purchaseOffer(offer.offerId)
                .onSuccess { serverBalance ->
                    if (serverBalance != null) {
                        demoProfileStore.setBalanceRub(serverBalance)
                    } else {
                        if (!demoProfileStore.tryDeductBalanceRub(offer.priceRub)) {
                            _snacks.tryEmit("Недостаточно средств на балансе")
                            _purchaseBusy.value = false
                            return@launch
                        }
                    }
                    _snacks.tryEmit("Тариф «${offer.tariffName}» оформлен")
                    _offerState.value = null
                    _showOfferDialog.value = false
                }
                .onFailure { e ->
                    _snacks.tryEmit(e.message ?: "Ошибка покупки")
                }
            _purchaseBusy.value = false
        }
    }

    fun dismissOffer() {
        _offerState.value = null
        _showOfferDialog.value = false
    }

    fun dismissOfferDialogOnly() {
        _showOfferDialog.value = false
    }

    class Factory(
        private val repository: TransportRepository,
        private val demoProfileStore: DemoProfileStore,
        private val onFinesAlert: (Int) -> Unit,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(repository, demoProfileStore, onFinesAlert) as T
    }
}

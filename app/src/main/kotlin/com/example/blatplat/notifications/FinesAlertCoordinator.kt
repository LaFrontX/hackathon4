package com.example.blatplat.notifications

/**
 * Порог штрафов для push «Будьте бдительны на дороге».
 * Позже порог и текст можно вынести в конфиг с сервера.
 */
object FinesAlertCoordinator {

    const val FINES_ALERT_THRESHOLD: Int = 30

    private var alertedThisSession = false

    fun shouldNotify(finesCount: Int): Boolean =
        finesCount > FINES_ALERT_THRESHOLD && !alertedThisSession

    fun markNotified() {
        alertedThisSession = true
    }

    fun resetSession() {
        alertedThisSession = false
    }
}

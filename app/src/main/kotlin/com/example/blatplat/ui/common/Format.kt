package com.example.blatplat.ui.common

/** Форматирование суммы в рублях с пробелами в разрядах. */
fun formatRubles(amount: Int): String {
    val digits = amount.toString()
    val grouped = digits
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()
    return "$grouped ₽"
}

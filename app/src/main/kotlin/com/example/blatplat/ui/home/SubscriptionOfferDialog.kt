package com.example.blatplat.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.blatplat.domain.model.TariffOffer
import com.example.blatplat.ui.common.formatRubles

private val OfferRed = Color(0xFFC62828)
private val OfferRedDark = Color(0xFF8E0000)
private val OfferText = Color(0xFFFFF8F8)

@Composable
fun SubscriptionOfferDialog(
    offer: TariffOffer,
    purchaseBusy: Boolean,
    onPurchase: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        containerColor = OfferRed,
        titleContentColor = OfferText,
        textContentColor = OfferText,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Специальное предложение",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OfferText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val rec = offer.recommendedTier
                Text(
                    text = "Вам предложен месячный абонемент на ${rec.discountPercent}% скидки " +
                        "(название тарифа: ${rec.name})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OfferText,
                    lineHeight = 26.sp,
                )
                Spacer(modifier = Modifier.height(16.dp))
                offer.tiers.forEach { tier ->
                    if (tier.discountPercent != rec.discountPercent) {
                        Text(
                            text = "${tier.discountPercent}% — ${tier.name}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = OfferText.copy(alpha = 0.95f),
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Стоимость: ${formatRubles(offer.priceRub)}",
                    fontSize = 16.sp,
                    color = OfferText.copy(alpha = 0.9f),
                )
                Text(
                    text = "Скидка действует до: ${offer.discountValidUntil}",
                    fontSize = 15.sp,
                    color = OfferText.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onPurchase,
                enabled = !purchaseBusy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OfferText,
                    contentColor = OfferRedDark,
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (purchaseBusy) "Оформление…" else "Купить тариф",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = OfferText, fontSize = 16.sp)
            }
        },
    )
}

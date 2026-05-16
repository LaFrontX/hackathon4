package com.example.blatplat.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.blatplat.R

object NotificationHelper {

    private const val CHANNEL_PAYMENT = "payment_notifications"
    private const val CHANNEL_ROAD_SAFETY = "road_safety_alerts"
    private const val NOTIFICATION_ID_PAYMENT = 1001
    private const val NOTIFICATION_ID_ROAD_SAFETY = 1002

    fun showPaymentNotification(
        context: Context,
        amount: String,
        title: String,
    ) {
        createNotificationChannel(context)

        val deepLinkUri = Uri.parse("transportapp://payment/$amount")
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            amount.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PAYMENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("Пополните баланс на $amount ₽")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_PAYMENT, notification)
    }

    fun showRoadSafetyAlert(context: Context, finesCount: Int) {
        createNotificationChannels(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ROAD_SAFETY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Будьте бдительны на дороге")
            .setContentText("Зафиксировано штрафов: $finesCount. Соблюдайте ПДД на платных участках.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Зафиксировано штрафов: $finesCount (порог > ${FinesAlertCoordinator.FINES_ALERT_THRESHOLD}). " +
                        "Соблюдайте скоростной режим и разметку на платных дорогах.",
                ),
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ROAD_SAFETY, notification)
    }

    private fun createNotificationChannel(context: Context) {
        createNotificationChannels(context)
    }

    private fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val payment = NotificationChannel(
            CHANNEL_PAYMENT,
            "Пополнение баланса",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Уведомления о необходимости пополнить баланс"
        }

        val roadSafety = NotificationChannel(
            CHANNEL_ROAD_SAFETY,
            "Безопасность на дороге",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Предупреждения при большом числе штрафов"
        }

        manager.createNotificationChannel(payment)
        manager.createNotificationChannel(roadSafety)
    }
}

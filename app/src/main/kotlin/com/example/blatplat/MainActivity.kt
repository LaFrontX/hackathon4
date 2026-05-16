package com.example.blatplat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.blatplat.navigation.AppNavigation
import com.example.blatplat.notifications.NotificationHelper
import com.example.blatplat.ui.theme.MoscowTransportTheme

class MainActivity : ComponentActivity() {

    private var pendingDeepLinkIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingDeepLinkIntent = intent
        enableEdgeToEdge()
        val app = application as TransportApplication
        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val deepLinkIntent = pendingDeepLinkIntent

            LaunchedEffect(deepLinkIntent) {
                deepLinkIntent?.let { intent ->
                    navController.handleDeepLink(intent)
                    pendingDeepLinkIntent = null
                }
            }

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
            ) { _ ->
                NotificationHelper.showPaymentNotification(
                    context = context,
                    amount = TEST_NOTIFICATION_AMOUNT,
                    title = TEST_NOTIFICATION_TITLE,
                )
            }

            fun showNotification(block: () -> Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) == PackageManager.PERMISSION_GRANTED -> block()
                        else -> notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS,
                        )
                    }
                } else {
                    block()
                }
            }

            val showTestNotification = remember {
                {
                    showNotification {
                        NotificationHelper.showPaymentNotification(
                            context = context,
                            amount = TEST_NOTIFICATION_AMOUNT,
                            title = TEST_NOTIFICATION_TITLE,
                        )
                    }
                }
            }

            val showFinesAlert = remember {
                { finesCount: Int ->
                    showNotification {
                        NotificationHelper.showRoadSafetyAlert(context, finesCount)
                    }
                }
            }

            MoscowTransportTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        sessionManager = app.sessionManager,
                        repository = app.repository,
                        demoProfileStore = app.demoProfileStore,
                        onTestNotificationClick = showTestNotification,
                        onFinesAlert = showFinesAlert,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLinkIntent = intent
    }

    companion object {
        private const val TEST_NOTIFICATION_AMOUNT = "1000"
        private const val TEST_NOTIFICATION_TITLE = "Пополните баланс"
    }
}

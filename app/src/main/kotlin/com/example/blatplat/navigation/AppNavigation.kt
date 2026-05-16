package com.example.blatplat.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.blatplat.data.local.DemoProfileStore
import com.example.blatplat.data.session.SessionManager
import com.example.blatplat.data.session.SessionState
import com.example.blatplat.domain.repository.TransportRepository
import com.example.blatplat.ui.PaymentScreen
import com.example.blatplat.ui.pager.MainPagerScreen
import com.example.blatplat.ui.auth.AuthViewModel
import com.example.blatplat.ui.auth.LoginScreen
import com.example.blatplat.notifications.FinesAlertCoordinator
import com.example.blatplat.ui.home.HomeViewModel
import com.example.blatplat.ui.payment.PaymentViewModel
import com.example.blatplat.ui.theme.MtAccent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    const val PAYMENT = "payment/{amount}"

    fun payment(amount: String): String = "payment/$amount"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    sessionManager: SessionManager,
    repository: TransportRepository,
    demoProfileStore: DemoProfileStore,
    onTestNotificationClick: () -> Unit,
    onFinesAlert: (finesCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val session by sessionManager.sessionState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val authFactory = remember(repository, sessionManager) {
        AuthViewModel.Factory(repository, sessionManager)
    }
    val paymentFactory = remember(demoProfileStore) { PaymentViewModel.Factory(demoProfileStore) }
    val homeFactory = remember(repository, demoProfileStore, onFinesAlert) {
        HomeViewModel.Factory(repository, demoProfileStore, onFinesAlert)
    }

    LaunchedEffect(session) {
        if (session is SessionState.Authenticated) {
            if (navController.currentDestination?.route == Routes.LOGIN) {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        if (session is SessionState.Unauthenticated) {
            val route = navController.currentDestination?.route
            if (route != null && route != Routes.LOGIN && route != Routes.SPLASH) {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
    ) {
        composable(Routes.SPLASH) {
            LaunchedEffect(session) {
                when (session) {
                    SessionState.Loading -> Unit
                    is SessionState.Authenticated ->
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                            launchSingleTop = true
                        }

                    SessionState.Unauthenticated ->
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                            launchSingleTop = true
                        }
                }
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MtAccent)
            }
        }

        composable(
            Routes.LOGIN,
            enterTransition = {
                slideInHorizontally(animationSpec = tween(280)) { it / 8 } + fadeIn(tween(240))
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(220)) { -it / 10 } + fadeOut(tween(200))
            },
        ) {
            LoginScreen(factory = authFactory)
        }

        composable(
            Routes.HOME,
            enterTransition = {
                slideInHorizontally(animationSpec = tween(280)) { it / 6 } + fadeIn(tween(260))
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(220)) { -it / 8 } + fadeOut(tween(200))
            },
        ) {
            MainPagerScreen(
                demoProfileStore = demoProfileStore,
                homeFactory = homeFactory,
                paymentFactory = paymentFactory,
                onLogoutClick = {
                    FinesAlertCoordinator.resetSession()
                    scope.launch { sessionManager.logout() }
                },
                onTestNotificationClick = onTestNotificationClick,
            )
        }

        composable(
            route = Routes.PAYMENT,
            arguments = listOf(
                navArgument("amount") {
                    type = NavType.StringType
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "transportapp://payment/{amount}"
                },
            ),
            enterTransition = {
                slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(tween(260))
            },
            popExitTransition = {
                slideOutHorizontally(animationSpec = tween(260)) { it } + fadeOut(tween(220))
            },
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0"
            PaymentScreen(
                initialAmount = amount,
                factory = paymentFactory,
                onNavigateBack = { navController.navigateUp() },
            )
        }
    }
}

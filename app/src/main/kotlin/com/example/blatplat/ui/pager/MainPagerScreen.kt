package com.example.blatplat.ui.pager

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blatplat.data.local.DemoProfileStore
import com.example.blatplat.ui.HomeMainPage
import com.example.blatplat.ui.PaymentScreen
import com.example.blatplat.ui.cabinet.PersonalCabinetScreen
import com.example.blatplat.ui.history.TripHistoryScreen
import com.example.blatplat.ui.home.HomeViewModel
import com.example.blatplat.ui.payment.PaymentViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPagerScreen(
    demoProfileStore: DemoProfileStore,
    homeFactory: HomeViewModel.Factory,
    paymentFactory: PaymentViewModel.Factory,
    initialPage: Int = 0,
    onLogoutClick: () -> Unit,
    onTestNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themes = PagerThemes.all
    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, themes.lastIndex),
        pageCount = { themes.size },
    )
    val interpolated = rememberInterpolatedPagerColors(pagerState, themes)
    val snackbarHostState = remember { SnackbarHostState() }
    val homeViewModel: HomeViewModel = viewModel(factory = homeFactory)

    LaunchedEffect(homeViewModel) {
        homeViewModel.snackMessages.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    LaunchedEffect(initialPage) {
        if (initialPage in themes.indices && initialPage != pagerState.currentPage) {
            pagerState.animateScrollToPage(initialPage, animationSpec = tween(450))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = interpolated.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(interpolated.background),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Свайп влево / вправо",
                    color = interpolated.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                PagerIndicator(
                    pagerState = pagerState,
                    themes = themes,
                    activeColors = interpolated,
                    modifier = Modifier.fillMaxWidth(),
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    beyondViewportPageCount = 1,
                ) { page ->
                    val pageColors = themes[page]
                    when (page) {
                        0 -> HomeMainPage(
                            demoProfileStore = demoProfileStore,
                            homeFactory = homeFactory,
                            colors = pageColors,
                            onLogoutClick = onLogoutClick,
                            onTestNotificationClick = onTestNotificationClick,
                            homeViewModel = homeViewModel,
                        )

                        1 -> PaymentScreen(
                            initialAmount = "0",
                            factory = paymentFactory,
                            colors = pageColors,
                            showTopBarBack = false,
                            onNavigateBack = {},
                        )

                        2 -> TripHistoryScreen(
                            colors = pageColors,
                            showBackButton = false,
                            onNavigateBack = {},
                        )

                        3 -> PersonalCabinetScreen(
                            demoProfileStore = demoProfileStore,
                            colors = pageColors,
                            showBackButton = false,
                            onNavigateBack = {},
                        )
                    }
                }
            }

            Text(
                text = themes[pagerState.currentPage].title,
                color = interpolated.accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
            )
        }
    }
}

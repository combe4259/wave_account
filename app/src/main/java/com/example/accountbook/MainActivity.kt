package com.example.accountbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.accountbook.ui.theme.AccountBookTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import com.example.accountbook.view.ExpenseViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accountbook.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 전체화면 활용으로 몰입감 있는 경험 제공
        setContent {
            AccountBookTheme {
                AccountBookApp()
            }
        }
    }
}

sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    object Calendar : Screen("calendar", Icons.Default.DateRange, "가계부")
    object Gallery : Screen("gallery", Icons.Default.Star, "갤러리")
    object Statistics : Screen("statistics", Icons.Default.Settings, "통계")

    data class DailyDetail(val date: Long) : Screen("daily_detail/${date}", Icons.Default.Today, "일별 상세")
    data class AddExpense(val initialDate: Long? = null) : Screen(
        route = if (initialDate != null) "add_expense/${initialDate}" else "add_expense",
        icon = Icons.Default.Add,
        title = "지출 추가"
    )
}

// 하단 네비게이션에 표시될 메인 화면들
val bottomNavItems = listOf(
    Screen.Calendar,
    Screen.Gallery,
    Screen.Statistics
)

@Composable
fun AccountBookApp() {
    // 화면 상태를 중앙에서 관리하여 예측 가능한 네비게이션 제공
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Calendar) }

    // ViewModel을 앱 레벨에서 생성하여 모든 화면에서 동일한 인스턴스 사용
    val viewModel: ExpenseViewModel = viewModel()

    Scaffold(
        topBar = {
            AppTopBar(
                currentScreen = currentScreen,
                shouldShowTopBar = !isDetailScreen(currentScreen)
            )
        },
        bottomBar = {
            AppBottomBar(
                currentScreen = currentScreen,
                shouldShowBottomBar = !isDetailScreen(currentScreen),
                onScreenSelected = { screen -> currentScreen = screen }
            )
        }
    ) { paddingValues ->
        // 메인 콘텐츠 영역
        AppContent(
            currentScreen = currentScreen,
            viewModel = viewModel,
            onNavigateToScreen = { screen -> currentScreen = screen },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

// TopBar 로직을 별도 컴포넌트로 분리하여 재사용성과 테스트 용이성 향상
@Composable
private fun AppTopBar(
    currentScreen: Screen,
    shouldShowTopBar: Boolean
) {
    if (shouldShowTopBar) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = currentScreen.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// BottomBar 로직을 분리하여 네비게이션 관련 코드를 명확하게 구분
@Composable
private fun AppBottomBar(
    currentScreen: Screen,
    shouldShowBottomBar: Boolean,
    onScreenSelected: (Screen) -> Unit
) {
    if (shouldShowBottomBar) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 3.dp
        ) {
            bottomNavItems.forEach { screen ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            screen.icon,
                            contentDescription = screen.title,
                            tint = if (isCurrentMainScreen(currentScreen, screen)) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    label = {
                        Text(
                            text = screen.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isCurrentMainScreen(currentScreen, screen)) {
                                FontWeight.Medium
                            } else {
                                FontWeight.Normal
                            }
                        )
                    },
                    selected = isCurrentMainScreen(currentScreen, screen),
                    onClick = { onScreenSelected(screen) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

// 메인 콘텐츠 네비게이션 로직을 별도로 분리하여 코드 가독성 향상
@Composable
private fun AppContent(
    currentScreen: Screen,
    viewModel: ExpenseViewModel,
    onNavigateToScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (currentScreen) {
            is Screen.Calendar -> {
                CalendarMainScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    onDateSelected = { selectedDate ->
                        // 달력에서 날짜 선택 시 해당 날짜의 상세 화면으로 이동
                        onNavigateToScreen(Screen.DailyDetail(selectedDate))
                    }
                )
            }

            is Screen.Gallery -> {
                ExpenseGalleryScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is Screen.Statistics -> {
                ExpenseStatisticsScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }

            is Screen.DailyDetail -> {
                DailyExpenseScreen(
                    selectedDate = currentScreen.date,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    onNavigateBack = {
                        // 논리적으로 명확한 네비게이션: 상세 화면에서 달력으로 복귀
                        onNavigateToScreen(Screen.Calendar)
                    },
                    onNavigateToAdd = { date ->
                        // 특정 날짜로 지출 추가 화면 이동
                        onNavigateToScreen(Screen.AddExpense(initialDate = date))
                    }
                )
            }

            is Screen.AddExpense -> {
                AddExpenseScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    initialDate = currentScreen.initialDate,
                    onNavigateBack = {
                        // 지출 추가 완료 후 적절한 화면으로 복귀
                        // 특정 날짜에서 온 경우 해당 날짜로, 아니면 달력으로
                        if (currentScreen.initialDate != null) {
                            onNavigateToScreen(Screen.DailyDetail(currentScreen.initialDate))
                        } else {
                            onNavigateToScreen(Screen.Calendar)
                        }
                    }
                )
            }
        }
    }
}

// 유틸리티 함수들을 통해 복잡한 조건 로직을 명확하게 추상화
private fun isDetailScreen(screen: Screen): Boolean {
    return screen is Screen.DailyDetail || screen is Screen.AddExpense
}

private fun isCurrentMainScreen(currentScreen: Screen, targetScreen: Screen): Boolean {
    return when {
        currentScreen is Screen.Calendar && targetScreen is Screen.Calendar -> true
        currentScreen is Screen.Gallery && targetScreen is Screen.Gallery -> true
        currentScreen is Screen.Statistics && targetScreen is Screen.Statistics -> true
        // 상세 화면에서도 해당하는 메인 화면을 활성화 상태로 표시
        currentScreen is Screen.DailyDetail && targetScreen is Screen.Calendar -> true
        currentScreen is Screen.AddExpense && targetScreen is Screen.Calendar -> true
        else -> false
    }
}
package com.example.accountbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.accountbook.presentation.main.MainViewModel
import com.example.accountbook.presentation.main.MainTab
import com.example.accountbook.ui.screens.*
import com.example.accountbook.view.ExpenseViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { //이 시점에서 앱의 기본 설정과 UI 구성이 이루어짐
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 전체화면 활용
        setContent {
            AccountBookTheme {
                AccountBookApp()
            }
        }
    }
}


//앱의 모든 화면 관리
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
    // Hilt로 ViewModel 주입
    val mainViewModel: MainViewModel = hiltViewModel()
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val transactions by mainViewModel.monthlyTransactions.collectAsStateWithLifecycle()
    
    // 기존 ExpenseViewModel도 일단 유지 (점진적 마이그레이션)
    val expenseViewModel: ExpenseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.onPrimary,
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
            viewModel = expenseViewModel,  // 일단 기존 ViewModel 사용
            monthlyGoal = mainViewModel.monthlyBudget,
            onGoalChange = { mainViewModel.updateMonthlyBudget(it) },
            onNavigateToScreen = { screen -> currentScreen = screen },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

// BottomBar
@Composable
private fun AppBottomBar(
    currentScreen: Screen,
    shouldShowBottomBar: Boolean,
    onScreenSelected: (Screen) -> Unit
) {
    if (shouldShowBottomBar) {
        NavigationBar(
            containerColor = Color.White,
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
                        indicatorColor = Color.White
                    )
                )
            }
        }
    }
}

// 메인 콘텐츠 네비게이션 로직
@Composable
private fun AppContent(
    currentScreen: Screen,
    viewModel: ExpenseViewModel,
    monthlyGoal: Int,
    onGoalChange: (Int) -> Unit,
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
                    monthlyGoal = monthlyGoal,
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
                    monthlyGoal = monthlyGoal,
                    onGoalChange = onGoalChange,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is Screen.DailyDetail -> {
                DailyExpenseScreen(
                    selectedDate = currentScreen.date,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    onNavigateBack = {
                        // 상세 화면에서 달력으로 복귀
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

//조건 로직
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
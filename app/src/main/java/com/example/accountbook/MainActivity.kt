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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QueryStats
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
import com.example.accountbook.presentation.main.MainUiState
import com.example.accountbook.domain.model.Transaction
import com.example.accountbook.ui.screens.CalendarMainScreen
import com.example.accountbook.presentation.adapter.ViewModelAdapter
import com.example.accountbook.ui.screens.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument

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
    object Gallery : Screen("gallery", Icons.Default.Image, "갤러리")
    object Statistics : Screen("statistics", Icons.Default.QueryStats, "통계")
}

// 하단 네비게이션에 표시될 메인 화면들
val bottomNavItems = listOf(
    Screen.Calendar,
    Screen.Gallery,
    Screen.Statistics
)

@Composable
fun AccountBookApp() {
    // Navigation Controller 생성
    val navController = rememberNavController()
    // Hilt로 ViewModel 주입
    val mainViewModel: MainViewModel = hiltViewModel()
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val transactions by mainViewModel.monthlyTransactions.collectAsStateWithLifecycle()
    
    // 현재 route를 추적
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        bottomBar = {
            AppBottomBar(
                navController = navController,
                currentRoute = currentRoute,
                shouldShowBottomBar = !isDetailRoute(currentRoute)
            )
        }
    ) { paddingValues ->
        // NavHost로 네비게이션 관리
        AppNavHost(
            navController = navController,
            mainViewModel = mainViewModel,
            uiState = uiState,
            transactions = transactions,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

// BottomBar
@Composable
private fun AppBottomBar(
    navController: NavHostController,
    currentRoute: String?,
    shouldShowBottomBar: Boolean
) {
    if (shouldShowBottomBar) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 3.dp
        ) {
            bottomNavItems.forEach { screen ->
                val selected = currentRoute == screen.route || 
                    (currentRoute?.startsWith("daily_detail") == true && screen.route == "calendar") ||
                    (currentRoute?.startsWith("add_expense") == true && screen.route == "calendar")
                    
                NavigationBarItem(
                    icon = {
                        Icon(
                            screen.icon,
                            contentDescription = screen.title,
                            tint = if (selected) {
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
                            fontWeight = if (selected) {
                                FontWeight.Medium
                            } else {
                                FontWeight.Normal
                            }
                        )
                    },
                    selected = selected,
                    onClick = { 
                        navController.navigate(screen.route) {
                            // 백스택 관리: 메인 화면들 간 이동시
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
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

// NavHost로 네비게이션 관리
@Composable
private fun AppNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    uiState: MainUiState,
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Calendar.route,
        modifier = modifier
    ) {
        // 달력 화면
        composable(Screen.Calendar.route) {
            val adapter = remember { ViewModelAdapter(mainViewModel) }
            CalendarMainScreen(
                viewModel = adapter,
                monthlyGoal = uiState.monthlyBudget,
                onDateSelected = { selectedDate ->
                    navController.navigate("daily_detail/$selectedDate")
                },
                onNavigateToAdd = { date ->
                    navController.navigate("add_expense/$date")
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // 갤러리 화면
        composable(Screen.Gallery.route) {
            val adapter = remember { ViewModelAdapter(mainViewModel) }
            ExpenseGalleryScreen(
                viewModel = adapter,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // 통계 화면
        composable(Screen.Statistics.route) {
            val adapter = remember { ViewModelAdapter(mainViewModel) }
            ExpenseStatisticsScreen(
                viewModel = adapter,
                monthlyGoal = uiState.monthlyBudget,
                onGoalChange = { newGoal ->
                    mainViewModel.updateMonthlyBudget(newGoal)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // 일별 상세 화면
        composable(
            route = "daily_detail/{date}",
            arguments = listOf(navArgument("date") { type = NavType.LongType })
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getLong("date") ?: System.currentTimeMillis()
            val adapter = remember { ViewModelAdapter(mainViewModel) }
            DailyExpenseScreen(
                selectedDate = date,
                viewModel = adapter,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAdd = { selectedDate ->
                    navController.navigate("add_expense/$selectedDate")
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // 지출 추가 화면
        composable(
            route = "add_expense/{initialDate}",
            arguments = listOf(navArgument("initialDate") { 
                type = NavType.LongType
                nullable = false
            })
        ) { backStackEntry ->
            val initialDate = backStackEntry.arguments?.getLong("initialDate")
            val adapter = remember { ViewModelAdapter(mainViewModel) }
            AddExpenseScreen(
                viewModel = adapter,
                initialDate = initialDate,
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // 날짜 없이 지출 추가
        composable("add_expense") {
            val adapter = remember { ViewModelAdapter(mainViewModel) }
            AddExpenseScreen(
                viewModel = adapter,
                initialDate = null,
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

//조건 로직
private fun isDetailRoute(route: String?): Boolean {
    return route?.startsWith("daily_detail") == true || 
           route?.startsWith("add_expense") == true
}
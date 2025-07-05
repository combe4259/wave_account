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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import com.example.accountbook.view.ExpenseViewModel
import com.example.accountbook.ui.screens.AddExpenseScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accountbook.ui.screens.CalendarMainScreen
import com.example.accountbook.ui.screens.DailyExpenseScreen
import com.example.accountbook.ui.screens.ExpenseGalleryScreen
import com.example.accountbook.ui.screens.ExpenseStatisticsScreen
import com.example.accountbook.view.ExpenseGalleryViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()//전체화면 활용
        setContent {
            AccountBookTheme {
                AccountBookApp()
            }
        }
    }
}

sealed class Screen(val route: String, val icon: ImageVector, val title: String) {

    object Calendar : Screen("calendar", Icons.Default.DateRange, "가계부")
    object Gallery    : Screen("gallery",    Icons.Default.Star,     "갤러리")
    object Statistics : Screen("statistics", Icons.Default.Settings, "통계")

    data class DailyDetail(val date: Long) : Screen("daily_detail", Icons.Default.Today, "일별 상세")
    data class AddExpense(val date: Long? = null) : Screen("add_expense", Icons.Default.Add, "지출 추가")
}




val bottomNavItems = listOf(
    Screen.Calendar,
    Screen.Gallery,
    Screen.Statistics
)

@Composable
fun AccountBookApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Calendar) }
    val viewModel: ExpenseViewModel = viewModel()

    Scaffold(
        topBar = {
            // 일별 상세 화면과 지출 추가 화면에서는 자체 TopBar 사용
            if (currentScreen !is Screen.DailyDetail && currentScreen !is Screen.AddExpense) {
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
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        bottomBar = {
            // 세부 화면에서는 하단 네비게이션 숨김
            if (currentScreen !is Screen.DailyDetail && currentScreen !is Screen.AddExpense) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentScreen::class == screen::class,
                            onClick = { currentScreen = screen }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 스마트 캐스트 문제를 해결하기 위해 현재 화면을 지역 변수에 저장
            val screen = currentScreen

            when (screen) {
                // 메인 가계부 화면 - 달력
                is Screen.Calendar -> CalendarMainScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    onDateSelected = { selectedDate ->
                        // 달력에서 날짜를 클릭하면 해당 날짜의 상세 화면으로 이동
                        currentScreen = Screen.DailyDetail(selectedDate)
                    }
                )

                // 갤러리 화면
                is Screen.Gallery -> ExpenseGalleryScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )

                // 통계 화면
                is Screen.Statistics -> ExpenseStatisticsScreen(
                    modifier = Modifier.fillMaxSize()
                )

                // 일별 상세 화면
                is Screen.DailyDetail -> DailyExpenseScreen(
                    selectedDate = screen.date, // 이제 스마트 캐스트가 작동함
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    onNavigateBack = {
                        // 뒤로 버튼을 누르면 달력으로 돌아감
                        currentScreen = Screen.Calendar
                    },
                    onNavigateToAdd = { date ->
                        // 지출 추가 버튼을 누르면 선택된 날짜로 지출 추가 화면 이동
                        currentScreen = Screen.AddExpense(date)
                    }
                )

                // 지출 추가 화면
                is Screen.AddExpense -> AddExpenseScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    onNavigateBack = {
                        // 지출 추가 완료 후 달력으로 돌아감
                        currentScreen = Screen.Calendar
                    }
                )
            }
        }
    }
}
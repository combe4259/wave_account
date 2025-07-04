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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import com.example.accountbook.ui.screens.ExpenseListScreen
import com.example.accountbook.ui.screens.AddExpenseScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accountbook.ui.screens.ExpenseGalleryScreen
import com.example.accountbook.ui.screens.ExpenseStatisticsScreen


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
    object List       : Screen("list",       Icons.Default.Home,     "가계부")
    object Add        : Screen("add",        Icons.Default.Add,      "추가")
    object Gallery    : Screen("gallery",    Icons.Default.Star,     "갤러리")
    object Statistics : Screen("statistics", Icons.Default.Settings, "통계")
}

val bottomNavItems = listOf(
    Screen.List,
    Screen.Gallery,
    Screen.Statistics
)

@Composable
fun AccountBookApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }
    val viewModel: ExpenseViewModel = viewModel()

    Scaffold(
        topBar = {
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
        },
        floatingActionButton = {
            if (currentScreen == Screen.List) {
                FloatingActionButton(
                    onClick = { currentScreen = Screen.Add }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "지출 추가")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon =    { Icon(screen.icon, contentDescription = screen.title) },
                        label =   { Text(screen.title) },
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                Screen.List -> ExpenseListScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToAdd = { currentScreen = Screen.Add }
                )
                Screen.Gallery -> ExpenseGalleryScreen(modifier = Modifier.fillMaxSize())
                Screen.Statistics -> ExpenseStatisticsScreen(modifier = Modifier.fillMaxSize())
                Screen.Add -> AddExpenseScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    onNavigateBack = { currentScreen = Screen.List }
                )
            }
        }
    }
}
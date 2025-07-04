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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accountbook.view.ExpenseViewModel
import com.example.accountbook.ui.screens.ExpenseListScreen
import com.example.accountbook.ui.screens.AddExpenseScreen
import androidx.lifecycle.viewmodel.compose.viewModel


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

@Composable
fun AccountBookApp() {
    var currentScreen by remember { mutableStateOf("list") } //네비게이션 기억
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
                        text = when (currentScreen) {
                            "list" -> "가계부"
                            "add" -> "지출 추가"
                            else -> "가계부"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentScreen == "list") {
                FloatingActionButton(
                    onClick = { currentScreen = "add" }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "지출 추가")
                }
            }
        }
    ) { paddingValues ->
        when (currentScreen) {
            "list" -> ExpenseListScreen(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onNavigateToAdd = { currentScreen = "add" }
            )
            "add" -> AddExpenseScreen(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onNavigateBack = { currentScreen = "list" }
            )
        }
    }
}
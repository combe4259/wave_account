package com.example.accountbook.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.accountbook.presentation.gallery.GalleryViewModel
import com.example.accountbook.ui.screens.CalendarMainScreen
import com.example.accountbook.ui.screens.ExpenseGalleryScreen
import com.example.accountbook.ui.screens.ExpenseStatisticsScreen
import com.example.accountbook.view.ExpenseViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
    // 기존 화면들이 아직 ExpenseViewModel을 사용하므로 임시로 유지
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val transactions by mainViewModel.monthlyTransactions.collectAsStateWithLifecycle()
    val statistics by mainViewModel.monthlyStatistics.collectAsStateWithLifecycle()
    
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState.selectedTab) {
            MainTab.CALENDAR -> {
                // 기존 CalendarMainScreen 사용 (점진적 마이그레이션)
                CalendarMainScreen(
                    viewModel = expenseViewModel,
                    monthlyGoal = uiState.monthlyBudget,
                    modifier = Modifier.fillMaxSize(),
                    onDateSelected = { date ->
                        mainViewModel.selectDate(date)
                    }
                )
            }
            
            MainTab.GALLERY -> {
                // 기존 ExpenseGalleryScreen 사용
                ExpenseGalleryScreen(
                    viewModel = expenseViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            MainTab.STATISTICS -> {
                // 기존 ExpenseStatisticsScreen 사용
                ExpenseStatisticsScreen(
                    monthlyGoal = uiState.monthlyBudget,
                    onGoalChange = { mainViewModel.updateMonthlyBudget(it) },
                    viewModel = expenseViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
package com.example.accountbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.accountbook.model.Expense
import com.example.accountbook.view.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyExpenseScreen(
    selectedDate: Long,
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToAdd: (Long) -> Unit
) {
    val allExpenses by viewModel.allExpenses.observeAsState(emptyList())

    // 선택된 날짜의 지출만 필터링
    val dailyExpenses = remember(allExpenses, selectedDate) {
        filterExpensesByDate(allExpenses, selectedDate)
    }

    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA)
    val totalDailyExpense = dailyExpenses.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(dateFormat.format(Date(selectedDate)))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAdd(selectedDate) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "지출 추가")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 일일 지출 요약 카드
            DailySummaryCard(
                totalExpense = totalDailyExpense,
                expenseCount = dailyExpenses.size
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 지출 목록
            if (dailyExpenses.isEmpty()) {
                EmptyDayState(
                    onAddExpense = { onNavigateToAdd(selectedDate) }
                )
            } else {
                DailyExpenseList(
                    expenses = dailyExpenses,
                    onDeleteExpense = { expense ->
                        viewModel.deleteExpense(expense)
                    }
                )
            }
        }
    }
}

@Composable
fun DailySummaryCard(
    totalExpense: Double,
    expenseCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "오늘의 지출",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${expenseCount}건",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = NumberFormat.getNumberInstance(Locale.KOREA).format(totalExpense) + "원",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptyDayState(
    onAddExpense: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "이 날짜에는 지출 내역이 없습니다",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onAddExpense,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("지출 추가하기")
            }
        }
    }
}

@Composable
fun DailyExpenseList(
    expenses: List<Expense>,
    onDeleteExpense: (Expense) -> Unit
) {
    // 카테고리별로 그룹화
    val groupedExpenses = expenses.groupBy { it.category }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedExpenses.forEach { (category, categoryExpenses) ->
            item {
                CategoryHeader(
                    category = category,
                    totalAmount = categoryExpenses.sumOf { it.amount },
                    count = categoryExpenses.size
                )
            }

            items(categoryExpenses) { expense ->
                DailyExpenseItem(
                    expense = expense,
                    onDelete = { onDeleteExpense(expense) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CategoryHeader(
    category: String,
    totalAmount: Double,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$category ($count)개",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = NumberFormat.getNumberInstance(Locale.KOREA).format(totalAmount) + "원",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun DailyExpenseItem(
    expense: Expense,
    onDelete: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.KOREA)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = timeFormat.format(Date(expense.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = NumberFormat.getNumberInstance(Locale.KOREA).format(expense.amount) + "원",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// 유틸리티 함수
fun filterExpensesByDate(expenses: List<Expense>, targetDate: Long): List<Expense> {
    val targetCalendar = Calendar.getInstance().apply { timeInMillis = targetDate }
    val targetYear = targetCalendar.get(Calendar.YEAR)
    val targetMonth = targetCalendar.get(Calendar.MONTH)
    val targetDay = targetCalendar.get(Calendar.DAY_OF_MONTH)

    return expenses.filter { expense ->
        val expenseCalendar = Calendar.getInstance().apply { timeInMillis = expense.date }
        expenseCalendar.get(Calendar.YEAR) == targetYear &&
                expenseCalendar.get(Calendar.MONTH) == targetMonth &&
                expenseCalendar.get(Calendar.DAY_OF_MONTH) == targetDay
    }.sortedByDescending { it.date } // 최신 순으로 정렬
}
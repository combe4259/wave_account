package com.example.accountbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accountbook.model.Expense
import com.example.accountbook.view.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarMainScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier,
    onDateSelected: (Long) -> Unit
) {
    val expenses by viewModel.allExpenses.observeAsState(emptyList())
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }

    // 월별 지출 데이터 계산
    val monthlyExpenses = remember(expenses, currentMonth) {
        calculateMonthlyExpenses(expenses, currentMonth)
    }

    Column(modifier = modifier.padding(16.dp)) {
        // 월 네비게이션 헤더
        MonthNavigationHeader(
            currentMonth = currentMonth,
            onPreviousMonth = {
                currentMonth = Calendar.getInstance().apply {
                    time = currentMonth.time
                    add(Calendar.MONTH, -1)
                }
            },
            onNextMonth = {
                currentMonth = Calendar.getInstance().apply {
                    time = currentMonth.time
                    add(Calendar.MONTH, 1)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 요일 헤더
        WeekdayHeader()

        Spacer(modifier = Modifier.height(8.dp))

        // 달력 그리드
        CalendarGrid(
            currentMonth = currentMonth,
            monthlyExpenses = monthlyExpenses,
            onDateClick = onDateSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 월간 총 지출 요약
        MonthSummaryCard(monthlyExpenses = monthlyExpenses)
    }
}

@Composable
fun MonthNavigationHeader(
    currentMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthFormat = SimpleDateFormat("yyyy년 MM월", Locale.KOREA)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "이전 달")
        }

        Text(
            text = monthFormat.format(currentMonth.time),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "다음 달")
        }
    }
}

@Composable
fun WeekdayHeader() {
    val weekdays = listOf("일", "월", "화", "수", "목", "금", "토")

    Row(modifier = Modifier.fillMaxWidth()) {
        weekdays.forEach { day ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    monthlyExpenses: Map<Int, Double>,
    onDateClick: (Long) -> Unit
) {
    val daysInMonth = getDaysInMonth(currentMonth)

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(daysInMonth) { dayInfo ->
            CalendarDay(
                dayInfo = dayInfo,
                totalExpense = monthlyExpenses[dayInfo.day] ?: 0.0,
                onDateClick = onDateClick
            )
        }
    }
}

@Composable
fun CalendarDay(
    dayInfo: DayInfo,
    totalExpense: Double,
    onDateClick: (Long) -> Unit
) {
    val isToday = isToday(dayInfo.date)
    val hasExpense = totalExpense > 0

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onDateClick(dayInfo.date) },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isToday -> MaterialTheme.colorScheme.primaryContainer
                hasExpense -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (hasExpense) 2.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (dayInfo.isCurrentMonth) dayInfo.day.toString() else "",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    !dayInfo.isCurrentMonth -> Color.Transparent
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            if (hasExpense && dayInfo.isCurrentMonth) {
                Text(
                    text = formatCurrency(totalExpense),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MonthSummaryCard(monthlyExpenses: Map<Int, Double>) {
    val totalMonthExpense = monthlyExpenses.values.sum()
    val expenseDays = monthlyExpenses.filter { it.value > 0 }.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "이번 달 총 지출",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = NumberFormat.getNumberInstance(Locale.KOREA).format(totalMonthExpense) + "원",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${expenseDays}일 지출 기록",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// 데이터 클래스와 유틸리티 함수들
data class DayInfo(
    val day: Int,
    val date: Long,
    val isCurrentMonth: Boolean
)

fun getDaysInMonth(calendar: Calendar): List<DayInfo> {
    val result = mutableListOf<DayInfo>()
    val tempCal = Calendar.getInstance().apply { time = calendar.time }

    // 월의 첫 날로 이동
    tempCal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1

    // 이전 달의 마지막 며칠 추가
    val prevMonth = Calendar.getInstance().apply {
        time = tempCal.time
        add(Calendar.MONTH, -1)
    }
    val daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

    for (i in firstDayOfWeek - 1 downTo 0) {
        val day = daysInPrevMonth - i
        val date = Calendar.getInstance().apply {
            time = prevMonth.time
            set(Calendar.DAY_OF_MONTH, day)
        }.timeInMillis
        result.add(DayInfo(day, date, false))
    }

    // 현재 달의 모든 날 추가
    val daysInCurrentMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (day in 1..daysInCurrentMonth) {
        val date = Calendar.getInstance().apply {
            time = tempCal.time
            set(Calendar.DAY_OF_MONTH, day)
        }.timeInMillis
        result.add(DayInfo(day, date, true))
    }

    // 다음 달의 첫 며칠 추가 (6줄 맞추기)
    val remainingDays = 42 - result.size // 6주 * 7일 = 42
    for (day in 1..remainingDays) {
        val nextMonth = Calendar.getInstance().apply {
            time = tempCal.time
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, day)
        }
        result.add(DayInfo(day, nextMonth.timeInMillis, false))
    }

    return result
}

fun calculateMonthlyExpenses(expenses: List<Expense>, currentMonth: Calendar): Map<Int, Double> {
    val result = mutableMapOf<Int, Double>()
    val targetYear = currentMonth.get(Calendar.YEAR)
    val targetMonth = currentMonth.get(Calendar.MONTH)

    expenses.forEach { expense ->
        val expenseDate = Calendar.getInstance().apply { timeInMillis = expense.date }
        if (expenseDate.get(Calendar.YEAR) == targetYear &&
            expenseDate.get(Calendar.MONTH) == targetMonth) {
            val day = expenseDate.get(Calendar.DAY_OF_MONTH)
            result[day] = (result[day] ?: 0.0) + expense.amount
        }
    }

    return result
}

fun isToday(date: Long): Boolean {
    val today = Calendar.getInstance()
    val targetDate = Calendar.getInstance().apply { timeInMillis = date }

    return today.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == targetDate.get(Calendar.DAY_OF_YEAR)
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
    return formatter.format(amount.toInt())
}
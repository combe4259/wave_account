package com.example.accountbook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accountbook.dto.DayInfo
import com.example.accountbook.dto.ExpenseWithCategory
import com.example.accountbook.dto.MonthlyExpenseData
import com.example.accountbook.ui.theme.MainColor
import com.example.accountbook.view.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// 달력 메인 구현
@Composable
fun CalendarMainScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier,
    onDateSelected: (Long) -> Unit
) {
    val expensesWithCategory by viewModel.allExpensesWithCategory.observeAsState(emptyList())
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedTab by remember { mutableStateOf(0) } // 0: 달력, 1: 일일


    //하나의 통합된 계산 -> 모든 필요한 데이터 생성
    val monthlyData = remember(expensesWithCategory, currentMonth) {
        calculateMonthlyExpenseData(expensesWithCategory, currentMonth)
    }

    Scaffold(

        containerColor = MaterialTheme.colorScheme.onPrimary,
        floatingActionButton = {

            FloatingActionButton(
                onClick = {onDateSelected(System.currentTimeMillis()) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "지출 추가",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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

            // 탭 추가
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("달력")
                    }
                }

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("일일")
                    }
                }
            }


            // 탭 내용
            when (selectedTab) {
                0 -> {
                    // 달력 탭 - 통합된 데이터 사용
                    CalendarTabContent(
                        monthlyData = monthlyData,
                        onDateSelected = onDateSelected
                    )
                }

                1 -> {
                    // 일일 탭
                    DailyListTabContent(
                        monthlyData = monthlyData,
                        onExpenseClick = { expense ->
                            onDateSelected(expense.date)
                        },
                        onDeleteExpense = { expense ->
                            viewModel.deleteExpense(expense.toExpense())
                        }
                    )
                }
            }
        }
    }
}


// 공통 월별 요약 컴포넌트
@Composable
fun CommonMonthSummaryCard(
    totalAmount: Double,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onContainerColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            //containerColor = MaterialTheme.colorScheme.surfaceVariant
            containerColor = MainColor.copy(alpha = 0.1f)

        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "이번 달 지출",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = NumberFormat.getNumberInstance(Locale.KOREA).format(totalAmount) + "원",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


// 달력 탭
@Composable
private fun CalendarTabContent(
    monthlyData: MonthlyExpenseData,
    onDateSelected: (Long) -> Unit
) {
    Column {
        // 공통 요약 카드 사용
        CommonMonthSummaryCard(
            totalAmount = monthlyData.totalAmount
        )

        // 요일 헤더
        WeekdayHeader()

        Spacer(modifier = Modifier.height(8.dp))

        // 달력 그리드 - 기존 로직 유지하되 통합된 데이터 사용
        CalendarGrid(
            currentMonth = monthlyData.month,
            monthlyExpenses = monthlyData.dailyTotals,
            onDateClick = onDateSelected
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

//  일일 리스트 탭
@Composable
private fun DailyListTabContent(
    monthlyData: MonthlyExpenseData,
    onExpenseClick: (ExpenseWithCategory) -> Unit,
    onDeleteExpense: (ExpenseWithCategory) -> Unit
) {
    if (monthlyData.allExpenses.isEmpty()) {
        // 빈 상태 표시
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "이번 달에는 지출 내역이 없습니다",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {

        // 공통 요약
        CommonMonthSummaryCard(
            totalAmount = monthlyData.totalAmount
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 날짜별로 그룹핑된 지출 목록
        val groupedExpenses = monthlyData.allExpenses.groupBy { expense ->
            SimpleDateFormat("dd", Locale.KOREA).format(Date(expense.date)).toInt()
        }.toSortedMap(reverseOrder())

        // 날짜별 지출 목록
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groupedExpenses.forEach { (day, dailyExpenses) ->
                // 날짜 헤더
                item {
                    DayExpenseHeader(
                        day = day,
                        expenses = dailyExpenses
                    )
                }
                // 해당 날짜의 지출들
                items(dailyExpenses) { expense ->
                    DailyExpenseItem(
                        expense = expense,
                        onClick = { onExpenseClick(expense) },
                        onDelete = { onDeleteExpense(expense) }
                    )
                }
                // 날짜 섹션 간 간격
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun DayExpenseHeader(
    day: Int,
    expenses: List<ExpenseWithCategory>
) {
    val dayTotal = expenses.sumOf { it.amount }
    val expenseCount = expenses.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${day}일",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "(${expenseCount}건)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = NumberFormat.getNumberInstance(Locale.KOREA).format(dayTotal) + "원",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
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
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "다음 달")
        }
    }
}

@Composable
fun WeekdayHeader() {
    val weekdays = listOf("일", "월", "화", "수", "목", "금", "토")

    Row(modifier = Modifier.fillMaxWidth()) {
        weekdays.forEachIndexed { index, day ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = when (index) {
                        0 -> Color.Red  // 일요일 (첫 번째)
                        6 -> Color.Red  // 토요일 (마지막)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant  // 평일
                    }
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
    // 주말 여부 판단
    val isWeekendDay = isWeekend(dayInfo.date)

    if (!dayInfo.isCurrentMonth) {
        Box(modifier = Modifier.aspectRatio(1f))
        return
    }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onDateClick(dayInfo.date) },
        colors = CardDefaults.cardColors(
            containerColor = when {
                hasExpense -> Color.White
                else -> Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (hasExpense) 2.dp else 1.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 오늘 날짜일 때 상단 바 색상
            if (isToday) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .background(MainColor)
                        .align(Alignment.TopCenter)

                )
            }

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
                        isToday -> MainColor
                        isWeekendDay -> Color.Red
                        else -> MaterialTheme.colorScheme.onSurface  // 평일= 기본 색상
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
                        isToday -> MainColor  // 오늘 날짜는 여전히 MainColor
                        isWeekendDay -> Color.Red  // 주말은 빨간색으로 표시
                        else -> MaterialTheme.colorScheme.onSurface  // 평일은 기본 색상
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
}

// 월별 데이터를 한 번에 계산하는 통합 함수
fun calculateMonthlyExpenseData(
    expenses: List<ExpenseWithCategory>,
    currentMonth: Calendar
): MonthlyExpenseData {
    val targetYear = currentMonth.get(Calendar.YEAR)
    val targetMonth = currentMonth.get(Calendar.MONTH)

    val monthExpenses = expenses.filter { expense ->
        val expenseDate = Calendar.getInstance().apply { timeInMillis = expense.date }
        expenseDate.get(Calendar.YEAR) == targetYear &&
                expenseDate.get(Calendar.MONTH) == targetMonth
    }.sortedByDescending { it.date }

    val dailyTotals = monthExpenses.groupBy { expense ->
        val expenseDate = Calendar.getInstance().apply { timeInMillis = expense.date }
        expenseDate.get(Calendar.DAY_OF_MONTH)
    }.mapValues { (_, dayExpenses) ->
        dayExpenses.sumOf { it.amount }
    }

    return MonthlyExpenseData(
        totalAmount = monthExpenses.sumOf { it.amount },
        expenseCount = monthExpenses.size,
        expenseDays = dailyTotals.keys.size,
        dailyTotals = dailyTotals,
        allExpenses = monthExpenses,
        month = currentMonth
    )
}

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

fun isWeekend(date: Long): Boolean {
    val calendar = Calendar.getInstance().apply { timeInMillis = date }
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
}
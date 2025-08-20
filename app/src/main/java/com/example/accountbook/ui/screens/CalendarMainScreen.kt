package com.example.accountbook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accountbook.dto.DayInfo
import com.example.accountbook.dto.ExpenseWithCategory
import com.example.accountbook.dto.IncomeWithCategory
import com.example.accountbook.model.Expense
import com.example.accountbook.model.Income
import com.example.accountbook.ui.components.LiquidFill
import com.example.accountbook.ui.components.getIconEmoji
import com.example.accountbook.ui.theme.MainColor
import com.example.accountbook.presentation.adapter.ViewModelAdapter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// 수입/지출 통합 데이터 클래스
data class MonthlyIncomeExpenseData(
    val totalIncome: Double,
    val totalExpense: Double,
    val incomeCount: Int,
    val expenseCount: Int,
    val dailyIncomeTotals: Map<Int, Double>,
    val dailyExpenseTotals: Map<Int, Double>,
    val allIncomes: List<IncomeWithCategory>,
    val allExpenses: List<ExpenseWithCategory>,
    val month: Calendar
)

// 달력 메인 구현
@Composable
fun CalendarMainScreen(
    viewModel: ViewModelAdapter,
    monthlyGoal: Int,
    modifier: Modifier = Modifier,
    onDateSelected: (Long) -> Unit,
    onNavigateToAdd: ((Long) -> Unit)? = null
) {
    val expensesWithCategory by viewModel.allExpensesWithCategory.observeAsState(emptyList())
    val incomesWithCategory by viewModel.allIncomesWithCategory.observeAsState(emptyList()) // 수입 데이터 추가
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedTab by remember { mutableStateOf(0) } // 0: 달력, 1: 일일

    //파도 토글
    var isWaveEnabled by remember { mutableStateOf(true) }

    // 수입과 지출을 함께 계산하는 통합 데이터
    val monthlyData = remember(expensesWithCategory, incomesWithCategory, currentMonth) {
        calculateMonthlyIncomeExpenseData(expensesWithCategory, incomesWithCategory, currentMonth)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // 파도 토글 FAB (LiquidFill 효과 포함)
                if (selectedTab == 0){
                    Box(
                        modifier = Modifier.size(56.dp)
                    ) {
                        //  LiquidFill 배경
                        if (isWaveEnabled) {
                            LiquidFill(
                                progress = 0.8f, // 고정된 progress로 항상 파도 표시
                                waveColor = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                            )

                        }
                        else LiquidFill(
                            progress = 0.2f, // 고정된 progress로 항상 파도 표시
                            waveColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CircleShape)
                        )
                        // FAB 버튼
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    color = if (isWaveEnabled) Color.Transparent else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { isWaveEnabled = !isWaveEnabled },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "",
                                fontSize = 24.sp,
                                color = if (isWaveEnabled) Color.White else Color.Black
                            )
                        }
                    }

                }


                //지출 추가
                FloatingActionButton(
                    onClick = { onDateSelected(System.currentTimeMillis()) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "지출 추가",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
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
                isWaveEnabled = isWaveEnabled,
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
                },
                onWaveToggle = { isWaveEnabled = !isWaveEnabled }
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
                    // 달력 탭
                    CalendarTabContent(
                        monthlyData = monthlyData,
                        onDateSelected = onDateSelected,
                        isWaveEnabled = isWaveEnabled,
                        monthlyGoal = monthlyGoal
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
                        },
                        onIncomeClick = { income ->
                            onDateSelected(income.date)
                        },
                        onDeleteIncome = { income ->
                            viewModel.deleteIncome(income.toIncome())
                        }
                    )
                }
            }
        }
    }
}

// 수입/지출 통합 요약 카드
@Composable
fun IncomeExpenseSummaryCard(
    totalIncome: Double,
    totalExpense: Double
) {
    val balance = totalIncome - totalExpense

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 수입 섹션
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "수입",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = NumberFormat.getNumberInstance(Locale.KOREA).format(totalIncome) + "원",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFff4949)
                )
            }

            // 지출 섹션
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "지출",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = NumberFormat.getNumberInstance(Locale.KOREA).format(totalExpense) + "원",
                    style = MaterialTheme.typography.titleSmall,
                    //color = Color(0xFFff4949)
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // 잔액 섹션
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "총합",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = NumberFormat.getNumberInstance(Locale.KOREA).format(balance) + "원",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (balance >= 0) Color(0xFFff4949) else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


// 달력 탭 내용
@Composable
private fun CalendarTabContent(
    monthlyData: MonthlyIncomeExpenseData,
    onDateSelected: (Long) -> Unit,
    isWaveEnabled: Boolean,
    monthlyGoal: Int
) {
    Column {
        // 수입/지출 통합 요약 카드 사용
        IncomeExpenseSummaryCard(
            totalIncome = monthlyData.totalIncome,
            totalExpense = monthlyData.totalExpense
        )

        // 요일 헤더
        WeekdayHeader()

        Spacer(modifier = Modifier.height(8.dp))

        // 달력 그리드
        CalendarGrid(
            currentMonth = monthlyData.month,
            monthlyExpenses = monthlyData.dailyExpenseTotals,
            monthlyIncomes = monthlyData.dailyIncomeTotals,
            onDateClick = onDateSelected,
            isWaveEnabled = isWaveEnabled,
            monthlyGoal = monthlyGoal
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// 일일 리스트 탭 내용
@Composable
private fun DailyListTabContent(
    monthlyData: MonthlyIncomeExpenseData,
    onExpenseClick: (ExpenseWithCategory) -> Unit,
    onDeleteExpense: (ExpenseWithCategory) -> Unit,
    onIncomeClick: (IncomeWithCategory) -> Unit,
    onDeleteIncome: (IncomeWithCategory) -> Unit
) {
    Column {
        // 수입/지출 통합 요약
        IncomeExpenseSummaryCard(
            totalIncome = monthlyData.totalIncome,
            totalExpense = monthlyData.totalExpense
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (monthlyData.allExpenses.isEmpty() && monthlyData.allIncomes.isEmpty()) {
            // 빈 상태 표시
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "이번 달에는 수입/지출 내역이 없습니다",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // 날짜별로 수입과 지출을 함께 그룹핑
            val groupedData = (monthlyData.allExpenses + monthlyData.allIncomes)
                .groupBy { item ->
                    SimpleDateFormat("dd", Locale.KOREA).format(Date(
                        when(item) {
                            is ExpenseWithCategory -> item.date
                            is IncomeWithCategory -> item.date
                            else -> 0L
                        }
                    )).toInt()
                }
                .toSortedMap(reverseOrder())

            // 날짜별 수입/지출 목록
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedData.forEach { (day, dailyItems) ->
                    // 날짜 헤더
                    item {
                        DayIncomeExpenseHeader(
                            day = day,
                            items = dailyItems
                        )
                    }
                    // 해당 날짜의 수입/지출들
                    items(dailyItems) { item ->
                        when (item) {
                            is ExpenseWithCategory -> {
                                DailyExpenseItem(
                                    expense = item,
                                    onClick = { onExpenseClick(item) },
                                    onDelete = { onDeleteExpense(item) }
                                )
                            }
                            is IncomeWithCategory -> {
                                DailyIncomeItem(
                                    income = item,
                                    onClick = { onIncomeClick(item) },
                                    onDelete = { onDeleteIncome(item) }
                                )
                            }
                        }
                    }
                    // 날짜 섹션 간 간격
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

// 날짜별 수입/지출 헤더
@Composable
private fun DayIncomeExpenseHeader(
    day: Int,
    items: List<Any>
) {
    val dayIncome = items.filterIsInstance<IncomeWithCategory>().sumOf { it.amount }
    val dayExpense = items.filterIsInstance<ExpenseWithCategory>().sumOf { it.amount }
    val itemCount = items.size

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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${day}일",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (dayIncome > 0) {
                    Text(
                        text = NumberFormat.getNumberInstance(Locale.KOREA).format(dayIncome) + "원",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFff4949)
                    )
                }
                if (dayExpense > 0) {
                    Text(
                        text = NumberFormat.getNumberInstance(Locale.KOREA).format(dayExpense) + "원",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }


            }
        }
    }
}


// 일일 수입 아이템
@Composable
fun DailyIncomeItem(
    income: IncomeWithCategory,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                income.iconName?.let { iconName ->
                    Text(
                        text = getIconEmoji(iconName),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Column {
                    Text(
                        text = income.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1, // 추가
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = income.categoryName ?: "카테고리 없음",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFff4949)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date(income.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text =  NumberFormat.getNumberInstance(Locale.KOREA).format(income.amount) + "원",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFff4949)
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
@Composable
fun MonthNavigationHeader(
    currentMonth: Calendar,
    isWaveEnabled: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onWaveToggle: () -> Unit
) {
    val monthFormat = SimpleDateFormat("yyyy년 MM월", Locale.KOREA)

    Column {
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
    monthlyIncomes: Map<Int, Double>,
    onDateClick: (Long) -> Unit,
    isWaveEnabled: Boolean,
    monthlyGoal: Int
) {
    val daysInMonth = getDaysInMonth(currentMonth)
    val daysCount   = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dailyBudget = monthlyGoal.toFloat() / daysCount

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(daysInMonth) { dayInfo ->
            CalendarDay(
                dayInfo = dayInfo,
                totalExpense = monthlyExpenses[dayInfo.day] ?: 0.0,
                totalIncome = monthlyIncomes[dayInfo.day] ?:0.0,
                onDateClick = onDateClick,
                dailyBudget = dailyBudget,
                isWaveEnabled = isWaveEnabled
            )
        }
    }
}

@Composable
fun CalendarDay(
    dayInfo: DayInfo,
    totalExpense: Double,
    totalIncome: Double,
    onDateClick: (Long) -> Unit,
    dailyBudget: Float,
    isWaveEnabled: Boolean
) {
    val isToday = isToday(dayInfo.date)
    val hasExpense = totalExpense > 0
    val hasIncome = totalIncome > 0
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
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (hasExpense || hasIncome) 2.dp else 1.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            if(isWaveEnabled && hasExpense){
                val progress = (totalExpense / dailyBudget).toFloat()
                val waveColor = MaterialTheme.colorScheme.tertiary
                LiquidFill(
                    progress = progress,
                    waveColor = waveColor,
                    modifier = Modifier.matchParentSize()          // fills the whole cell
                )
            }


            if (isToday) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
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
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                if ((hasIncome || hasExpense) && dayInfo.isCurrentMonth) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(0.dp)

                    ) {
                        if (hasIncome) {
                            Text(
                                text = formatCurrency(totalIncome),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFff4949), // 수입 색상 (빨간색)
                                fontSize = 8.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 8.sp
                            )
                        }
                        if (hasExpense) {
                            Text(
                                text = formatCurrency(totalExpense),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary, // 지출 색상 (파란색)
                                fontSize = 8.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 8.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 수입/지출 통합 월별 데이터 계산 함수
fun calculateMonthlyIncomeExpenseData(
    expenses: List<ExpenseWithCategory>,
    incomes: List<IncomeWithCategory>,
    currentMonth: Calendar
): MonthlyIncomeExpenseData {
    val targetYear = currentMonth.get(Calendar.YEAR)
    val targetMonth = currentMonth.get(Calendar.MONTH)

    val monthExpenses = expenses.filter { expense ->
        val expenseDate = Calendar.getInstance().apply { timeInMillis = expense.date }
        expenseDate.get(Calendar.YEAR) == targetYear &&
                expenseDate.get(Calendar.MONTH) == targetMonth
    }.sortedByDescending { it.date }

    val monthIncomes = incomes.filter { income ->
        val incomeDate = Calendar.getInstance().apply { timeInMillis = income.date }
        incomeDate.get(Calendar.YEAR) == targetYear &&
                incomeDate.get(Calendar.MONTH) == targetMonth
    }.sortedByDescending { it.date }

    val dailyExpenseTotals = monthExpenses.groupBy { expense ->
        val expenseDate = Calendar.getInstance().apply { timeInMillis = expense.date }
        expenseDate.get(Calendar.DAY_OF_MONTH)
    }.mapValues { (_, dayExpenses) ->
        dayExpenses.sumOf { it.amount }
    }

    val dailyIncomeTotals = monthIncomes.groupBy { income ->
        val incomeDate = Calendar.getInstance().apply { timeInMillis = income.date }
        incomeDate.get(Calendar.DAY_OF_MONTH)
    }.mapValues { (_, dayIncomes) ->
        dayIncomes.sumOf { it.amount }
    }

    return MonthlyIncomeExpenseData(
        totalIncome = monthIncomes.sumOf { it.amount },
        totalExpense = monthExpenses.sumOf { it.amount },
        incomeCount = monthIncomes.size,
        expenseCount = monthExpenses.size,
        dailyIncomeTotals = dailyIncomeTotals,
        dailyExpenseTotals = dailyExpenseTotals,
        allIncomes = monthIncomes,
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

// IncomeWithCategory를 Income으로 변환
fun IncomeWithCategory.toIncome(): Income {
    return Income(
        id = this.id,
        amount = this.amount,
        description = this.description,
        date = this.date,
        categoryId = this.categoryId
    )
}
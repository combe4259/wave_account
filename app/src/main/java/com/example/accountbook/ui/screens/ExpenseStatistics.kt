package com.example.accountbook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accountbook.presentation.adapter.ViewModelAdapter
import com.github.mikephil.charting.utils.ColorTemplate
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.PieChartOutline
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.accountbook.dto.ExpenseWithCategory
import com.example.accountbook.ui.components.BottomStats
import com.example.accountbook.ui.components.ChartHeaderWithMonthToggle
import com.example.accountbook.ui.components.FirstLineChartDemo
import com.example.accountbook.ui.components.MonthlyGoalCard
import com.example.accountbook.ui.components.MonthlyGoalRow
import com.example.accountbook.ui.components.PieChartByCategory
import com.example.accountbook.ui.components.SecondLineChartDemo
import java.time.YearMonth

@Composable
fun OverviewTab(
    currentMonth: Calendar,
    onMonthChange: (Calendar) -> Unit,
    viewModel: ViewModelAdapter,
    sumsThisMonth: List<Pair<String, Float>>,
    hasExpenses: Boolean,
    modifier: Modifier = Modifier
) {

    val legendRows  = sumsThisMonth.size + 1
    val spacerH = 30
    val legendH     = (legendRows + 1) * 32      // 28 dp per row
    val pieH        = 240                  // keep the graphic 240 dp
    val pieSectionH = (pieH + spacerH + legendH).dp

    Column(
        modifier = Modifier
            .fillMaxWidth(),
//            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MonthNavigationHeaderfotStatistics(
            currentMonth = currentMonth,
            onPreviousMonth = {
                onMonthChange(
                    Calendar.getInstance().apply {
                        time = currentMonth.time
                        add(Calendar.MONTH, -1)
                    }
                )
            },
            onNextMonth = {
                onMonthChange(
                    Calendar.getInstance().apply {
                        time = currentMonth.time
                        add(Calendar.MONTH, 1)
                    }
                )
            }
        )

        if (!hasExpenses) {
            Text(
                "지출 내역 없음",
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
//            style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 18.sp
            )
            return
        } else {
            FirstLineChartDemo(
                currentMonth = currentMonth,
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            PieChartByCategory(
                currentMonth = currentMonth,
                viewModel = viewModel,
                sumsThisMonth = sumsThisMonth,
                chartHeight = pieSectionH,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        }
    }
}

@Composable
fun ProjectionTab(
    currentMonth: Calendar,
    viewModel: ViewModelAdapter,
    expenses: List<ExpenseWithCategory>,
    sumsThisMonth: List<Pair<String, Float>>,
    sliceColors: List<Int>,
    monthlyGoal: Int,
    onGoalChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // ◀ 이전 달이 기본값
    var compareMonth by rememberSaveable {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)   // 날짜 1일로 맞춰 두면 안전
            add(Calendar.MONTH, -1)
        })
    }
    val earliestMonth = remember(expenses) {
        expenses.minOfOrNull { exp ->
            Calendar.getInstance().apply {
                timeInMillis = exp.date
                set(Calendar.DAY_OF_MONTH, 1)
            }
        } ?: Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        MonthlyGoalCard(
            goal = monthlyGoal,
            onGoalChange = onGoalChange
        )

        ChartHeaderWithMonthToggle(
            title = "누적 지출 비교",
            selectedMonth = compareMonth,
            earliestMonth = earliestMonth,
            onMonthChange = { compareMonth = it}
        )

        SecondLineChartDemo(
            headerMonth = currentMonth,
            compareMonth = compareMonth,
            monthlyGoal = monthlyGoal,
            viewModel   = viewModel,
            modifier    = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        BottomStats(
            expenses      = expenses,
            sumsThisMonth = sumsThisMonth,
            sliceColors   = sliceColors
        )
    }
}

@Composable
fun ExpenseStatisticsScreen(
    modifier: Modifier = Modifier,
    monthlyGoal: Int,
    onGoalChange: (Int) -> Unit,
    viewModel: ViewModelAdapter = viewModel()
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    // 스크롤 상태를 UI에서 직접 관리 (SavedState로 화면 회전 대응)
    val scrollState = rememberScrollState(
        initial = rememberSaveable { 0 }
    )
    val expensesWithCategory by viewModel.allExpensesWithCategory.observeAsState(initial = emptyList())
    val year  = currentMonth.get(Calendar.YEAR)
    val month = currentMonth.get(Calendar.MONTH) + 1
    val sumsThisMonth = remember(expensesWithCategory, year, month) {
        expensesWithCategory
            .map { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() to it }
            .filter { (d, _) -> d.year == year && d.monthValue == month }
            .map { it.second }
            .groupBy { it.categoryName?.trim().orEmpty().ifEmpty { "미분류" } }
            .mapValues { (_, list) -> list.sumOf { it.amount }.toFloat() }
            .toList()
            .sortedByDescending { it.second }
    }
    val sliceColors = listOf(
        Color(0xFF5E69EE).copy(alpha = 0.8f).toArgb(), // indigo-violet
        Color(0xFF4966D4).copy(alpha = 0.8f).toArgb(), // deep periwinkle
        Color(0xFF3E8BEB).copy(alpha = 0.8f).toArgb(), // sky blue
        Color(0xFF39AFEA).copy(alpha = 0.8f).toArgb(), // cyan-blue
        Color(0xFF2FB7D5).copy(alpha = 0.8f).toArgb(), // teal-blue
        Color(0xFF44C6DC).copy(alpha = 0.8f).toArgb()  // turquoise-blue
    )
    val hasExpensesThisMonth = expensesWithCategory.any {
        val date = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
        date.year == year && date.monthValue == month
    }
    var selectedTab by remember { mutableStateOf(0)}

    Column(
        modifier = modifier
            //Fixme
            //.background(MaterialTheme.colorScheme.secondary)
            .fillMaxSize()
            .verticalScroll(scrollState)
            .navigationBarsPadding()
            .padding(16.dp),         // outer padding around the whole stack

        verticalArrangement = Arrangement.spacedBy(12.dp) // space between charts
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor   = MaterialTheme.colorScheme.surface,
            contentColor     = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PieChartOutline, contentDescription = null, Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("개요")
                }
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("권장 지출")
                }
            }
        }
        when (selectedTab) {
            0 -> OverviewTab(
                currentMonth   = currentMonth,
                onMonthChange = { currentMonth = it},
                viewModel      = viewModel,
                sumsThisMonth  = sumsThisMonth,
                hasExpenses    = hasExpensesThisMonth,
            )
            1 -> ProjectionTab(
                currentMonth   = currentMonth,
                viewModel      = viewModel,
                expenses       = expensesWithCategory,
                sumsThisMonth  = sumsThisMonth,
                sliceColors    = sliceColors,
                monthlyGoal = monthlyGoal,
                onGoalChange = onGoalChange
            )
        }
    }
}

@Composable
fun MonthNavigationHeaderfotStatistics(
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
package com.example.accountbook.ui.screens

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accountbook.view.ExpenseViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.lastOrNull
import android.util.Log
import android.view.WindowInsets
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.PieChartOutline
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.accountbook.dto.ExpenseWithCategory
import com.example.accountbook.model.Expense
import com.example.accountbook.ui.components.BottomStats
import com.example.accountbook.ui.components.SimpleLineMarker
import com.example.accountbook.ui.components.StatisticTopBar
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.NumberFormat
import java.time.Month
import kotlin.math.min
import kotlin.math.roundToInt

private const val MONTHLY_LIMIT = 1_000_000f      // 1 M ₩ target

@Composable
fun FirstLineChartDemo(
    modifier: Modifier = Modifier,
    currentMonth: Calendar,
    viewModel: ExpenseViewModel = viewModel()
) {
    val expensesWithCategory by viewModel.allExpensesWithCategory.observeAsState(initial = emptyList())
    val year  = currentMonth.get(Calendar.YEAR)
    val monthNumber = currentMonth.get(Calendar.MONTH) + 1
    val daysInMonth  = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = LocalDate.now()
    val daysToShow = if (
        today.year == year && today.monthValue == monthNumber
    ) {
        today.dayOfMonth
    } else {
        daysInMonth
    }

    val dailySums: Map<Int, Float> = expensesWithCategory
        .map { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() to it.amount }
        .filter { (date, _) ->
            date.year == year && date.monthValue == monthNumber
        }
        .groupBy   { (date, _) -> date.dayOfMonth }
        .mapValues { (_, list) -> list.sumOf { it.second }.toFloat() }

    val entries = (1..daysToShow).map { d ->
        Entry((d - 1).toFloat(), dailySums[d] ?: 0f)
    }
    val primary = MaterialTheme.colorScheme.primary
    val primaryInt   = primary.toArgb()
    val labels = (1..daysToShow).map { d ->
        "%d/%d".format(monthNumber, d)  // “07/05”
    }

    Box(modifier = modifier.fillMaxSize()) {
        key(currentMonth.timeInMillis) {
            AndroidView(
                factory = { ctx ->
                    LineChart(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setDrawGridBackground(false)
                        xAxis.setDrawGridLines(false)       // vertical grid lines
                        axisLeft.setDrawGridLines(false)    // horizontal grid lines (left side)
                        axisRight.setDrawGridLines(false)

                        description.isEnabled = false
                        axisRight.isEnabled = false
                        isHighlightPerTapEnabled = true
                        marker = SimpleLineMarker(ctx, labels)
                        animateX(300)

                    }
                },
                update = { chart ->
                    chart.xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        granularity = 1f
//                        labelCount = labels.size
//                        valueFormatter =
//                            IndexAxisValueFormatter(labels)           // :contentReference[oaicite:13]{index=13}
                    }

                    val dataSet = LineDataSet(entries, "지출 추이").apply {
                        setDrawCircles(true)
                        lineWidth = 2f
                        circleRadius = 4f
                        setDrawValues(false)
                        color = primary.copy(alpha = 0.7f).toArgb()
                        circleColors = listOf(primaryInt)
                    }
                    chart.data = LineData(dataSet)
                    chart.legend.isEnabled = false
                    chart.invalidate()
                    chart.animateX(300)
                }
            )
        }
    }
}

@Composable
fun SecondLineChartDemo(
    modifier: Modifier = Modifier,
    headerMonth: Calendar,
    viewModel: ExpenseViewModel = viewModel()
) {
    // 1) Observe all expenses
    //val expenses by viewModel.allExpenses.observeAsState(emptyList())
    val expensesWithCategory by viewModel.allExpensesWithCategory.observeAsState(initial = emptyList())

    // 2) Compute date info
    val today = LocalDate.now()
    val thisYear  = today.year
    val thisMonth = today.monthValue
    val todayDay  = today.dayOfMonth

    val prevCal = (headerMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
    val prevYear  = prevCal.get(Calendar.YEAR)
    val prevMonth = prevCal.get(Calendar.MONTH) + 1
    val prevDays  = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // 3) Helper: build cumulative map for a given year/month
    fun cumulative(year: Int, month: Int, daysInMonth: Int): List<Float> {
        val byDay = expensesWithCategory
            .map { exp ->
                val ld = Instant.ofEpochMilli(exp.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                ld to exp.amount
            }
            .filter { (ld, _) -> ld.year == year && ld.monthValue == month }
            .groupBy { (ld, _) -> ld.dayOfMonth }
            .mapValues { (_, list) -> list.sumOf { it.second }.toFloat() }

        // fill
        val daily = (1..daysInMonth).map { d -> byDay[d] ?: 0f }
        // cumulative
        return daily.runningFold(0f) { acc, v -> acc + v }.drop(1)
    }

    val currCum  = cumulative(thisYear, thisMonth, todayDay)
    val cmpCum   = cumulative(prevYear, prevMonth, prevDays)

    val prevEntries = cmpCum.mapIndexed { idx, sum -> Entry((idx + 1).toFloat(), sum) }
    val currEntries = currCum.mapIndexed { idx, sum -> Entry((idx + 1).toFloat(), sum) }

    val labels = List(31) { "%02d-%02d".format(thisMonth, it + 1) }
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val primaryInt   = primary.toArgb()
    val tertiaryInt  = tertiary.toArgb()
    val red = Color(0xFFFF0000)

    fun makeGradientPrevious(ctx: Context, base: Color): Drawable {
        val startColor = base.copy(alpha = 0.1f).toArgb()
        val endColor = base.copy(alpha = 0.5f).toArgb()
        return GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP,
            intArrayOf(startColor, endColor)
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        key(headerMonth.timeInMillis) {
            AndroidView(
                factory = { ctx ->
                    LineChart(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        description.isEnabled = false
                        axisRight.isEnabled = false
                        xAxis.isEnabled = false
                        legend.isEnabled = false

                        axisLeft.apply {
                            axisMinimum = 0f
                            axisMaximum = 1_100_000f
                            spaceTop = 15f
                        }

                        // 5a) horizontal limit line at 1,000,000
                        axisLeft.removeAllLimitLines()
                        axisLeft.addLimitLine(
                            LimitLine(1_000_000f, "월 최대")
                                .apply {
                                    lineColor = red.copy(alpha = 0.6f).toArgb()
                                    lineWidth = 2f
                                    textColor = red.toArgb()
                                    textSize = 12f
                                }
                        )
                        animateX(400)
                    }
                },
                update = { chart ->
                    // 5b) X-axis config
                    chart.xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        granularity = 1f
                        labelCount = 31
                        valueFormatter = IndexAxisValueFormatter(labels)
                    }

                    val setCurr = LineDataSet(currEntries, "이번 달 누적").apply {
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        setDrawCircles(false)
                        lineWidth = 2f
                        setDrawValues(false)
                        color = primaryInt
                        circleColors = listOf(primaryInt)
                    }

                    val dataSets = mutableListOf<ILineDataSet>()

                    if (prevEntries.any { it.y != 0f }) {
                        val setCmp = LineDataSet(prevEntries, "선택 달 누적").apply {
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                            setDrawCircles(false)
                            setDrawFilled(true)
                            fillDrawable = makeGradientPrevious(chart.context, tertiary)
                            color = tertiary.copy(alpha = 0.4f).toArgb()
                            lineWidth = 2f
                            setDrawValues(false)
                        }
                        dataSets += setCmp
                    }
                    dataSets += setCurr

                    chart.data = LineData(dataSets)
                    chart.invalidate()
                    chart.animateX(400)
                }
            )
        }
    }
}

@Composable
fun PieChartByCategory(
    currentMonth: Calendar,
    sumsThisMonth: List<Pair<String, Float>>,
    viewModel: ExpenseViewModel = viewModel(),
    chartHeight: Dp,
    modifier: Modifier = Modifier
        .wrapContentHeight()
) {
    val expensesWithCategory by viewModel.allExpensesWithCategory.observeAsState(initial = emptyList())
    val year     = currentMonth.get(Calendar.YEAR)
    val monthNumber    = currentMonth.get(Calendar.MONTH) + 1

    val sumsByCat = expensesWithCategory
        .map { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() to it }
        .filter { (date, _) ->
            date.year == year && date.monthValue == monthNumber
        }
        .map { it.second }   // drop date
        .groupBy { it.categoryName?.trim().orEmpty().ifEmpty { "미분류" } }
        .mapValues { (_, list) -> list.sumOf { it.amount }.toFloat() }
        .toList()
        .sortedByDescending { it.second }
    val totalForMarker = sumsByCat.sumOf { it.second.toDouble() }.toFloat()
//    val entries = remember(currentMonth, sumsByCat) {
//        sumsByCat.mapNotNull { (cat, sum) ->
//            if (sum > 0f) PieEntry(sum, cat) else null
//        }
//    }

    val top5 = sumsByCat.take(5)
    val etcSum = sumsByCat.drop(5).sumOf { it.second.toDouble() }.toFloat()
    val pieEntries = buildList {
        addAll(top5.map { PieEntry(it.second, it.first) })
        if (etcSum > 0f) add(PieEntry(etcSum, "기타 지출"))
    }

    val baseColors = listOf(
        Color(0xFF5E69EE), // indigo-violet
        Color(0xFF4966D4), // deep periwinkle
        Color(0xFF3E8BEB), // sky blue
        Color(0xFF39AFEA), // cyan-blue
        Color(0xFF2FB7D5), // teal-blue
        Color(0xFF44C6DC)  // turquoise-blue
    )

    val sliceColors = pieEntries.mapIndexed { idx, _ ->
        baseColors.getOrNull(idx)?.copy(alpha = 0.8f)?.toArgb()
            ?: ColorTemplate.MATERIAL_COLORS[idx % ColorTemplate.MATERIAL_COLORS.size]
    }

    Column(modifier = modifier
        .fillMaxWidth()
        .height(chartHeight)
    ) {
        key(currentMonth.timeInMillis) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                factory = { ctx ->
                    PieChart(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setUsePercentValues(true)
                        description.isEnabled = false
                        legend.isEnabled = false
                        isDrawHoleEnabled = false
                        isHighlightPerTapEnabled = true
                        setTransparentCircleAlpha(0)
                        setEntryLabelTextSize(13f)
                        setEntryLabelColor(Color.Black.toArgb())
//                        setEntryLabelTypeface(Typeface.DEFAULT_BOLD)
                        animateY(500, Easing.EaseInOutQuad)
                    }
                },
                update = { chart ->
                    // 7) Build DataSet with category-specific colors
                    val dataSet = PieDataSet(pieEntries, "").apply {
                        colors = sliceColors
                        valueFormatter = PercentFormatter(chart)
                        valueTextSize = 13f
//                        valueTextColor = Color.Black.toArgb()
                        setValueTextColors(sliceColors)
                        valueTypeface = Typeface.DEFAULT_BOLD
                        sliceSpace = 2f

                        setDrawValues(true)
                        isUsingSliceColorAsValueLineColor = true
                        valueLinePart1OffsetPercentage = 75f
                        valueLinePart1Length = 0.7f
                        valueLinePart2Length = 0.8f
                        valueLineColor = Color.Black.toArgb()
                        yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                        xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                    }
                    chart.data = PieData(dataSet)
                    chart.invalidate()
                    chart.animateY(500, Easing.EaseInOutQuad)
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        val pctFormat = NumberFormat.getPercentInstance().apply { minimumFractionDigits = 1 }
        Column(modifier = Modifier.fillMaxWidth()) {
            sumsByCat.forEachIndexed { idx, (category, amount) ->
                val colorInt = sliceColors[min(idx, sliceColors.size - 1)]
                val pct = amount / totalForMarker * 100f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ① colored box with percent
                    val idx = sumsByCat.indexOfFirst { it.first == category }
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(colorInt)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%.0f%%", pct),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // ② category name
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // ③ formatted amount
                    Text(
                        text = String.format("%,d원", amount.toLong()),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(colorInt)
                    )
                }
            }
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "총 지출",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = String.format("%,d원", sumsThisMonth.sumOf { it.second.toDouble() }.toLong()),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun OverviewTab(
    currentMonth: Calendar,
    onMonthChange: (Calendar) -> Unit,
    viewModel: ExpenseViewModel,
    sumsThisMonth: List<Pair<String, Float>>,
    hasExpenses: Boolean,
    modifier: Modifier = Modifier
) {

    val legendRows  = sumsThisMonth.size + 1
    val legendH     = legendRows * 50      // 28 dp per row
    val pieH        = 240                  // keep the graphic 240 dp
    val pieSectionH = (pieH + legendH).dp

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
    viewModel: ExpenseViewModel,
    expenses: List<ExpenseWithCategory>,
    sumsThisMonth: List<Pair<String, Float>>,
    sliceColors: List<Int>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "누적 지출 금액",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        SecondLineChartDemo(
            headerMonth = currentMonth,
            viewModel   = viewModel,
            modifier    = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        BottomStats(
            currentMonth  = currentMonth,
            expenses      = expenses,
            sumsThisMonth = sumsThisMonth,
            sliceColors   = sliceColors
        )
    }
}


@Composable
fun ExpenseStatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = viewModel()
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    val scrollState = rememberScrollState(initial = viewModel.statsScroll)
    DisposableEffect(Unit) {
        onDispose {    // called when you navigate away
            viewModel.statsScroll = scrollState.value
        }
    }
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
    val baseColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFF2ED573),
        Color(0xFFEDCE5C),
        Color(0xFFFF9800),
        Color(0xFFFF6348)
    )
    val sliceColors = sumsThisMonth.mapIndexed { idx, _ ->
        baseColors.getOrNull(idx)?.copy(alpha = 0.8f)?.toArgb()
            ?: ColorTemplate.MATERIAL_COLORS[idx % ColorTemplate.MATERIAL_COLORS.size]
    }
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
//        Text(
//            text = "월별 지출현황",
//            style = MaterialTheme.typography.headlineSmall,
//            fontWeight = FontWeight.Bold,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.padding(bottom = 4.dp)
//                .fillMaxWidth()
//        )
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor   = Color.White,
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
                sliceColors    = sliceColors
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
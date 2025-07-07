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
import androidx.core.graphics.alpha
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accountbook.ui.theme.MainColor
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
import kotlin.random.Random
import com.example.accountbook.ui.screens.MonthNavigationHeader
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.lastOrNull
import android.util.Log
@Composable
fun FirstLineChartDemo(
    viewModel: ExpenseViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    // generate 12 points (month vs value)
    //val expenses by viewModel.allExpenses.observeAsState(initial = emptyList())
    val expensesWithCategory by viewModel.allExpensesWithCategory.observeAsState(initial = emptyList())
    val today = LocalDate.now()  // e.g. 2025-07-04
    val year  = today.year
    val month = today.monthValue
    val days  = today.dayOfMonth  // e.g. 4

    // Convert epoch-ms → LocalDate for each expense, filter by month/year
    val monthly = expensesWithCategory.map{ Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() to it.amount }
        .filter { (date, _) -> date.year == year && date.monthValue == month }

    // Group amounts by day-of-month and sum
    val dailySums: Map<Int, Float> = monthly
        .groupBy   { (date, _) -> date.dayOfMonth }
        .mapValues { (_, list) -> list.sumOf { it.second }.toFloat() }

    val entries = (1..days).map { d ->
        Entry(d.toFloat(), dailySums[d] ?: 0f)
    }
    val labels = (1..days).map { String.format("%02d-%02d", month, it) }

    val primary = MaterialTheme.colorScheme.primary
    val primaryInt   = primary.toArgb()
    val tertiaryInt  = MaterialTheme.colorScheme.tertiary.toArgb()

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                LineChart(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // chart styling
                    description.isEnabled = false
                    axisRight.isEnabled = false
                    animateX(300)

                }
            },
            update = { chart ->
                chart.xAxis.apply {
                    position        = XAxis.XAxisPosition.BOTTOM
                    granularity     = 1f
                    labelCount      = labels.size
                    valueFormatter  = IndexAxisValueFormatter(labels)           // :contentReference[oaicite:13]{index=13}
                }

                val dataSet = LineDataSet(entries, "지출 추이").apply {
                    setDrawCircles(true)
                    lineWidth = 2f
                    circleRadius = 4f
                    setDrawValues(false)
                    color = primary.copy(alpha=0.7f).toArgb()
                    circleColors = listOf(primaryInt)
                }
                chart.data = LineData(dataSet)
                chart.legend.isEnabled = false
                chart.invalidate()
            }
        )
    }
}

@Composable
fun SecondLineChartDemo(
    viewModel: ExpenseViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    // 1) Observe all expenses
    //val expenses by viewModel.allExpenses.observeAsState(emptyList())
    val expensesWithCategory by viewModel.allExpensesWithCategory.observeAsState(initial = emptyList())

    // 2) Compute date info
    val today = LocalDate.now()
    val thisYear  = today.year
    val thisMonth = today.monthValue
    val todayDay  = today.dayOfMonth

    val prevMonthDate = today.minusMonths(1)
    val prevYear  = prevMonthDate.year
    val prevMonth = prevMonthDate.monthValue
    // How many days in previous month:
    val prevMonthLength = prevMonthDate.lengthOfMonth()

    // 3) Helper: build cumulative map for a given year/month
    fun cumulativeSums(year: Int, month: Int, daysInMonth: Int): List<Float> {
        // filter+sum per day
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

    val prevCum = cumulativeSums(prevYear, prevMonth, prevMonthLength)
//        .let { listOfFloats ->                              // name the List<Float>
//            if (prevMonthLength < 31) {
//                listOfFloats + List(31 - prevMonthLength) {     // inner lambda’s 'index'
//                    // refer to the OUTER list via listOfFloats
//                    listOfFloats.lastOrNull() ?: 0f
//                }
//            } else {
//                listOfFloats
//            }
//        }

    val currCum = cumulativeSums(thisYear, thisMonth, todayDay)
//        .let { listOfFloats ->
//            listOfFloats + List(31 - todayDay) {
//                listOfFloats.lastOrNull() ?: 0f
//            }
//        }


    // 4) Build Entries for days 1..31
    val prevEntries = prevCum.mapIndexed { idx, sum -> Entry((idx + 1).toFloat(), sum) }
    val currEntries = currCum.mapIndexed { idx, sum -> Entry((idx + 1).toFloat(), sum) }

    // 5) Prepare labels “07-01”…“07-31”
    val labels = List(31) { i ->
        String.format("%02d-%02d", thisMonth, i + 1)
    }
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val primaryInt   = primary.toArgb()
    val tertiaryInt  = tertiary.toArgb()
    val red = Color(0xFFFF0000)

    Box(modifier = modifier.fillMaxSize()) {
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
                        spaceTop     = 15f
                    }

                    // 5a) horizontal limit line at 1,000,000
                    axisLeft.removeAllLimitLines()
                    axisLeft.addLimitLine(
                        LimitLine(1_000_000f, "월 최대")
                            .apply {
                                lineColor = red.copy(alpha=0.6f).toArgb()
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
                    position       = XAxis.XAxisPosition.BOTTOM
                    granularity    = 1f
                    labelCount     = 31
                    valueFormatter = IndexAxisValueFormatter(labels)
                }

                // 5c) Build two data-sets with gradient fill

                // Gradient generator
                fun makeGradientPrevious(ctx: Context, base: Color): Drawable {
                    val startColor = base.copy(alpha=0.1f).toArgb()
                    val endColor = base.copy(alpha=0.5f).toArgb()
                    return GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        intArrayOf(startColor, endColor)
                    )
                }
                fun makeGradientCurrent(ctx: Context, base: Color): Drawable {
                    val startColor = base.copy(alpha=0.8f).toArgb()
                    val endColor = base.copy(alpha=0.5f).toArgb()
                    return GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(startColor, endColor)
                    )
                }

                val setPrev = LineDataSet(prevEntries, "이전 달 누적").apply {
                    mode            = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawCircles(false)
                    circleRadius    = 3f
                    lineWidth       = 2f
                    setDrawValues(false)
                    color           = tertiary.copy(alpha=0.4f).toArgb()
                    circleColors    = listOf(tertiaryInt)
                    setDrawFilled(true)
                    fillDrawable    = makeGradientPrevious(chart.context, tertiary)
                }
                val setCurr = LineDataSet(currEntries, "이번 달 누적").apply {
                    mode            = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawCircles(false)
                    circleRadius    = 3f
                    lineWidth       = 2f
                    setDrawValues(false)
                    color           = primaryInt
                    circleColors    = listOf(primaryInt)
//                    setDrawFilled(true)
//                    fillDrawable    = makeGradientCurrent(chart.context, primary)
                }

                chart.data = LineData(setPrev, setCurr)
                chart.invalidate()
            }
        )
    }
}

@Composable
fun PieChartByCategory(
    viewModel: ExpenseViewModel = viewModel(),
    modifier: Modifier = Modifier
) {

    //val expenses by viewModel.allExpenses.observeAsState(emptyList())
    val expensesWithCategory by viewModel.allExpensesWithCategory.observeAsState(initial = emptyList())
    val today    = LocalDate.now()
    val year     = today.year
    val month    = today.monthValue

    val sumsByCat: List<Pair<String, Float>> = expensesWithCategory
        .map { exp ->
            Instant.ofEpochMilli(exp.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate() to exp
        }
        .filter { (date, _) ->
            val result = date.year == year && date.monthValue == month
            Log.d("pieChart","날짜: ${date} -> $result")
            result
        }
        .map { (_, exp) -> exp } // 날짜 정보 제거, expense만 남김
        .groupBy { exp ->
            exp.categoryName?.trim() ?: "미분류"
        } // 카테고리별로 그룹핑
        .mapValues { (_, expenses) ->
            expenses.sumOf { it.amount }.toFloat()
        } // 각 그룹의 금액 합계 계산
        .map { (category, totalAmount) ->
            Log.d("PieChartDebug", "카테고리: '$category', 총 금액: $totalAmount")
            category to totalAmount
        }
        .toList()


    // 5) Build PieEntry list, only non-zero categories
    val entries = remember(sumsByCat) {
        Log.d("PieChartDebug", "최종 카테고리별 합계: $sumsByCat")
        sumsByCat.mapNotNull { (cat, sum) ->
            if (sum > 0f) PieEntry(sum, cat) else null
        }.sortedByDescending { it.value }
    }

    // 6) Choose a color per category (you can customize these)
    val baseColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFF2ed573),
        Color(0xFFEDCE5C),
        Color(0xFFFF9800),
        Color(0xFFff6348)
    )

    val sliceColors = entries.mapIndexed { idx, _ ->
        baseColors.getOrNull(idx)?.copy(alpha = 0.8f)?.toArgb()
            ?: ColorTemplate.MATERIAL_COLORS[idx % ColorTemplate.MATERIAL_COLORS.size]
    }


    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PieChart(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // Show percentages
                    setUsePercentValues(true)
                    description.isEnabled = false
                    legend.isEnabled = false
//                    setDrawEntryLabels(false)
                    isDrawHoleEnabled = false
                    setTransparentCircleAlpha(0)
//                    setEntryLabelTypeface(Typeface.DEFAULT_BOLD)
                    setEntryLabelTextSize(12f)
                    setEntryLabelColor(Color.Black.toArgb())
                    animateY(500, Easing.EaseInOutQuad)
                }
            },
            update = { chart ->
                // 7) Build DataSet with category-specific colors
                val dataSet = PieDataSet(entries, "").apply {
                    colors = sliceColors
                    valueFormatter = PercentFormatter(chart)
                    valueTextSize = 12f
                    valueTextColor = Color.Black.toArgb()
                    valueTypeface = Typeface.DEFAULT_BOLD
                    sliceSpace = 2f

                    setDrawValues(true)
                    isUsingSliceColorAsValueLineColor = true
                    valueLinePart1OffsetPercentage = 75f
                    valueLinePart1Length = 0.6f
                    valueLinePart2Length = 0.8f
                    valueLineColor = Color.Black.toArgb()
                    yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                    xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                }
                chart.data = PieData(dataSet)
                chart.invalidate()
            }
        )
    }
}


@Composable
fun ExpenseStatisticsScreen(modifier: Modifier = Modifier) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    Column (
        modifier = modifier
            //Fixme
            //.background(MaterialTheme.colorScheme.secondary)
            .fillMaxSize()
            .padding(16.dp),         // outer padding around the whole stack
        verticalArrangement = Arrangement.spacedBy(12.dp) // space between charts
    ) {
        MonthNavigationHeaderfotStatistics(
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
        FirstLineChartDemo(
            modifier = Modifier
                .weight(1f)             // share remaining height equally
                .fillMaxWidth()
        )
        PieChartByCategory(
            modifier = Modifier
                .height(230.dp)         // fixed height if you prefer
                .fillMaxWidth()
        )
        SecondLineChartDemo(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
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
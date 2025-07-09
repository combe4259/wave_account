package com.example.accountbook.ui.components

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accountbook.view.ExpenseViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import com.github.mikephil.charting.components.LimitLine
import androidx.compose.runtime.key
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

@Composable
fun SecondLineChartDemo(
    modifier: Modifier = Modifier,
    headerMonth: Calendar,
    compareMonth: Calendar,
    monthlyGoal: Int,
    viewModel: ExpenseViewModel = viewModel()
) {
    // 1) Observe all expenses
    val expensesWithCategory by viewModel.allExpensesWithCategory.observeAsState(initial = emptyList())

    // 2) Compute date info
    val today = LocalDate.now()
    val thisYear  = today.year
    val thisMonth = today.monthValue
    val todayDay  = today.dayOfMonth

    val cmpYear  = compareMonth.get(Calendar.YEAR)
    val cmpMonth = compareMonth.get(Calendar.MONTH) + 1
    val cmpDays  = compareMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

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
    val cmpCum   = cumulative(cmpYear, cmpMonth, cmpDays)

    val prevEntries = cmpCum.mapIndexed { idx, sum -> Entry((idx + 1).toFloat(), sum) }
    val currEntries = currCum.mapIndexed { idx, sum -> Entry((idx + 1).toFloat(), sum) }

    val labels = List(31) { "%d/%d".format(thisMonth, it) }
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val primaryInt   = primary.toArgb()
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
                        marker = SimpleLineMarker(ctx, labels)
                        axisLeft.apply {
                            axisMinimum = 0f
                            axisMaximum = monthlyGoal * 1.1f
                            spaceTop = 15f
                        }

                        // 5a) horizontal limit line at 1,000,000
                        axisLeft.removeAllLimitLines()
                        axisLeft.addLimitLine(
                            LimitLine(monthlyGoal.toFloat(), "월 최대")
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
                    chart.axisLeft.apply {
                        removeAllLimitLines()
                        axisMaximum = monthlyGoal * 1.1f
                        addLimitLine(
                            LimitLine(monthlyGoal.toFloat(), "월 최대").apply {
                                lineColor = red.copy(alpha = 0.6f).toArgb()
                                lineWidth = 2f
                                textColor = red.toArgb()
                                textSize = 12f
                            }
                        )
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
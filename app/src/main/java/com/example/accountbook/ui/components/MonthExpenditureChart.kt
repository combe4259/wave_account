package com.example.accountbook.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accountbook.view.ExpenseViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import androidx.compose.runtime.key

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
        Entry((d).toFloat(), dailySums[d] ?: 0f)
    }
    val primary = MaterialTheme.colorScheme.primary
    val primaryInt   = primary.toArgb()
    val labels = (0..daysToShow).map { d ->
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

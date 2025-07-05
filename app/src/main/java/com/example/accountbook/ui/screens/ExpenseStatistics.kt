package com.example.accountbook.ui.screens

import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
import kotlin.random.Random

@Composable
fun FirstLineChartDemo(
    viewModel: ExpenseViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    // generate 12 points (month vs value)
    val expenses by viewModel.allExpenses.observeAsState(initial = emptyList())
    val today = LocalDate.now()  // e.g. 2025-07-04
    val year  = today.year
    val month = today.monthValue
    val days  = today.dayOfMonth  // e.g. 4

    // Convert epoch-ms → LocalDate for each expense, filter by month/year
    val monthly = expenses.map{ Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() to it.amount }
        .filter { (date, _) -> date.year == year && date.monthValue == month }

    // Group amounts by day-of-month and sum
    val dailySums: Map<Int, Float> = monthly
        .groupBy   { (date, _) -> date.dayOfMonth }
        .mapValues { (_, list) -> list.sumOf { it.second }.toFloat() }

    val entries = (1..days).map { d ->
        Entry(d.toFloat(), dailySums[d] ?: 0f)
    }
    val labels = (1..days).map { String.format("%02d-%02d", month, it) }

    val primaryInt   = MaterialTheme.colorScheme.primary.toArgb()
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
                    color = primaryInt
                    circleColors = listOf(tertiaryInt)
                }
                chart.data = LineData(dataSet)
                chart.invalidate()
            }
        )
    }
}

/**
 * 2) A second LineChart with two data-sets for comparison.
 */
@Composable
fun SecondLineChartDemo(modifier: Modifier = Modifier) {
    // same x-axis, but two random series
    val entries = remember {
        (1..12).map { Entry(it.toFloat(), Random.nextFloat() * 80f + 20f) }
    }
    val startColor = MaterialTheme.colorScheme.secondary.toArgb()
    val endColor   = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f).toArgb()
    val gradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        intArrayOf(startColor, endColor)
    )

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
                    xAxis.granularity = 1f
                    animateX(300)
                }
            },
            update = { chart ->
                val setA = LineDataSet(entries, "Series A").apply {
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    color = ColorTemplate.COLORFUL_COLORS[0]
                    setColor(convertToArgb(MainColor))
                    setDrawCircles(false)
                    lineWidth = 2f
                    setDrawFilled(true)
                    fillDrawable = gradientDrawable
                }
                chart.data = LineData(setA)
                chart.invalidate()
            }
        )
    }
}

@Composable
fun PieChartDemo(
    modifier: Modifier = Modifier
) {
    val entries = remember {
        List(5) { index ->
            PieEntry((10..100).random().toFloat(), "Item ${index + 1}")
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PieChart(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // Show percentages
                    setUsePercentValues(true)
                    // No description text
                    description.isEnabled = false
                    // No legend
                    legend.isEnabled = false
                    // Label styling
                    setEntryLabelColor(convertToArgb(MainColor))
                    setEntryLabelTextSize(12f)
                    // Animate Y-axis
                    animateY(800, Easing.EaseInOutQuad)
                }
            },
            update = { chart ->
                val dataSet = PieDataSet(entries, "").apply {
                    // Use built-in pastel/material colors
                    colors = ColorTemplate.MATERIAL_COLORS.toList()
                    valueTextSize = 14f
                    setDrawValues(false)
                }
                chart.data = PieData(dataSet)
                chart.invalidate() // Refresh the chart
            }
        )
    }
}

@Composable
fun ExpenseStatisticsScreen(modifier: Modifier = Modifier) {
    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),         // outer padding around the whole stack
        verticalArrangement = Arrangement.spacedBy(12.dp) // space between charts
    ) {
        FirstLineChartDemo(
            modifier = Modifier
                .weight(1f)             // share remaining height equally
                .fillMaxWidth()
        )
        SecondLineChartDemo(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        PieChartDemo(
            modifier = Modifier
                .height(200.dp)         // fixed height if you prefer
                .fillMaxWidth()
        )
    }
}

fun convertToArgb(color: Color): Int = color.toArgb()



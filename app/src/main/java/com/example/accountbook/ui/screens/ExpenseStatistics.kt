package com.example.accountbook.ui.screens

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
import com.example.accountbook.ui.theme.MainColor
import com.example.accountbook.view.ExpenseViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.Calendar
import kotlin.random.Random
import com.example.accountbook.ui.screens.MonthNavigationHeader
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FirstLineChartDemo(modifier: Modifier = Modifier) {
    // generate 12 points (month vs value)
    val entries = remember {
        (1..12).map { month ->
            Entry(month.toFloat(), Random.nextFloat() * 100f)
        }
    }

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
                    xAxis.granularity = 1f
                    animateX(300)

                }
            },
            update = { chart ->
                val dataSet = LineDataSet(entries, "Monthly Data").apply {
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawCircles(true)
                    lineWidth = 2f
                    circleRadius = 4f
                    setDrawValues(false)
                    colors = listOf(ColorTemplate.MATERIAL_COLORS[0])
                    circleColors = listOf(ColorTemplate.MATERIAL_COLORS[1])
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
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    Column (
        modifier = modifier
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
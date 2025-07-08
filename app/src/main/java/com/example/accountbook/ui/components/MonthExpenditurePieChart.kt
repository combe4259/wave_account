package com.example.accountbook.ui.components

import android.graphics.Typeface
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar
import com.github.mikephil.charting.formatter.PercentFormatter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.key
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import kotlin.math.min

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
        Spacer(modifier = Modifier.height(30.dp))

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
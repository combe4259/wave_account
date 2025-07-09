package com.example.accountbook.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.accountbook.dto.ExpenseWithCategory
import java.time.YearMonth
import kotlin.math.roundToInt

@Composable
fun BottomStats(
    expenses: List<ExpenseWithCategory>,      // ← your left-joined entity
    sumsThisMonth: List<Pair<String, Float>>, // list you already built for pie
    sliceColors: List<Int>,                   // same order as sumsThisMonth
    monthlyLimit: Float = 1_000_000f          // 목표 지출 한도
) {
    val today  = LocalDate.now()
    val year = today.year
    val month = today.monthValue
    val isCurr = (year == today.year && month == today.monthValue)
    val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
    val daysLeft    = if (isCurr) daysInMonth - today.dayOfMonth else 0
    val spentSoFar = remember(expenses, year, month) {
        expenses
            .filter { e ->
                Instant.ofEpochMilli(e.date)
                    .atZone(ZoneId.systemDefault())
                    .let { d -> d.year == year && d.monthValue == month }
            }
            .sumOf { it.amount.toDouble() }      // Double to avoid overflow
            .toFloat()
    }

    val leftOver   = (monthlyLimit - spentSoFar).coerceAtLeast(0f)
    val prevMonthDate = today.minusMonths(1)
    val prevYear = prevMonthDate.year
    val prevMonth = prevMonthDate.monthValue

    val sumsPrev = remember(expenses, prevYear, prevMonth) {
        expenses
            .filter { exp ->
                val d = Instant.ofEpochMilli(exp.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                d.year == prevYear && d.monthValue == prevMonth
            }
            .groupBy { exp -> exp.categoryName?.trim().orEmpty().ifEmpty { "미분류" } }
            .mapValues { (_, list) -> list.sumOf { it.amount }.toFloat() }
            .toList()
            .sortedByDescending { it.second }
    }
    val pctBase = if (sumsPrev.isNotEmpty()) sumsPrev else sumsThisMonth
    val pctTotal = pctBase.sumOf { it.second.toDouble() }.toFloat()

    val colourMap = sumsThisMonth
        .mapIndexed { idx, (cat, _) -> cat to sliceColors[idx % sliceColors.size] }
        .toMap()

    /* ─────────────── 권장액 계산 ─────────────── */
    data class Row(val cat: String, val pct: Float, val perDay: Int, val color: Int)

    val rows = pctBase.mapIndexed { idx, (cat, amt) ->
        val share = if (pctTotal > 0) amt / pctTotal else 0f
        val totalRec = leftOver * share
        val rawPerDay   = if (daysLeft > 0) (totalRec / daysLeft).roundToInt() else 0
        val perDay = kotlin.math.ceil((rawPerDay / 100).toDouble()).toInt() * 100
        val colour = if (idx < 5) sliceColors[idx] else sliceColors[5]
        Row(cat, share, perDay, colour)
    }.sortedByDescending { it.pct }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Text(
            text = "남은 예산: ${"%,d".format(leftOver.toInt())}원",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        if (sumsPrev.isEmpty()) {
            Text(
                "소비 추천을 위한 데이터가 부족합니다.",
                color = Color.Gray,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                textAlign = TextAlign.Center
            )
            return   // nothing else to show
        } else {
            Text(
                text = "${daysLeft.coerceAtLeast(0)}일 동안 카테고리별 1일 권장 지출",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(8.dp))
            rows.forEach { r ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 퍼센트 박스
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(26.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(r.color)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(r.pct * 100).roundToInt()}%",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.width(8.dp))
                    Text(r.cat, modifier = Modifier.weight(1f))
                    Text(
                        text = "%,d원".format(r.perDay),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ChartHeaderWithMonthToggle(
    title: String,
    selectedMonth: Calendar,
    earliestMonth: Calendar,
    onMonthChange: (Calendar) -> Unit
) {
    val thisMonth = remember {
        Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        val leftEnabled = selectedMonth.after(earliestMonth)
        IconButton(onClick = {
            onMonthChange((selectedMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) })
            },
            enabled = leftEnabled) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "이전 달",
                tint = if (leftEnabled) Color.Black else Color.LightGray)
        }

        // ▶ 다음 (이번 달 이전까지만)
        val limit = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1); add(Calendar.MONTH, -1)
        }
        val next = (selectedMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }

        val label = "${selectedMonth.get(Calendar.MONTH) + 1}월"
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 0.dp)
        )
        IconButton(
            onClick = { onMonthChange(next) },
            enabled = next.before(limit)      // 미래·이번달은 비활성
        ) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "다음 달",
                tint = if (next.before(limit)) Color.Black else Color.LightGray
            )
        }
    }
}

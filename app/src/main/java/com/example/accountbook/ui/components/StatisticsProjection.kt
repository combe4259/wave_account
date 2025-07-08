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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import com.example.accountbook.dto.ExpenseWithCategory
import kotlin.math.roundToInt


@Composable
fun BottomStats(
    currentMonth: Calendar,
    expenses: List<ExpenseWithCategory>,      // ← your left-joined entity
    sumsThisMonth: List<Pair<String, Float>>, // list you already built for pie
    sliceColors: List<Int>,                   // same order as sumsThisMonth
    monthlyLimit: Float = 1_000_000f          // 목표 지출 한도
) {
    /* ─────────────── 날짜 정보 ─────────────── */
    val year   = currentMonth.get(Calendar.YEAR)
    val month  = currentMonth.get(Calendar.MONTH) + 1
    val today  = LocalDate.now()
    val isCurr = (year == today.year && month == today.monthValue)
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val daysLeft    = if (isCurr) daysInMonth - today.dayOfMonth else 0

    /* ─────────────── 이번 달 지출 & 잔액 ─────────────── */
    val spentSoFar = sumsThisMonth.sumOf { it.second.toDouble() }.toFloat()
    val leftOver   = (monthlyLimit - spentSoFar).coerceAtLeast(0f)

    /* ─────────────── 지난달 % (없으면 이번달 % 사용) ─────────────── */
    val prevCal = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
    val prevYear  = prevCal.get(Calendar.YEAR)
    val prevMonth = prevCal.get(Calendar.MONTH) + 1

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

    /* ─────────────── 권장액 계산 ─────────────── */
    data class Row(val cat: String, val pct: Float, val perDay: Int, val color: Int)

    val rows = pctBase.mapIndexed { idx, (cat, amt) ->
        val share = if (pctTotal > 0) amt / pctTotal else 0f
        val totalRec = leftOver * share
        val rawPerDay   = if (daysLeft > 0) (totalRec / daysLeft).roundToInt() else 0
        val perDay = kotlin.math.ceil((rawPerDay / 100).toDouble()).toInt() * 100
        Row(cat, share, perDay, sliceColors[idx % sliceColors.size])
    }.sortedByDescending { it.pct }

    /* ─────────────── UI ─────────────── */
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        /* 1) 남은 금액 */
        Text(
            text = "남은 지출 가능 금액: ${"%,d".format(leftOver.toInt())}원",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        /* 2) 헤더 */
        Text(
            text = "남은 ${daysLeft.coerceAtLeast(0)}일 동안 카테고리별 1일 권장 지출",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(8.dp))

        /* 3) 행들 */
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
                    text = "%,d원/일".format(r.perDay),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

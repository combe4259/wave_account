package com.example.accountbook.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope


@Composable
fun LiquidFill(
    progress: Float,                  // 0-1   (e.g. 0.5f for 50 %)
    modifier: Modifier = Modifier,
    waveColor: Color = MaterialTheme.colorScheme.tertiary,
    amplitudeDp: Dp = 3.dp,           // wave height
    speed: Int = 3500                 // ms for one full cycle
) {
    val amplitude = with(LocalDensity.current) { amplitudeDp.toPx() }
    val phase by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = speed, easing = LinearEasing)
        )
    )
    val phase2 by rememberInfiniteTransition().animateFloat(
        0f, (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(speed * 2, easing = LinearEasing)) // 5배 느리게
    )

    //넘침 효과
    val overflowPhase by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = speed / 2, easing = LinearEasing)
        )
    )


    Canvas(modifier = modifier.clipToBounds()) {
        val w = size.width
        val h = size.height
        val isOverflow = progress > 1f

        if (isOverflow) {
            // 넘침 효과 - 전체를 채우고 추가 효과
            drawOverflowEffect(w, h, amplitude, overflowPhase, waveColor)
        } else {
            // 일반 파도 효과
            val level = h * (1f - progress.coerceIn(0f, 1f))
            drawNormalWaves(w, h, level, amplitude, phase, phase2, waveColor)
        }
    }
}



//일반 파도
private fun DrawScope.drawNormalWaves(
    w: Float,
    h: Float,
    level: Float,
    amplitude: Float,
    phase: Float,
    phase2: Float,
    waveColor: Color
) {
    // 첫 번째 파도
    val path = Path().apply {
        moveTo(0f, h)
        lineTo(0f, level)


        val step = 4
        for (x in 0..w.toInt() step step) {
            val normalizedX = x / w
            val y = level + amplitude * sin(normalizedX * 2 * PI + phase)
            lineTo(x.toFloat(), y.toFloat())
        }


        val lastY = level + amplitude * sin(2 * PI + phase)
        lineTo(w, lastY.toFloat())
        lineTo(w, h)
        close()
    }
    drawPath(path, waveColor.copy(alpha = 0.5f))

    // 두 번째 파도
    val bgPath = Path().apply {
        moveTo(0f, h)
        lineTo(0f, level)

        val step = 4
        for (x in 0..w.toInt() step step) {
            val normalizedX = x / w
            val y = level + amplitude * 0.6f * sin(normalizedX * 2 * PI + phase2 + PI / 4)
            lineTo(x.toFloat(), y.toFloat())
        }

        val lastY = level + amplitude * 0.6f * sin(2 * PI + phase2 + PI / 4)
        lineTo(w, lastY.toFloat())
        lineTo(w, h)
        close()
    }
    drawPath(bgPath, waveColor.copy(alpha = 0.3f))
}

//TODO ????좀이상함
//넘침 효과
private fun DrawScope.drawOverflowEffect(
    w: Float,
    h: Float,
    amplitude: Float,
    overflowPhase: Float,
    waveColor: Color
) {
    // 전체 배경을 채움
    drawRect(
        color = waveColor.copy(alpha = 0.7f),
        size = androidx.compose.ui.geometry.Size(w, h)
    )

    // 넘치는 파도들 (여러 개)
    for (i in 1..3) {
        val path = Path().apply {
            moveTo(0f, 0f)

            val step = 3
            for (x in 0..w.toInt() step step) {
                val normalizedX = x / w
                val waveHeight = amplitude * (1.5f + i * 0.3f)
                val phaseOffset = overflowPhase + (i * PI / 3)
                val y = waveHeight * sin(normalizedX * 3 * PI + phaseOffset) - (i * 5f)
                lineTo(x.toFloat(), y.toFloat())
            }

            val lastY = amplitude * (1.5f + 3 * 0.3f) * sin(3 * PI + overflowPhase + (3 * PI / 3)) - (3 * 5f)
            lineTo(w, lastY.toFloat())
            lineTo(w, 0f)
            close()
        }

        drawPath(
            path = path,
            color = waveColor.copy(alpha = 0.4f - i * 0.1f)
        )
    }

    drawDroplets(w, h, overflowPhase, waveColor)

}
//TODO
//물방울
private fun DrawScope.drawDroplets(
    w: Float,
    h: Float,
    phase: Float,
    waveColor: Color
) {
    val dropletCount = 12
    for (i in 0 until dropletCount) {
        // 가로 위치 랜덤 분산
        val baseX = (w / dropletCount) * i + (w / dropletCount / 2)
        val offsetX = sin((phase + i * 0.7).toDouble()).toFloat() * (w * 0.1f)
        val x = baseX + offsetX

        // 아래에서 위로 올라오는 애니메이션
        val animationCycle = (phase + i * PI / 3) % (2 * PI)
        val normalizedTime = (animationCycle / (2 * PI)).toFloat()

        // h에서 꼭대기(0)로 올라감
        val baseY = h - (normalizedTime * h * 1.2f)
        val floatingY = sin((phase * 2 + i).toDouble()).toFloat() * 5f
        val y = (baseY + floatingY).coerceIn(-h * 0.2f, h)
        //크기 약간씩 커짐
        val sizeMultiplier = 1f + (1f - normalizedTime) * 0.6f
        val radius = (2.5f + sin((phase + i * 1.3).toDouble()).toFloat() * 1.2f) * sizeMultiplier

        // 올라갈수록 투명해짐
        val alpha = (0.4f + (1f - normalizedTime) * 0.4f).coerceIn(0f, 0.8f)

        // 물방울 그리기
        drawCircle(
            color = waveColor.copy(alpha = alpha),
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(x, y)
        )

        // 작은 기포
        if (i % 3 == 0) {
            val smallBubbleY = y - radius * 2
            val smallRadius = radius * 0.4f
            drawCircle(
                color = waveColor.copy(alpha = alpha * 0.6f),
                radius = smallRadius,
                center = androidx.compose.ui.geometry.Offset(x + radius * 0.8f, smallBubbleY)
            )
        }
    }
}
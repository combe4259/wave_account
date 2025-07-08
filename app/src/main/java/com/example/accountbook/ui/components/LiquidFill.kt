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

    Canvas(modifier = modifier.clipToBounds()) {
        val w = size.width
        val h = size.height
        val level = h * (1f - progress.coerceIn(0f, 1f))

        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, h)
            lineTo(0f, level)
            for (x in 0..w.toInt() step 8) {
                val y = level + amplitude * sin((x / w) * 2 * PI + phase)
                lineTo(x.toFloat(), y.toFloat())
            }
            lineTo(w, h)
            close()
        }
        drawPath(path, waveColor.copy(alpha=0.5f))

        val bgPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, h)
            lineTo(0f, level)
            for (x in 0..w.toInt() step 8) {
                val y = level + amplitude * 0.6f *
                        sin((x / w) * 2 * PI + phase2 + PI / 4)
                lineTo(x.toFloat(), y.toFloat())
            }
            lineTo(w, h)
            close()
        }
        drawPath(bgPath, waveColor.copy(alpha = 0.3f))
    }
}

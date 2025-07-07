// TextMarker.kt
package com.example.accountbook.ui.charts

import android.content.Context
import android.graphics.*
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.util.Locale
import kotlin.math.sqrt

class TextMarker(
    context: Context,
    private val textSizeSp: Float = 12f,
    private val bgColor: Int = Color.parseColor("#CC000000"),
    private val textColor: Int = Color.WHITE,
    private val total: Float? = null,
    private val pieCenter: MPPointF,
    private val pieRadius: Float
) : IMarker {

    private var text: String = ""
    private val dm = context.resources.displayMetrics
    private val density = dm.density
    private val scaledDensity = dm.scaledDensity

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textAlign = Paint.Align.CENTER
        textSize = textSizeSp * scaledDensity
    }
    private val paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bgColor
    }
    private val padding = 8f * density
    private val cornerR = 6f * density
    private val extraOffset = 8f * density   // how far past the circle

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        text = when {
            e is PieEntry && total != null -> {
                val pct = e.value / total * 100f
                String.format(Locale.getDefault(), "%s: %.1f%%", e.label, pct)
            }
            e is PieEntry -> "${e.label}: ${e.value}"
            else -> {
                val day = (e?.x?.toInt() ?: 0) + 1
                val amt = (e?.y?.toInt() ?: 0)
                String.format(Locale.getDefault(), "%02d일: %,d원", day, amt)
            }
        }
    }

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        if (text.isEmpty()) return

        // 1) measure text
        val textW = paintText.measureText(text)
        val fm    = paintText.fontMetrics
        val textH = fm.descent - fm.ascent

        val boxW = textW + padding * 2
        val boxH = textH + padding * 2

        // 2) find unit vector from center → touch point
        val dx = posX - pieCenter.x
        val dy = posY - pieCenter.y
        val dist = sqrt(dx*dx + dy*dy)
        val ux = dx / dist
        val uy = dy / dist

        // 3) target bubble center = circle edge + extraOffset
        val cx = pieCenter.x + ux * (pieRadius + extraOffset)
        val cy = pieCenter.y + uy * (pieRadius + extraOffset)

        // 4) rectangle to draw
        val left = cx - boxW/2
        val top  = cy - boxH/2
        val rect = RectF(left, top, left + boxW, top + boxH)

        // 5) draw
        canvas.drawRoundRect(rect, cornerR, cornerR, paintBg)
        val tx = rect.centerX()
        val ty = rect.centerY() - (fm.ascent + fm.descent)/2
        canvas.drawText(text, tx, ty, paintText)
    }

    override fun getOffset(): MPPointF = MPPointF(0f, 0f)
    override fun getOffsetForDrawingAtPoint(x: Float, y: Float): MPPointF = MPPointF(0f, 0f)
}

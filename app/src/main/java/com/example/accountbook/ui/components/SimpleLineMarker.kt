package com.example.accountbook.ui.components

import android.content.Context
import android.graphics.*
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.util.*

class SimpleLineMarker(
    context: Context,
    private val labels: List<String>          // “07/01”, “07/02”, …
) : IMarker {

    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC000000")
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 14f * context.resources.displayMetrics.scaledDensity
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    private var text = ""
    private val padding = 8 * context.resources.displayMetrics.density
    private val corner = 6 * context.resources.displayMetrics.density

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e ?: return
        val dayIdx  = e.x.toInt()                 // 0-based
        val label   = labels.getOrNull(dayIdx) ?: (dayIdx + 1).toString()
        val amount  = e.y.toInt()
        text = "$label: %,d원".format(Locale.getDefault(), amount)
    }

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        if (text.isEmpty()) return
        val w = textPaint.measureText(text) + padding * 2
        val fm = textPaint.fontMetrics
        val h = fm.descent - fm.ascent + padding * 2

        // draw bubble centered slightly above the point
        val left = posX - w / 2
        val top  = posY - h - 16f   // 16 px gap above circle
        val rect = RectF(left, top, left + w, top + h)
        canvas.drawRoundRect(rect, corner, corner, bubblePaint)
        val tx = rect.centerX()
        val ty = rect.centerY() - (fm.ascent + fm.descent) / 2
        canvas.drawText(text, tx, ty, textPaint)
    }

    override fun getOffset(): MPPointF = MPPointF(0f, 0f)
    override fun getOffsetForDrawingAtPoint(x: Float, y: Float): MPPointF = MPPointF(0f, 0f)
}

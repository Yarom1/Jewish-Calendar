package com.yarom.jewishcalendar.domain.print

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

/**
 * Draws one calendar page's content (a week, a day, or a month grid) into an arbitrary [rect] on
 * a [Canvas] - plain android.graphics drawing, not a Compose snapshot, so it's reliable to render
 * off the UI thread from a PrintDocumentAdapter and to tile N copies per physical sheet.
 *
 * Text is right-aligned at the row's own right edge, which is enough to read correctly for
 * Hebrew (the platform's own bidi/shaping already renders the glyphs correctly - only the block
 * alignment needs to be handled here, since this is a simple label/value table, not mixed prose).
 */
object CalendarPrintRenderer {

    private const val PADDING = 10f
    private const val TITLE_SIZE = 16f
    private const val HEADER_SIZE = 12f
    private const val NOTE_SIZE = 9f
    private const val LINE_SIZE = 8f

    private fun textPaint(size: Float, bold: Boolean = false, color: Int = Color.BLACK) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        this.color = color
        textAlign = Paint.Align.RIGHT
    }

    private fun linePaint(color: Int = Color.LTGRAY, width: Float = 1f) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        strokeWidth = width
        style = Paint.Style.STROKE
    }

    fun render(canvas: Canvas, rect: RectF, content: PrintContent) {
        canvas.drawRect(rect, linePaint(Color.DKGRAY, 1.5f))
        when (content) {
            is PrintContent.Week -> renderWeek(canvas, rect, content.content)
            is PrintContent.Day -> renderDay(canvas, rect, content.content)
            is PrintContent.Month -> renderMonth(canvas, rect, content.content)
        }
    }

    private fun renderWeek(canvas: Canvas, rect: RectF, content: PrintWeekContent) {
        val titlePaint = textPaint(TITLE_SIZE * scaleFor(rect), bold = true)
        val right = rect.right - PADDING
        var y = rect.top + PADDING + titlePaint.textSize
        canvas.drawText(content.title, right, y, titlePaint)
        y += titlePaint.textSize * 0.6f

        val dayHeight = (rect.height() - (y - rect.top) - PADDING * 2) / content.days.size.coerceAtLeast(1)
        for (day in content.days) {
            val dayTop = y
            y = drawDayBlock(canvas, day, right, rect.left + PADDING, y, scaleFor(rect))
            canvas.drawLine(rect.left + PADDING, dayTop + dayHeight - 2f, rect.right - PADDING, dayTop + dayHeight - 2f, linePaint())
            y = dayTop + dayHeight
        }

        val notePaint = textPaint(NOTE_SIZE * scaleFor(rect))
        val boldNotePaint = textPaint(NOTE_SIZE * scaleFor(rect), bold = true)
        var bottomY = rect.bottom - PADDING
        canvas.drawText("צאת השבת: ${content.havdalah}   |   הדלקת נרות: ${content.candleLighting}", right, bottomY, boldNotePaint)
        content.haftarahLine?.let {
            bottomY -= notePaint.textSize * 1.4f
            canvas.drawText(it, right, bottomY, notePaint)
        }
        content.parashaLine?.let {
            bottomY -= notePaint.textSize * 1.4f
            canvas.drawText(it, right, bottomY, boldNotePaint)
        }
    }

    private fun renderDay(canvas: Canvas, rect: RectF, content: PrintDayContent) {
        val titlePaint = textPaint(TITLE_SIZE * scaleFor(rect), bold = true)
        val right = rect.right - PADDING
        var y = rect.top + PADDING + titlePaint.textSize
        canvas.drawText(content.title, right, y, titlePaint)
        y += titlePaint.textSize * 0.8f

        y = drawDayBlock(canvas, content.day, right, rect.left + PADDING, y, scaleFor(rect))

        if (content.studyLines.isNotEmpty()) {
            y += LINE_SIZE * scaleFor(rect)
            val headerPaint = textPaint(HEADER_SIZE * scaleFor(rect), bold = true)
            canvas.drawText("לימוד יומי", right, y, headerPaint)
            y += headerPaint.textSize * 1.3f
            y = drawLines(canvas, content.studyLines, right, y, LINE_SIZE * scaleFor(rect))
        }
    }

    private fun renderMonth(canvas: Canvas, rect: RectF, content: PrintMonthContent) {
        val scale = scaleFor(rect)
        val titlePaint = textPaint(TITLE_SIZE * scale, bold = true)
        val right = rect.right - PADDING
        var y = rect.top + PADDING + titlePaint.textSize
        canvas.drawText(content.title, right, y, titlePaint)
        y += titlePaint.textSize * 0.6f

        val gridTop = y
        val gridHeight = rect.bottom - PADDING - gridTop
        val rows = (content.cells.size / 7).coerceAtLeast(1)
        val colWidth = (rect.width() - PADDING * 2) / 7f
        val rowHeight = gridHeight / (rows + 1) // +1 for the weekday-label header row

        val headerPaint = textPaint(HEADER_SIZE * scale, bold = true)
        for ((index, label) in content.weekdayLabels.withIndex()) {
            val colRight = rect.right - PADDING - index * colWidth
            canvas.drawText(label, colRight - colWidth / 2f + headerPaint.textSize / 2f, gridTop + rowHeight * 0.7f, headerPaint)
        }

        val dayNumPaint = textPaint(HEADER_SIZE * scale)
        val hebrewDayPaint = textPaint(NOTE_SIZE * scale, color = Color.DKGRAY)
        for ((cellIndex, cell) in content.cells.withIndex()) {
            val row = cellIndex / 7
            val col = cellIndex % 7
            val cellRight = rect.right - PADDING - col * colWidth
            val cellTop = gridTop + rowHeight * (row + 1)
            canvas.drawRect(cellRight - colWidth, cellTop, cellRight, cellTop + rowHeight, linePaint())
            val alpha = if (cell.inMonth) 255 else 120
            dayNumPaint.alpha = alpha
            hebrewDayPaint.alpha = alpha
            val centerX = cellRight - colWidth / 2f
            canvas.drawText(cell.dayNumber, centerX + dayNumPaint.textSize / 2f, cellTop + rowHeight * 0.45f, dayNumPaint)
            canvas.drawText(cell.hebrewDay, centerX + hebrewDayPaint.textSize / 2f, cellTop + rowHeight * 0.75f, hebrewDayPaint)
            if (cell.hasEvents) {
                canvas.drawCircle(cellRight - 6f, cellTop + 6f, 2.5f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.RED })
            }
            if (cell.isToday) {
                canvas.drawRect(cellRight - colWidth + 1f, cellTop + 1f, cellRight - 1f, cellTop + rowHeight - 1f, linePaint(Color.BLUE, 2f))
            }
        }
    }

    /** Draws [day]'s date/note header plus its lines, returning the Y position just below it. */
    private fun drawDayBlock(canvas: Canvas, day: PrintDayBlock, right: Float, left: Float, startY: Float, scale: Float): Float {
        var y = startY
        val headerPaint = textPaint(HEADER_SIZE * scale, bold = true)
        canvas.drawText("${day.dateLabel}   ${day.hebrewLabel}", right, y, headerPaint)
        y += headerPaint.textSize * 1.3f
        day.noteLine?.let {
            val notePaint = textPaint(NOTE_SIZE * scale, color = Color.RED)
            canvas.drawText(it, right, y, notePaint)
            y += notePaint.textSize * 1.3f
        }
        y = drawLines(canvas, day.lines, right, y, LINE_SIZE * scale)
        return y
    }

    private fun drawLines(canvas: Canvas, lines: List<PrintLine>, right: Float, startY: Float, textSize: Float): Float {
        var y = startY
        val labelPaint = textPaint(textSize)
        val valuePaint = textPaint(textSize, bold = true)
        for (line in lines) {
            canvas.drawText(line.value, right, y, valuePaint)
            val valueWidth = valuePaint.measureText(line.value)
            canvas.drawText(line.label, right - valueWidth - 12f, y, labelPaint)
            y += textSize * 1.35f
        }
        return y
    }

    /** Text/line sizes are authored for a full-page rect - shrink proportionally for a tiled
     * (2/4/8-up) sub-rect, since [PADDING] and font sizes are otherwise fixed point sizes. */
    private fun scaleFor(rect: RectF): Float = (rect.width() / 500f).coerceIn(0.4f, 1.2f)
}

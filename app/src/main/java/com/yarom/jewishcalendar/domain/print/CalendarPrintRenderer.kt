package com.yarom.jewishcalendar.domain.print

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

/**
 * Draws one calendar page's content (a week, a day, or a month grid) into an arbitrary [rect] on
 * a [Canvas] - plain android.graphics drawing, not a Compose snapshot, so it's reliable to render
 * off the UI thread from a PrintDocumentAdapter and to tile N copies per physical sheet.
 *
 * Colors and typeface mirror the app's own "classic printed wall-calendar" theme
 * (ui/theme/Theme.kt's light scheme: parchment page, deep teal ink, burgundy/brass-gold accents
 * for Shabbat/Yom Tov, serif type) so the printed page looks like the calendar itself rather than
 * a bare data table (spec follow-up: the previous plain-black-Paint table read as "empty").
 *
 * Headers/titles are right-aligned at the row's right edge; each zman's value+label cluster is
 * left-aligned at the row's left edge (mirroring the app's own on-screen ZmanLine under its RTL
 * layout direction). The platform's own bidi/shaping already renders the Hebrew glyphs correctly
 * - only the block alignment needs to be handled here, since this is a label/value table, not
 * mixed prose.
 */
object CalendarPrintRenderer {

    private const val PADDING = 10f
    private const val TITLE_SIZE = 17f
    private const val HEADER_SIZE = 12f
    private const val NOTE_SIZE = 9f
    private const val LINE_SIZE = 8.5f
    private const val CARD_INSET = 6f
    private const val CARD_RADIUS = 6f
    private const val ACCENT_BAR_WIDTH = 5f

    // Same literals as the light color scheme in ui/theme/Theme.kt - a printed page is always
    // read against paper, so this mirrors the light ("classic parchment") variant regardless of
    // the device's own theme setting.
    private const val PARCHMENT = 0xFFFBF3E1.toInt()
    private const val SURFACE = 0xFFFFFCF5.toInt()
    private const val INK = 0xFF2B2015.toInt()
    private const val INK_MUTED = 0xFF6B5C46.toInt()
    private const val TEAL = 0xFF184A47.toInt()
    private const val GOLD = 0xFFC9A227.toInt()
    private const val BURGUNDY = 0xFF7A1F2B.toInt()
    private const val OUTLINE = 0xFFB59A5C.toInt()
    private const val EVENT_INDIGO = 0xFF33448F.toInt()

    private val serifRegular = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val serifBold = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    private fun textPaint(size: Float, bold: Boolean = false, color: Int = INK) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        typeface = if (bold) serifBold else serifRegular
        this.color = color
        textAlign = Paint.Align.RIGHT
    }

    private fun centerTextPaint(size: Float, bold: Boolean = false, color: Int = INK) =
        textPaint(size, bold, color).apply { textAlign = Paint.Align.CENTER }

    private fun linePaint(color: Int = OUTLINE, width: Float = 1f) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        strokeWidth = width
        style = Paint.Style.STROKE
    }

    private fun fillPaint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    fun render(canvas: Canvas, rect: RectF, content: PrintContent) {
        canvas.drawRect(rect, fillPaint(PARCHMENT))
        canvas.drawRect(rect, linePaint(OUTLINE, 1.5f))
        when (content) {
            is PrintContent.Week -> renderWeek(canvas, rect, content.content)
            is PrintContent.Day -> renderDay(canvas, rect, content.content)
            is PrintContent.Month -> renderMonth(canvas, rect, content.content)
        }
    }

    private fun renderWeek(canvas: Canvas, rect: RectF, content: PrintWeekContent) {
        val scale = fitScaleForWeek(rect, content)
        val right = rect.right - PADDING
        val left = rect.left + PADDING

        val titlePaint = textPaint(TITLE_SIZE * scale, bold = true, color = TEAL)
        var y = rect.top + PADDING + titlePaint.textSize
        canvas.drawText(content.title, right, y, titlePaint)
        y += titlePaint.textSize * 0.35f
        canvas.drawLine(left, y, right, y, linePaint(GOLD, 1.5f * scale))
        y += 8f * scale

        // Each day's block advances the cursor by exactly the height it actually drew (not an
        // even 1/7 split), so a day with extra lines (candle lighting, more visible zmanim)
        // never overlaps the next one's header (spec follow-up: this was the "doesn't look good"
        // overlap bug in the previous version).
        for (day in content.days) {
            y = drawDayCard(canvas, day, right, left, y, scale)
            y += 6f * scale
        }

        if (content.weekZmanimLines.isNotEmpty()) {
            y = drawSectionCard(canvas, "זמני השבוע", content.weekZmanimLines, right, left, y, scale)
            y += 6f * scale
        }

        val studyLines = listOfNotNull(
            content.dafYomiBavli?.let { PrintLine("בבלי", it) },
            content.dafYomiYerushalmi?.let { PrintLine("ירושלמי", it) },
        )
        if (studyLines.isNotEmpty()) {
            y = drawSectionCard(canvas, "לימוד יומי", studyLines, right, left, y, scale)
            y += 6f * scale
        }

        val notePaint = textPaint(NOTE_SIZE * scale, color = INK)
        val boldNotePaint = textPaint(NOTE_SIZE * scale, bold = true, color = BURGUNDY)
        content.parashaLine?.let {
            canvas.drawText(it, right, y, boldNotePaint)
            y += notePaint.textSize * 1.4f
        }
        content.haftarahLine?.let {
            canvas.drawText(it, right, y, notePaint)
            y += notePaint.textSize * 1.4f
        }
        y += 3f * scale
        drawSummaryBand(canvas, "צאת השבת: ${content.havdalah}   |   הדלקת נרות: ${content.candleLighting}", right, left, y, scale)
    }

    private fun renderDay(canvas: Canvas, rect: RectF, content: PrintDayContent) {
        val scale = fitScaleForDay(rect, content)
        val right = rect.right - PADDING
        val left = rect.left + PADDING

        val titlePaint = textPaint(TITLE_SIZE * scale, bold = true, color = TEAL)
        var y = rect.top + PADDING + titlePaint.textSize
        canvas.drawText(content.title, right, y, titlePaint)
        y += titlePaint.textSize * 0.35f
        canvas.drawLine(left, y, right, y, linePaint(GOLD, 1.5f * scale))
        y += 8f * scale

        y = drawDayCard(canvas, content.day, right, left, y, scale)

        if (content.studyLines.isNotEmpty()) {
            y += 6f * scale
            drawSectionCard(canvas, "לימוד יומי", content.studyLines, right, left, y, scale)
        }
    }

    /** Total height [content] needs to draw at scale=1 - every term below is directly
     * proportional to scale, so [fitScaleForWeek] can solve for the exact fitting scale in one
     * division rather than measuring iteratively. */
    private fun estimateWeekUnitHeight(content: PrintWeekContent): Float {
        var total = TITLE_SIZE * 1.35f + 8f
        for (day in content.days) {
            total += dayCardHeight(day) + 6f
        }
        if (content.weekZmanimLines.isNotEmpty()) {
            total += sectionCardHeight(content.weekZmanimLines.size) + 6f
        }
        val studyCount = (if (content.dafYomiBavli != null) 1 else 0) + (if (content.dafYomiYerushalmi != null) 1 else 0)
        if (studyCount > 0) {
            total += sectionCardHeight(studyCount) + 6f
        }
        val summaryLines = (if (content.parashaLine != null) 1 else 0) + (if (content.haftarahLine != null) 1 else 0)
        total += summaryLines * NOTE_SIZE * 1.4f + 3f + summaryBandHeight()
        return total
    }

    private fun fitScaleForWeek(rect: RectF, content: PrintWeekContent): Float {
        val available = rect.height() - PADDING * 2
        val unitHeight = estimateWeekUnitHeight(content)
        val maxScale = scaleFor(rect)
        if (unitHeight <= 0f) return maxScale
        return (available / unitHeight).coerceIn(0.25f, maxScale)
    }

    private fun estimateDayUnitHeight(content: PrintDayContent): Float {
        var total = TITLE_SIZE * 1.35f + 8f + dayCardHeight(content.day)
        if (content.studyLines.isNotEmpty()) {
            total += 6f + sectionCardHeight(content.studyLines.size)
        }
        return total
    }

    private fun fitScaleForDay(rect: RectF, content: PrintDayContent): Float {
        val available = rect.height() - PADDING * 2
        val unitHeight = estimateDayUnitHeight(content)
        val maxScale = scaleFor(rect)
        if (unitHeight <= 0f) return maxScale
        return (available / unitHeight).coerceIn(0.25f, maxScale)
    }

    /** Card height at scale=1 (mirrors [drawDayCard]'s own layout math exactly). */
    private fun dayCardHeight(day: PrintDayBlock): Float {
        var total = CARD_INSET * 2 + HEADER_SIZE * 1.3f
        if (day.noteLine != null) total += NOTE_SIZE * 1.3f
        total += day.lines.size * LINE_SIZE * 1.5f
        return total
    }

    private fun sectionCardHeight(lineCount: Int): Float =
        CARD_INSET * 2 + HEADER_SIZE * 1.3f + lineCount * LINE_SIZE * 1.5f

    private fun summaryBandHeight(): Float = NOTE_SIZE * 1.35f + CARD_INSET * 2

    /**
     * Draws one day as a rounded "card" - surface fill, thin outline (or a thicker teal outline
     * on today), and a burgundy/brass-gold vertical accent bar on the trailing edge echoing the
     * colored weekday tag strip on DayCell in the on-screen calendar grid.
     */
    private fun drawDayCard(canvas: Canvas, day: PrintDayBlock, right: Float, left: Float, startY: Float, scale: Float): Float {
        val cardTop = startY
        val cardHeight = dayCardHeight(day) * scale
        val cardBottom = cardTop + cardHeight
        val cardRect = RectF(left, cardTop, right, cardBottom)
        val radius = CARD_RADIUS * scale

        canvas.drawRoundRect(cardRect, radius, radius, fillPaint(SURFACE))
        canvas.drawRoundRect(cardRect, radius, radius, linePaint(if (day.isToday) TEAL else OUTLINE, if (day.isToday) 2f * scale else 1f * scale))

        // Plain rect, clipped to the card's rounded bounds, so only its outer corners are cut off
        // by the card's own rounding - simpler and safer than nesting a second rounded shape.
        val accentColor = if (day.isSpecial) BURGUNDY else GOLD
        val accentBar = RectF(right - ACCENT_BAR_WIDTH * scale, cardTop, right, cardBottom)
        canvas.save()
        canvas.clipPath(android.graphics.Path().apply { addRoundRect(cardRect, radius, radius, android.graphics.Path.Direction.CW) })
        canvas.drawRect(accentBar, fillPaint(accentColor))
        canvas.restore()

        // rowTop tracks the top of each row's own vertical slot (not its baseline), so the
        // cursor advance below always equals that row's full allotted height (fontSize *
        // multiplier) with no double-counting - the baseline is only ever derived from rowTop
        // for the drawText call itself, never fed back into the cursor (spec follow-up: the
        // previous version added a full text-height to reach the baseline *and then* advanced by
        // the full row height again, silently inflating each row and pushing every card short of
        // what its content actually needed - the exact cause of lines climbing on top of the
        // next card).
        val textRight = right - ACCENT_BAR_WIDTH * scale - 6f * scale
        val textLeft = left + CARD_INSET * scale
        var rowTop = cardTop + CARD_INSET * scale
        val headerPaint = textPaint(HEADER_SIZE * scale, bold = true, color = TEAL)
        canvas.drawText("${day.dateLabel}   ${day.hebrewLabel}", textRight, rowTop + headerPaint.textSize, headerPaint)
        rowTop += headerPaint.textSize * 1.3f
        day.noteLine?.let {
            val notePaint = textPaint(NOTE_SIZE * scale, bold = true, color = BURGUNDY)
            canvas.drawText(it, textRight, rowTop + notePaint.textSize, notePaint)
            rowTop += notePaint.textSize * 1.3f
        }
        drawLines(canvas, day.lines, textLeft, rowTop, LINE_SIZE * scale)
        return cardBottom
    }

    /** A labeled card for "זמני השבוע"/"לימוד יומי", styled like the on-screen WeekZmanimBox/WeekStudyBox. */
    private fun drawSectionCard(canvas: Canvas, title: String, lines: List<PrintLine>, right: Float, left: Float, startY: Float, scale: Float): Float {
        val cardTop = startY
        val cardHeight = sectionCardHeight(lines.size) * scale
        val cardBottom = cardTop + cardHeight
        val cardRect = RectF(left, cardTop, right, cardBottom)
        val radius = CARD_RADIUS * scale

        canvas.drawRoundRect(cardRect, radius, radius, fillPaint(SURFACE))
        canvas.drawRoundRect(cardRect, radius, radius, linePaint(GOLD, 1.2f * scale))

        val textRight = right - CARD_INSET * scale
        val textLeft = left + CARD_INSET * scale
        var rowTop = cardTop + CARD_INSET * scale
        val headerPaint = textPaint(HEADER_SIZE * scale, bold = true, color = TEAL)
        canvas.drawText(title, textRight, rowTop + headerPaint.textSize, headerPaint)
        rowTop += headerPaint.textSize * 1.3f
        drawLines(canvas, lines, textLeft, rowTop, LINE_SIZE * scale)
        return cardBottom
    }

    /** The candle-lighting/havdalah summary as a filled burgundy band with parchment text,
     * echoing ShabbatTimeBox's colored strip on the weekly screen. */
    private fun drawSummaryBand(canvas: Canvas, text: String, right: Float, left: Float, startY: Float, scale: Float) {
        val bandTop = startY
        val bandHeight = summaryBandHeight() * scale
        val bandRect = RectF(left, bandTop, right, bandTop + bandHeight)
        val radius = CARD_RADIUS * scale
        canvas.drawRoundRect(bandRect, radius, radius, fillPaint(BURGUNDY))
        val paint = centerTextPaint(NOTE_SIZE * 1.15f * scale, bold = true, color = PARCHMENT)
        canvas.drawText(text, (left + right) / 2f, bandTop + bandHeight / 2f + paint.textSize * 0.35f, paint)
    }

    private fun renderMonth(canvas: Canvas, rect: RectF, content: PrintMonthContent) {
        val scale = scaleFor(rect)
        val titlePaint = textPaint(TITLE_SIZE * scale, bold = true, color = TEAL)
        val right = rect.right - PADDING
        val left = rect.left + PADDING
        var y = rect.top + PADDING + titlePaint.textSize
        canvas.drawText(content.title, right, y, titlePaint)
        y += titlePaint.textSize * 0.35f
        canvas.drawLine(left, y, right, y, linePaint(GOLD, 1.5f * scale))
        y += 6f * scale

        val gridTop = y
        val gridHeight = rect.bottom - PADDING - gridTop
        val rows = (content.cells.size / 7).coerceAtLeast(1)
        val colWidth = (rect.width() - PADDING * 2) / 7f
        val headerRowHeight = HEADER_SIZE * scale * 2f
        val rowHeight = (gridHeight - headerRowHeight) / rows

        // Weekday header row: a colored tag band per column (burgundy for Shabbat, brass-gold
        // for the rest) with parchment text, echoing DayCell's own colored weekday tag strip.
        for ((index, label) in content.weekdayLabels.withIndex()) {
            val colRight = rect.right - PADDING - index * colWidth
            val isShabbosCol = index == content.weekdayLabels.lastIndex
            val bandRect = RectF(colRight - colWidth, gridTop, colRight, gridTop + headerRowHeight)
            canvas.drawRect(bandRect, fillPaint(if (isShabbosCol) BURGUNDY else GOLD))
            val headerPaint = centerTextPaint(HEADER_SIZE * scale, bold = true, color = PARCHMENT)
            canvas.drawText(label, colRight - colWidth / 2f, gridTop + headerRowHeight / 2f + headerPaint.textSize * 0.35f, headerPaint)
        }

        val dayNumPaint = textPaint(HEADER_SIZE * scale)
        for ((cellIndex, cell) in content.cells.withIndex()) {
            val row = cellIndex / 7
            val col = cellIndex % 7
            val cellRight = rect.right - PADDING - col * colWidth
            val cellTop = gridTop + headerRowHeight + rowHeight * row
            val cellRect = RectF(cellRight - colWidth + 1f, cellTop + 1f, cellRight - 1f, cellTop + rowHeight - 1f)

            canvas.drawRect(cellRect, fillPaint(if (cell.inMonth) SURFACE else PARCHMENT))
            canvas.drawRect(cellRect, linePaint(OUTLINE, 0.75f))

            val cellAlpha = if (cell.inMonth) 255 else 110
            val numberColor = if (cell.isSpecial) BURGUNDY else INK
            dayNumPaint.color = numberColor
            dayNumPaint.alpha = cellAlpha
            dayNumPaint.textAlign = Paint.Align.CENTER
            val hebrewDayPaint = centerTextPaint(NOTE_SIZE * scale, color = if (cell.isSpecial) BURGUNDY else GOLD)
            hebrewDayPaint.alpha = cellAlpha
            val centerX = cellRight - colWidth / 2f
            canvas.drawText(cell.dayNumber, centerX, cellTop + rowHeight * 0.42f, dayNumPaint)
            canvas.drawText(cell.hebrewDay, centerX, cellTop + rowHeight * 0.72f, hebrewDayPaint)

            if (cell.hasEvents) {
                canvas.drawCircle(cellRight - colWidth + 8f * scale, cellTop + 8f * scale, 2.5f * scale, fillPaint(EVENT_INDIGO))
            }
            if (cell.isToday) {
                canvas.drawRect(cellRect, linePaint(TEAL, 2f * scale))
            }
        }
    }

    /** Value/time and its label drawn as one cluster pinned to the row's left edge (time first,
     * label just to its right) - this mirrors the app's own on-screen ZmanLine, which under the
     * app's RTL layout direction pins that same [Spacer(weight) - label - time] cluster to the
     * left with the flexible gap on the right (spec follow-up: an earlier version spread label
     * and value across the whole row width instead of keeping them together like the on-screen
     * calendar does). */
    private fun drawLines(canvas: Canvas, lines: List<PrintLine>, left: Float, startY: Float, textSize: Float): Float {
        var rowTop = startY
        val labelPaint = textPaint(textSize, color = INK_MUTED).apply { textAlign = Paint.Align.LEFT }
        val valuePaint = textPaint(textSize, bold = true, color = TEAL).apply { textAlign = Paint.Align.LEFT }
        for (line in lines) {
            val baseline = rowTop + textSize
            canvas.drawText(line.value, left, baseline, valuePaint)
            val valueWidth = valuePaint.measureText(line.value)
            canvas.drawText(line.label, left + valueWidth + textSize * 0.5f, baseline, labelPaint)
            rowTop += textSize * 1.5f
        }
        return rowTop
    }

    /** Text/line sizes are authored for a full-page rect - shrink proportionally for a tiled
     * (2/4/8-up) sub-rect, since [PADDING] and font sizes are otherwise fixed point sizes. */
    private fun scaleFor(rect: RectF): Float = (rect.width() / 500f).coerceIn(0.4f, 1.2f)
}

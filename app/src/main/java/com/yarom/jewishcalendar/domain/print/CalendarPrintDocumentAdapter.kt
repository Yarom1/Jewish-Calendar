package com.yarom.jewishcalendar.domain.print

import android.content.Context
import android.graphics.RectF
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import java.io.FileOutputStream
import java.io.IOException

/**
 * Renders [content] as a single-page PDF, tiling [copiesPerPage] identical copies onto that one
 * physical sheet (1/2/4/8-up) - handed to Android's own PrintManager, whose system dialog already
 * offers "Save as PDF" (to a user-chosen folder, typically Downloads) and any installed print
 * app/service, so this adapter only needs to produce the page content.
 */
class CalendarPrintDocumentAdapter(
    private val context: Context,
    private val jobName: String,
    private val content: PrintContent,
    private val copiesPerPage: Int,
) : PrintDocumentAdapter() {

    private var pdfDocument: PrintedPdfDocument? = null

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?,
    ) {
        pdfDocument = PrintedPdfDocument(context, newAttributes)
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }
        val info = PrintDocumentInfo.Builder(jobName)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(1)
            .build()
        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback,
    ) {
        val document = pdfDocument
        if (document == null) {
            callback.onWriteFailed(null)
            return
        }
        val page = document.startPage(0)
        val pageInfo = page.info
        val fullRect = RectF(0f, 0f, pageInfo.pageWidth.toFloat(), pageInfo.pageHeight.toFloat())
        for (rect in tileRects(fullRect, copiesPerPage)) {
            // Clip each tile to its own rect - a safety net so that if a tile's content ever runs
            // longer than its auto-fit scale estimated (small tiles like 4/8-up leave very little
            // margin for error), the overflow is cropped cleanly instead of bleeding into the next
            // tile's area and getting painted over by its background (spec follow-up: that's what
            // "the design broke" turned out to be - tile 1's overflow getting stomped by tile 2's
            // own fill, not a drawing corruption).
            page.canvas.save()
            page.canvas.clipRect(rect)
            CalendarPrintRenderer.render(page.canvas, rect, content)
            page.canvas.restore()
        }
        document.finishPage(page)

        try {
            FileOutputStream(destination.fileDescriptor).use { out -> document.writeTo(out) }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: IOException) {
            callback.onWriteFailed(e.message)
        } finally {
            document.close()
            pdfDocument = null
        }
    }

    override fun onFinish() {
        pdfDocument?.close()
        pdfDocument = null
    }

    private fun tileRects(full: RectF, count: Int): List<RectF> {
        val (cols, rows) = when (count) {
            2 -> 1 to 2
            4 -> 2 to 2
            8 -> 2 to 4
            else -> 1 to 1
        }
        val cellWidth = full.width() / cols
        val cellHeight = full.height() / rows
        val margin = 8f
        val rects = mutableListOf<RectF>()
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                rects.add(
                    RectF(
                        col * cellWidth + margin,
                        row * cellHeight + margin,
                        (col + 1) * cellWidth - margin,
                        (row + 1) * cellHeight - margin,
                    ),
                )
            }
        }
        return rects
    }
}

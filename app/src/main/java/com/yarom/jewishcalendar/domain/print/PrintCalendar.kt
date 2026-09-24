package com.yarom.jewishcalendar.domain.print

import android.content.Context
import android.print.PrintManager

/** Opens Android's system print dialog for [content] - it already offers "Save as PDF" and every
 * installed print app/service, so no separate save-vs-choose-app UI is needed here. */
fun printCalendarContent(context: Context, jobName: String, content: PrintContent, copiesPerPage: Int) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    printManager.print(jobName, CalendarPrintDocumentAdapter(context, jobName, content, copiesPerPage), null)
}

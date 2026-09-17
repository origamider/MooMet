package com.example.moomet.usage

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object UsageTimeFormatter {
    fun formatTime(timeMs: Long): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.JAPAN)

        return formatter.format(Date(timeMs))
    }
}

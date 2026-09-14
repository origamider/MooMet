package com.example.moomet.data

data class AppUsageSession(
    val packageName: String,
    val startTimeMs: Long,
    val endTimeMs: Long
)
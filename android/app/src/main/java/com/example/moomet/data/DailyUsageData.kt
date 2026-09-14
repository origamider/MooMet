package com.example.moomet.data

data class DailyUsageData(
    val date: String,
    val timeZone: String,
    val apps: List<AppUsageData>
)
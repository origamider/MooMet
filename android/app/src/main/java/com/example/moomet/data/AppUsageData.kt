package com.example.moomet.data

enum class UsageCategory {
    SOCIAL_MEDIA,
    AI_TOOL
}

data class AppUsageData(
    val packageName: String,
    val appName: String,
    val category: UsageCategory,
    val foregroundDurationMs: Long
)
package com.example.moomet.usage

import android.app.usage.UsageEvents
import com.example.moomet.data.AppUsageSession
import com.example.moomet.data.AppUsageData
import com.example.moomet.data.DailyUsageData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object UsageDataProcessor {

    @Suppress("DEPRECATION")
    fun createAppUsageSessionList(usageEvents: UsageEvents?): List<AppUsageSession> {
        if (usageEvents == null) {
            return emptyList()
        }

        val sessionList = mutableListOf<AppUsageSession>()

        var activePackageName: String? = null
        var activeStartTimeMs: Long? = null

        fun closeActiveSession(endTimeMs: Long) {
            val packageName = activePackageName ?: return
            val startTimeMs = activeStartTimeMs ?: return

            if (endTimeMs > startTimeMs) {
                sessionList.add(
                    AppUsageSession(
                        packageName = packageName,
                        startTimeMs = startTimeMs,
                        endTimeMs = endTimeMs
                    )
                )
            }

            activePackageName = null
            activeStartTimeMs = null
        }

        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)

            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    val resumedPackageName =
                        event.packageName ?: continue

                    if (resumedPackageName == activePackageName) {
                        continue
                    }

                    closeActiveSession(event.timeStamp)

                    if (TrackedAppCatalog.getUsageCategory(resumedPackageName) != null) {
                        activePackageName = resumedPackageName
                        activeStartTimeMs = event.timeStamp
                    }
                }

                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val pausedPackageName =
                        event.packageName ?: continue

                    if (pausedPackageName == activePackageName) {
                        closeActiveSession(event.timeStamp)
                    }
                }

                UsageEvents.Event.SCREEN_NON_INTERACTIVE,
                UsageEvents.Event.KEYGUARD_SHOWN,
                UsageEvents.Event.DEVICE_SHUTDOWN -> {
                    closeActiveSession(event.timeStamp)
                }
            }
        }

        return sessionList

    }

    fun createAppUsageDataList(appUsageSessionList: List<AppUsageSession>): List<AppUsageData> {
        val durationByPackage = mutableMapOf<String, Long>()

        for (session in appUsageSessionList) {
            val durationMs = session.endTimeMs - session.startTimeMs

            if (durationMs <= 0) {
                continue
            }

            val currentTotal = durationByPackage[session.packageName] ?: 0L
            durationByPackage[session.packageName] = currentTotal + durationMs
        }

        val appUsageDataList = mutableListOf<AppUsageData>()

        for ((packageName, totalDurationMs) in durationByPackage) {
            val category = TrackedAppCatalog.getUsageCategory(packageName) ?: continue

            val appName = TrackedAppCatalog.getAppName(packageName) ?: continue

            appUsageDataList.add(
                AppUsageData(
                    packageName = packageName,
                    appName = appName,
                    category = category,
                    foregroundDurationMs = totalDurationMs
                )
            )
        }
        return appUsageDataList
    }

    fun createDailyUsageData(appUsageDataList: List<AppUsageData>): DailyUsageData {
        val calendar = Calendar.getInstance()

        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val date = dateFormatter.format(calendar.time)

        val timeZone = calendar.timeZone.id

        return DailyUsageData(
            date = date,
            timeZone = timeZone,
            apps = appUsageDataList
        )
    }
}
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
        val startTimeByPackage = mutableMapOf<String, Long>()
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)

            val eventPackageName = event.packageName ?: continue

            if (TrackedAppCatalog.getUsageCategory(eventPackageName) == null) {
                continue
            }

            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    if (eventPackageName !in startTimeByPackage) {
                        startTimeByPackage[eventPackageName] = event.timeStamp
                    }
                }

                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val startTime = startTimeByPackage.remove(eventPackageName) ?: continue

                    if (event.timeStamp > startTime) {
                        sessionList.add(
                            AppUsageSession(
                                packageName = eventPackageName,
                                startTimeMs = startTime,
                                endTimeMs = event.timeStamp
                            )
                        )
                    }
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
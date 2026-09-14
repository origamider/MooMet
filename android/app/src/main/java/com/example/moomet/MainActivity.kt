package com.example.moomet

import android.os.Bundle
import android.widget.Toast
import android.app.usage.UsageStats
import android.app.usage.UsageEvents
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.material3.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.moomet.ui.theme.MooMetTheme
import com.example.moomet.data.AppUsageData
import com.example.moomet.data.DailyUsageData
import com.example.moomet.data.AppUsageSession
import com.example.moomet.usage.TrackedAppCatalog
import com.example.moomet.usage.UsageStatsReader
import java.util.Locale
import java.util.Calendar
import java.text.SimpleDateFormat

class MainActivity : ComponentActivity() {
    private lateinit var usageStatsReader: UsageStatsReader
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        usageStatsReader = UsageStatsReader(this)
        
        enableEdgeToEdge()
        setContent {
            MooMetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Button(
                        onClick = {
                            if (usageStatsReader.hasUsageStatsPermission()) {
                                val appUsageSessionList = createAppUsageSessionList()
                                
                                for(session in appUsageSessionList) {
                                    val durationMs = session.endTimeMs - session.startTimeMs
                                    
                                    Log.d(
                                        "MooMetUsageSession",
                                        "${session.packageName}: " +
                                            "start=${session.startTimeMs}, " +   
                                            "end=${session.endTimeMs}, " +
                                            "duration=${durationMs}ms"
                                                 
                                     )
                                }
                                
                                val usageStatsList = usageStatsReader.getTodayUsageStats()

                                val appUsageDataList = createAppUsageDataList(usageStatsList)

                                val dailyUsageData = createDailyUsageData(appUsageDataList)

                                Log.d(
                                    "MooMetDailyUsage",
                                    dailyUsageData.toString()
                                )

                                for (appUsageData in appUsageDataList) {
                                    val minutes = appUsageData.foregroundDurationMs / 1000 / 60
                                    
                                    Log.d(
                                        "MooMetAppUsage",
                                        "${appUsageData.appName} " +
                                        "(${appUsageData.category}): ${minutes}分"
                                    )
                                    
                                }

                                Toast.makeText(
                                    this@MainActivity,
                                    "対象アプリを${appUsageDataList.size}件取得しました",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                startActivity(intent)
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        Text("使用状況へのアクセスを許可")
                    }
                }
            }
        }
    }

    private fun logTodayUsageEvents() {
        val usageEvents = usageStatsReader.getTodayUsageEvents()

        if(usageEvents == null) {
            Log.d(
                "MooMetUsageEvent",
                "使用状況イベントを取得できませんでした"
                )
                return
        }

        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)

            val eventPackageName = event.packageName ?: continue

            if(TrackedAppCatalog.getUsageCategory(eventPackageName) != null) {
                Log.d(
                    "MooMetUsageEvent",
                    "$eventPackageName: " +
                        "type=${event.eventType}, " +
                        "time=${event.timeStamp}"
                )
            }
        }
    }
    
    @Suppress("DEPRECATION")
    private fun createAppUsageSessionList(): List<AppUsageSession> {
        val usageEvents = usageStatsReader.getTodayUsageEvents() ?: return emptyList()
                                    
        val sessionList = mutableListOf<AppUsageSession>()
        val startTimeByPackage = mutableMapOf<String, Long>()
        val event = UsageEvents.Event()
                                    
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
                                        
            val eventPackageName = event.packageName ?: continue
            if (TrackedAppCatalog.getUsageCategory(eventPackageName) == null) {continue}
            
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

    private fun createAppUsageDataList(
        usageStatsList: List<UsageStats>
    ): List<AppUsageData> {
        val appUsageDataList = mutableListOf<AppUsageData>()

        for (usageStats in usageStatsList) {
            val category = TrackedAppCatalog.getUsageCategory(usageStats.packageName)

            val appName = TrackedAppCatalog.getAppName(usageStats.packageName)

            if (
                category != null &&
                appName != null &&
                usageStats.totalTimeInForeground > 0
            ) {
                val appUsageData = AppUsageData(
                    packageName = usageStats.packageName,
                    appName = appName,
                    category = category,
                    foregroundDurationMs = usageStats.totalTimeInForeground
                )
                appUsageDataList.add(appUsageData)
            }
        }
        return appUsageDataList
    }

    private fun createDailyUsageData(
        appUsageDataList: List<AppUsageData>
    ): DailyUsageData {
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
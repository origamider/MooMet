package com.example.moomet

import android.os.Bundle
import android.widget.Toast
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
import com.example.moomet.usage.UsageStatsReader
import com.example.moomet.usage.UsageDataProcessor
import com.example.moomet.usage.UsageTimeFormatter

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
                                val usageEvents = usageStatsReader.getTodayUsageEvents()

                                val appUsageSessionList =
                                    UsageDataProcessor.createAppUsageSessionList(usageEvents)

                                for (session in appUsageSessionList) {
                                    val durationMs = session.endTimeMs - session.startTimeMs

                                    val startTime =
                                        UsageTimeFormatter.formatTime(session.startTimeMs)

                                    val endTime = UsageTimeFormatter.formatTime(session.endTimeMs)

                                    Log.d(
                                        "MooMetUsageSession",
                                        "${session.packageName}: " +
                                                "$startTime ~ $endTime, " +
                                                "duration=${durationMs}ms"

                                    )
                                }

                                val appUsageDataList =
                                    UsageDataProcessor.createAppUsageDataList(appUsageSessionList)

                                val dailyUsageData =
                                    UsageDataProcessor.createDailyUsageData(appUsageDataList)

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
}

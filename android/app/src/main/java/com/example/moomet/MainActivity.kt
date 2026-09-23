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
import androidx.compose.foundation.layout.Column
import androidx.lifecycle.lifecycleScope
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.example.moomet.ui.theme.MooMetTheme
import com.example.moomet.usage.UsageStatsReader
import com.example.moomet.usage.UsageDataProcessor
import com.example.moomet.usage.UsageTimeFormatter
import com.example.moomet.health.HealthConnectReader
import com.example.moomet.health.HealthConnectAvailability
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MainActivity : ComponentActivity() {
    private lateinit var usageStatsReader: UsageStatsReader
    private lateinit var healthConnectReader: HealthConnectReader

    private val requestHealthPermissionsLauncher =
        registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { grantedPermissions ->
            val sleepGranted =
                HealthPermission.getReadPermission(SleepSessionRecord::class) in grantedPermissions

            val exerciseGranted =
                HealthPermission.getReadPermission(ExerciseSessionRecord::class) in grantedPermissions


            Log.d(
                "MooMetHealthConnect",
                "permissionResult: sleep=$sleepGranted, exercise=$exerciseGranted"
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        usageStatsReader = UsageStatsReader(this)
        healthConnectReader = HealthConnectReader(this)

        val healthConnectAvailability = healthConnectReader.getAvailability()

        Log.d(
            "MooMetHealthConnect",
            "availability=$healthConnectAvailability"
        )

        if (healthConnectAvailability == HealthConnectAvailability.AVAILABLE) {
            lifecycleScope.launch {
                val hasSleepPermission = healthConnectReader.hasSleepPermission()
                val hasExercisePermission = healthConnectReader.hasExercisePermission()

                Log.d(
                    "MooMetHealthConnect",
                    "currentPermissions: sleep=$hasSleepPermission, exercise=$hasExercisePermission"
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            MooMetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ) {
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

                                        val endTime =
                                            UsageTimeFormatter.formatTime(session.endTimeMs)

                                        Log.d(
                                            "MooMetUsageSession",
                                            "${session.packageName}: " +
                                                    "$startTime ~ $endTime, " +
                                                    "duration=${durationMs}ms"

                                        )
                                    }

                                    val appUsageDataList =
                                        UsageDataProcessor.createAppUsageDataList(
                                            appUsageSessionList
                                        )

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
                            }
                        ) {
                            Text("使用状況へのアクセスを許可")
                        }

                        Button(
                            onClick = {
                                when (healthConnectAvailability) {
                                    HealthConnectAvailability.AVAILABLE -> {
                                        requestHealthPermissionsLauncher.launch(
                                            healthConnectReader.requiredPermissions
                                        )
                                    }

                                    HealthConnectAvailability.UPDATE_REQUIRED -> {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Health Connectの更新が必要です",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    HealthConnectAvailability.UNAVAILABLE -> {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "この端末ではHealth Connectを利用できません",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        ) {
                            Text("健康データへのアクセスを許可")
                        }

                        Button(
                            onClick = {
                                lifecycleScope.launch {
                                    if (!healthConnectReader.hasSleepPermission()) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "健康データへのアクセス許可が必要です",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        return@launch
                                    }

                                    val zoneId = ZoneId.systemDefault()

                                    val startTime = LocalDate.now(zoneId)
                                        .minusDays(1)
                                        .atTime(12, 0)
                                        .atZone(zoneId)
                                        .toInstant()

                                    val endTime = Instant.now()

                                    val sleepSessions =
                                        healthConnectReader.readSleepSessions(startTime, endTime)

                                    for (sleepSession in sleepSessions) {
                                        val startTimeText =
                                            UsageTimeFormatter.formatTime(sleepSession.startTime.toEpochMilli())

                                        val endTimeText =
                                            UsageTimeFormatter.formatTime(sleepSession.endTime.toEpochMilli())

                                        val durationMinutes =
                                            Duration.between(sleepSession.startTime, sleepSession.endTime).toMinutes()

                                        Log.d(
                                            "MooMetSleep",
                                            "$startTimeText ~ $endTimeText, " +
                                                    "duration=${durationMinutes}分"
                                        )
                                    }

                                    Toast.makeText(
                                        this@MainActivity,
                                        "睡眠データを${sleepSessions.size}件取得しました",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        ) {
                            Text("睡眠データを取得")
                        }
                        Button(
                            onClick = {
                                lifecycleScope.launch {
                                    if (!healthConnectReader.hasExercisePermission()) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "運動データへのアクセス許可が必要です",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@launch
                                    }

                                    val zoneId = ZoneId.systemDefault()

                                    val startOfToday = LocalDate.now(zoneId)
                                        .atStartOfDay(zoneId)
                                        .toInstant()

                                    val now = Instant.now()

                                    val exerciseSessions =
                                        healthConnectReader.readExerciseSessions(startOfToday, now)

                                    Log.d(
                                        "MooMetExercise",
                                        "今日の運動セッション: ${exerciseSessions.size}件"
                                    )

                                    for (session in exerciseSessions) {
                                        val startTimeText =
                                            UsageTimeFormatter.formatTime(session.startTime.toEpochMilli())

                                        val endTimeText =
                                            UsageTimeFormatter.formatTime(session.endTime.toEpochMilli())

                                        val minutes =
                                            Duration.between(session.startTime, session.endTime).toMinutes()

                                        Log.d(
                                            "MooMetExercise",
                                            "$startTimeText ~ $endTimeText, 時間=${minutes}分"
                                        )
                                    }

                                    val totalExerciseMinutes = exerciseSessions.sumOf { session ->
                                        Duration.between(
                                            session.startTime,
                                            session.endTime
                                        ).toMinutes()
                                    }

                                    Log.d(
                                        "MooMetExercise",
                                        "今日の合計運動時間=${totalExerciseMinutes}分"
                                    )
                                }
                            }
                        ) {
                            Text("今日の運動データを取得")
                        }
                    }
                }
            }
        }
    }
}

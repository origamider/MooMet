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
import com.example.moomet.health.HealthConnectReader
import com.example.moomet.health.HealthConnectAvailability
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
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
                                    Toast.makeText(
                                        this@MainActivity,
                                        "使用状況へのアクセスは許可済みです",
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
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        updateTodayUsageData()
        updateTodayExerciseDuration()
        updateRecentSleepDuration()
    }

    private fun updateTodayUsageData() {
        if (!usageStatsReader.hasUsageStatsPermission()) {
            Log.d(
                "MooMetUsage",
                "アプリ使用状況の読み取り権限がありません"
            )
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val usageEvents =
                usageStatsReader.getTodayUsageEvents()

            val appUsageSessionList =
                UsageDataProcessor.createAppUsageSessionList(usageEvents)

            val appUsageDataList =
                UsageDataProcessor.createAppUsageDataList(
                    appUsageSessionList
                )

            val dailyUsageData =
                UsageDataProcessor.createDailyUsageData(
                    appUsageDataList
                )

            Log.d(
                "MooMetDailyUsage",
                "自動更新: $dailyUsageData"
            )

            for (appUsageData in appUsageDataList) {
                val minutes =
                    appUsageData.foregroundDurationMs / 1000 / 60

                Log.d(
                    "MooMetAppUsage",
                    "自動更新: ${appUsageData.appName} " +
                        "(${appUsageData.category}): ${minutes}分"
                )
            }
        }
    }

    private fun updateTodayExerciseDuration() {
        if (healthConnectReader.getAvailability() != HealthConnectAvailability.AVAILABLE) {
            return
        }

        lifecycleScope.launch {
            if (!healthConnectReader.hasExercisePermission()) {
                Log.d(
                    "MooMetExercise",
                    "運動データの読み取りができません"
                )
                return@launch
            }

            val zoneId = ZoneId.systemDefault()

            val startOfToday = LocalDate.now(zoneId)
                .atStartOfDay(zoneId)
                .toInstant()

            val now = Instant.now()

            val totalExerciseDuration =
                healthConnectReader.readTotalExerciseDuration(startOfToday, now)

            val totalExerciseMinutes =
                totalExerciseDuration.toMinutes()

            Log.d(
                "MooMetExercise",
                "自動更新: 今日の合計運動時間=${totalExerciseMinutes}分"
            )
        }
    }

    private fun updateRecentSleepDuration() {
        if (healthConnectReader.getAvailability() != HealthConnectAvailability.AVAILABLE) {
            return
        }

        lifecycleScope.launch {
            if (!healthConnectReader.hasSleepPermission()) {
                Log.d(
                    "MooMetSleep",
                    "睡眠データの読み取りができません"
                )
                return@launch
            }

            val zoneId = ZoneId.systemDefault()

            val startTime = LocalDate.now(zoneId)
                .minusDays(1)
                .atTime(12, 0)
                .atZone(zoneId)
                .toInstant()

            val endTime = Instant.now()

            val totalSleepDuration =
                healthConnectReader.readTotalSleepDuration(startTime, endTime)

            val totalSleepMinutes =
                totalSleepDuration.toMinutes()

            Log.d(
                "MooMetSleep",
                "自動更新: 今日の睡眠時間=${totalSleepMinutes}分"
            )
        }
    }
}

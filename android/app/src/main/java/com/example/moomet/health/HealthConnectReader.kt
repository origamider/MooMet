package com.example.moomet.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.Duration

class HealthConnectReader(private val context : Context) {
    val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )

    private val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    fun getAvailability(): HealthConnectAvailability {
        return when(HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE ->
                HealthConnectAvailability.AVAILABLE

            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                HealthConnectAvailability.UPDATE_REQUIRED

            else ->
                HealthConnectAvailability.UNAVAILABLE
        }
    }

    suspend fun hasSleepPermission(): Boolean {
        val grantedPermissions =
            healthConnectClient.permissionController.getGrantedPermissions()

        return HealthPermission.getReadPermission(SleepSessionRecord::class) in grantedPermissions
    }

    suspend fun hasExercisePermission(): Boolean {
        val grantedPermissions =
            healthConnectClient.permissionController.getGrantedPermissions()

        return HealthPermission.getReadPermission(ExerciseSessionRecord::class) in grantedPermissions
    }

    suspend fun readTotalExerciseDuration(startTime: Instant, endTime: Instant): Duration {
        val response = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        return response[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL] ?: Duration.ZERO
    }

    suspend fun readTotalSleepDuration(startTime: Instant, endTime: Instant): Duration {
        val response = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        return response[SleepSessionRecord.SLEEP_DURATION_TOTAL] ?: Duration.ZERO
    }
}
package com.example.moomet.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

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

    suspend fun readSleepSessions(startTime: Instant, endTime: Instant): List<SleepSessionRecord> {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        return response.records
    }

    suspend fun readExerciseSessions(startTime: Instant, endTime: Instant): List<ExerciseSessionRecord> {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        return response.records
    }
}
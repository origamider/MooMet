package com.example.moomet.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import java.util.Calendar

class UsageStatsReader(private val context: Context) {
    fun hasUsageStatsPermission(): Boolean {
        val appOpsManager = 
            context.getSystemService(AppOpsManager::class.java)
        
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        
        return mode == AppOpsManager.MODE_ALLOWED
    }
    
    fun getTodayUsageEvents(): UsageEvents? {
        val usageStatsManager = 
            context.getSystemService(UsageStatsManager::class.java)
            
        return usageStatsManager.queryEvents(
            getStartOfTodayMillis(),
            System.currentTimeMillis()
        )
    }
    
    private fun getStartOfTodayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    
}
package com.example.moomet.usage

import com.example.moomet.data.UsageCategory

object TrackedAppCatalog {
    private val socialMediaPackages: Map<String, String> = mapOf(
        "com.google.android.youtube" to "YouTube",
        "com.instagram.android" to "Instagram",
        "com.twitter.android" to "X",
        "com.zhiliaoapp.musically" to "TikTok"
    )
    
    private val aiToolPackages: Map<String, String> = mapOf(
        "com.openai.chatgpt" to "ChatGPT",
        "com.google.android.apps.bard" to "Gemini"
    )
    
    fun getUsageCategory(packageName: String): UsageCategory? {
        return when {
            packageName in socialMediaPackages ->
                UsageCategory.SOCIAL_MEDIA
            
            packageName in aiToolPackages ->
                UsageCategory.AI_TOOL
            
                else -> null
        }
    }
    
    fun getAppName(packageName: String): String? {
        return socialMediaPackages[packageName]
            ?: aiToolPackages[packageName]
    }
}
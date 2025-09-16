package com.astro.autotapper.repository

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                ?: return false
            
            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            
            val serviceFound = enabledServices.any { serviceInfo ->
                val packageName = serviceInfo.resolveInfo?.serviceInfo?.packageName
                val serviceName = serviceInfo.resolveInfo?.serviceInfo?.name
                android.util.Log.d("PermissionRepository", "Found service: $packageName/$serviceName")
                packageName == context.packageName && 
                serviceName == "com.astro.autotapper.service.AutoTapAccessibilityService"
            }
            
            android.util.Log.d("PermissionRepository", "Accessibility service enabled: $serviceFound")
            serviceFound
        } catch (e: Exception) {
            android.util.Log.e("PermissionRepository", "Error checking accessibility service", e)
            false
        }
    }
}

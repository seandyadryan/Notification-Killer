package com.deploydulupulangnanti.notificationkiller.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

data class InstalledAppInfo(val packageName: String, val appName: String)

object AppInstalledHelper {
    fun getInstalledLaunchableApps(context: Context): List<InstalledAppInfo> {
        val pm = context.packageManager
        val list = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val result = mutableListOf<InstalledAppInfo>()
        for (app in list) {
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
            if (!isSystem || launchIntent != null) {
                result.add(InstalledAppInfo(app.packageName, pm.getApplicationLabel(app).toString()))
            }
        }
        return result.sortedBy { it.appName.lowercase() }
    }
}

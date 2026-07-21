package com.example.feedfree.ui.stats

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.feedfree.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import android.app.AppOpsManager
import android.provider.Settings
import kotlinx.coroutines.flow.update

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable
)

// Modello per la singola app usata (con tempo e icona)
data class AppUsageItem(
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    val usageTimeMillis: Long
)

// Lo stato completo della pagina Statistiche
data class StatsUiState(
    val totalScreenTimeMillis: Long, // Per il cerchio grande in alto
    val usedApps: List<AppUsageItem>, // Per le barre "Le tue attività monitorate"
    val allInstalledApps: List<AppInfo> // Per i cerchietti in basso "Le tue applicazioni"
)

fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

// 1. Ereditiamo da AndroidViewModel e passiamo l'Application
class StatsViewModel(application: Application) : AndroidViewModel(application) {

    // Stato reattivo interno
    private val _uiState = MutableStateFlow<StatsUiState?>(null)
    val uiState: StateFlow<StatsUiState?> = _uiState.asStateFlow()

    private val _blockedApps = MutableStateFlow<Set<String>>(emptySet())
    val blockedApps = _blockedApps.asStateFlow()

    // Set che contiene i packageName delle app monitorate
    private val _monitoredApps = MutableStateFlow<Set<String>>(emptySet())
    val monitoredApps = _monitoredApps.asStateFlow()

    // Mappa che associa il packageName ai millisecondi del timer
    private val _appTimers = MutableStateFlow<Map<String, Long>>(emptyMap())
    val appTimers = _appTimers.asStateFlow()

    init {
        loadAndroidData()
    }
    private fun loadAndroidData() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
            val launcherPackage = context.packageManager.resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName

            val usageStats = getTodayUsageStats(context)
            val installedApps = getInstalledApps(context)

            val usedAppsList = installedApps.mapNotNull { appInfo ->
                val time = usageStats[appInfo.packageName]
                if (time != null && time > 0 && appInfo.packageName != launcherPackage) {
                    AppUsageItem(
                        appName = appInfo.appName,
                        packageName = appInfo.packageName,
                        icon = appInfo.icon,
                        usageTimeMillis = time
                    )
                } else {
                    null
                }
            }.sortedByDescending { it.usageTimeMillis }

            usedAppsList.forEach { appItem ->
                val minutes = appItem.usageTimeMillis / 1000 / 60
                android.util.Log.d("APP_SCREEN_TIME", "App: ${appItem.appName} - Tempo: $minutes minuti")
            }

            // ELENCO DELLE APP DI SISTEMA PURAMENTE STRUMENTALI DA ESCLUDERE DAL TOTALE
            val ignoredPackages = listOf(
                "com.android.settings",
                "com.google.android.apps.maps",
                "com.google.android.googlequicksearchbox"
            )

            // SOMMA PULITA: Sommiamo tutte le app usate per più di 1 minuto, escludendo gli strumenti di sistema
            val totalCleanTime = usedAppsList
                .filter { it.usageTimeMillis > 60 * 1000 && !ignoredPackages.contains(it.packageName) }
                .sumOf { it.usageTimeMillis }

            // 2. Salviamo nello stato il tempo totale pulito e coerente con l'utilizzo reale
            _uiState.value = StatsUiState(
                totalScreenTimeMillis = totalCleanTime,
                usedApps = usedAppsList,
                allInstalledApps = installedApps
            )
        }
    }
    private fun getInstalledApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        val appsList = mutableListOf<AppInfo>()

        for (resolveInfo in resolveInfos) {
            val packageName = resolveInfo.activityInfo.packageName
            val appName = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)

            appsList.add(AppInfo(packageName, appName, icon))
        }
        return appsList
    }

    fun setAppBlocked(app: AppInfo, isBlocked: Boolean) {
        _blockedApps.update { currentSet ->
            if (isBlocked) currentSet + app.packageName else currentSet - app.packageName
        }
    }

    fun setAppMonitored(app: AppInfo, isMonitored: Boolean) {
        _monitoredApps.update { currentSet ->
            if (isMonitored) currentSet + app.packageName else currentSet - app.packageName
        }
    }

    fun setAppTimer(app: AppInfo, timerMillis: Long) {
        _appTimers.update { currentMap ->
            val newMap = currentMap.toMutableMap()
            if (timerMillis > 0) {
                newMap[app.packageName] = timerMillis
            } else {
                newMap.remove(app.packageName) // Rimuove il timer se impostato a 0
            }
            newMap
        }
    }

    private fun getTrueScreenTime(context: Context, startTime: Long, endTime: Long): Long {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = android.app.usage.UsageEvents.Event()

        var totalScreenTime = 0L
        var lastScreenOnTime = startTime
        var isScreenOn = false

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                android.app.usage.UsageEvents.Event.SCREEN_INTERACTIVE -> {
                    isScreenOn = true
                    lastScreenOnTime = event.timeStamp
                }
                android.app.usage.UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    if (isScreenOn) {
                        totalScreenTime += (event.timeStamp - lastScreenOnTime)
                        isScreenOn = false
                    }
                }
            }
        }

        // Se lo schermo è attualmente acceso, aggiunge il tempo parziale fino a questo momento
        if (isScreenOn) {
            totalScreenTime += (System.currentTimeMillis() - lastScreenOnTime)
        }

        return totalScreenTime
    }

    private fun getTodayUsageStats(context: Context): Map<String, Long> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = android.app.usage.UsageEvents.Event()

        // Mappa temporanea per tracciare quando un'app è andata in foreground
        val appStartTimes = mutableMapOf<String, Long>()
        val appUsageTimes = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val packageName = event.packageName

            when (event.eventType) {
                android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED,
                android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    appStartTimes[packageName] = event.timeStamp
                }
                android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED,
                android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val startTimeApp = appStartTimes[packageName]
                    if (startTimeApp != null && event.timeStamp >= startTimeApp) {
                        val duration = event.timeStamp - startTimeApp
                        appUsageTimes[packageName] = (appUsageTimes[packageName] ?: 0L) + duration
                        appStartTimes.remove(packageName)
                    }
                }
            }
        }

        // Se un'app è attualmente aperta in primo piano, calcoliamo il tempo parziale fino ad adesso
        for ((packageName, startTimeApp) in appStartTimes) {
            if (startTimeApp >= startTime) {
                val duration = System.currentTimeMillis() - startTimeApp
                appUsageTimes[packageName] = (appUsageTimes[packageName] ?: 0L) + duration
            }
        }

        return appUsageTimes
    }

}
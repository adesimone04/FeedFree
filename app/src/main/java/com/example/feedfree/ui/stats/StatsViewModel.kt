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
        // 2. Usiamo viewModelScope.launch(Dispatchers.IO) per eseguire
        // la lettura dei dati in background, senza bloccare l'interfaccia grafica
        viewModelScope.launch(Dispatchers.IO) {

            // 3. Ora possiamo usare getApplication() come Context!
            val context = getApplication<Application>()

            val usageStats = getTodayUsageStats(context)
            val installedApps = getInstalledApps(context)

            val usedAppsList = installedApps.mapNotNull { appInfo ->
                val time = usageStats[appInfo.packageName]

                if (time != null && time > 0) {
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

            // 1. Calcoliamo il tempo totale sommando tutti i valori in millisecondi
            val totalTime = usedAppsList.sumOf { it.usageTimeMillis }


            // 3. Salviamo tutto nello stato. La UI reagirà automaticamente mostrandoli!
            _uiState.value = StatsUiState(
                totalScreenTimeMillis = totalTime,
                usedApps = usedAppsList,
                allInstalledApps = installedApps
            )
        }
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

        val usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
        val appUsageTimes = mutableMapOf<String, Long>()

        for ((packageName, stats) in usageStatsMap) {
            if (stats.totalTimeInForeground > 0) {
                appUsageTimes[packageName] = stats.totalTimeInForeground
            }
        }
        return appUsageTimes
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
}
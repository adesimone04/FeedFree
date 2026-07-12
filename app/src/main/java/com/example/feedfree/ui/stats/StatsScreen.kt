package com.example.feedfree.ui.stats

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType

// 1. Funzione di supporto per formattare il tempo
fun formatScreenTime(millis: Long): String {
    val totalMinutes = millis / (1000 * 60)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}

// 2. La tua schermata principale (Senza la "class")
@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    // Osserviamo lo stato dal ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedAppForPopup by remember { mutableStateOf<AppInfo?>(null) }

    val blockedApps by viewModel.blockedApps.collectAsState()
    val monitoredApps by viewModel.monitoredApps.collectAsState()
    val appTimers by viewModel.appTimers.collectAsState()

    if (!hasUsageStatsPermission(context)) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Abbiamo bisogno del tuo permesso per calcolare il tempo di utilizzo.",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                    // Manda l'utente dritto alla pagina delle impostazioni giusta!
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF85B08F), // Il colore di sfondo (verde)
                    contentColor = Color(0xFF484E5F)          // Il colore del testo e delle eventuali icone interne
                )
                ) {
                    Text("Concedi Permesso")
                }
            }
        }
        return // Fermiamo il rendering qui se manca il permesso
    }

    // Se i dati non sono ancora stati caricati, mostriamo un testo temporaneo
    if (uiState == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Caricamento...")
        }
        return
    }

    // Estraiamo lo stato
    val state = uiState!!

    // Formattiamo il tempo totale
    val timeString = formatScreenTime(state.totalScreenTimeMillis)
    val screenTimeMillis = state.totalScreenTimeMillis

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Screentime(timeString, screenTimeMillis)
        Spacer(modifier = Modifier.height(32.dp))

        Text(
        text = "Attività monitorate:",
        fontSize = 25.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start,
        color = Color.Black
    )
        Spacer(modifier = Modifier.height(16.dp))
        MonitoredAppsList(
            monitoredAppsPackages = monitoredApps,
            allApps = state.allInstalledApps,
            totalScreenTimeMillis = screenTimeMillis,
            onAppClick = { selectedAppForPopup = it }
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Applicazioni con timer:",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        TimerAppsList(
            appTimers = appTimers,
            allApps = state.allInstalledApps,
            onAppClick = { selectedAppForPopup = it }
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Applicazioni bloccate:",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        val appsBloccate = state.allInstalledApps.filter { blockedApps.contains(it.packageName) }
        if (appsBloccate.isEmpty()) {
            Text(
                text = "Nessuna applicazione bloccata.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            MyApps(
                apps = appsBloccate,
                onAppClick = { selectedAppForPopup = it }
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Le tue applicazioni:",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        MyApps(
            apps = state.allInstalledApps,
            onAppClick = { appCliccata ->
                selectedAppForPopup = appCliccata
            }
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
    selectedAppForPopup?.let { app ->
        AppBottomSheet(
            app = app,
            // ⬅️ PASSIAMO GLI STATI ATTUALI LEGGENDO DAL MOCK
            initialIsBlocked = blockedApps.contains(app.packageName),
            initialIsMonitored = monitoredApps.contains(app.packageName),
            initialTimerMillis = appTimers[app.packageName] ?: 0L,
            onDismiss = { selectedAppForPopup = null },
            onSaveConfig = { appCliccata, tabIndex, isActive, ore, minuti ->
                when (tabIndex) {
                    0 -> viewModel.setAppBlocked(appCliccata, isActive)
                    1 -> viewModel.setAppMonitored(appCliccata, isActive)
                    2 -> {
                        val timerMillis = (ore.toLongOrNull() ?: 0L) * 3600000L + (minuti.toLongOrNull() ?: 0L) * 60000L
                        viewModel.setAppTimer(appCliccata, timerMillis)
                    }
                }
            }
        )
    }
}

@Composable
fun Screentime(timeString: String, screenTimeMillis: Long) {

    // MOTORE DELL'ANIMAZIONE INFINITA (Solo per l'onda)
    val infiniteTransition = rememberInfiniteTransition(label = "wave_transition")
    val phaseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Colori e forme
    val customGreen = Color(0xFFB7CEB5)
    val trackColor = Color(0xFFA1C19F)
    val progressColor = Color(0xFF67A989)
    val pillShape = RoundedCornerShape(24.dp)

    // Calcolo progresso su 24h
    val millisIn24h = 24L * 60 * 60 * 1000
    val progressFraction = (screenTimeMillis.toFloat() / millisIn24h).coerceIn(0f, 1f)
    val sweepAngle = progressFraction * 360f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = pillShape, clip = false)
            .background(color = customGreen, shape = pillShape)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = "ScreenTime\ndi oggi:",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp,
            textAlign = TextAlign.Center,
            color = Color.Black
        )

        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ) {

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val baseRadius = (size.width / 2) - 13.dp.toPx()
                val amplitude = 6.dp.toPx()
                val frequency = 8


                // 1. SFONDO STATICO E LISCIO (Cerchio perfetto)
                if (sweepAngle < 360f) {
                    drawArc(
                        color = trackColor,
                        // Lo facciamo partire da "ore 12" (-90f) PIÙ i gradi già occupati dall'onda
                        startAngle = -90f + sweepAngle + 15f,
                        // La lunghezza del binario sarà solo lo spazio vuoto rimanente (360 - i gradi dell'onda)
                        sweepAngle = 360f - sweepAngle - 30f,
                        useCenter = false,
                        topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                        size = Size(baseRadius * 2, baseRadius * 2),
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 2. FUNZIONE ONDA (Solo per il progresso)
                fun drawWavyArc(color: Color, startAngleDeg: Float, sweepAngleDeg: Float) {
                    // Se il progresso è zero, non disegniamo l'onda per evitare pallini fluttuanti
                    if (sweepAngleDeg <= 0f) return

                    val path = Path()
                    val steps = 350
                    val startRad = Math.toRadians(startAngleDeg.toDouble()).toFloat()
                    val sweepRad = Math.toRadians(sweepAngleDeg.toDouble()).toFloat()

                    for (i in 0..steps) {
                        val fraction = i / steps.toFloat()
                        val currentAngleRad = startRad + fraction * sweepRad
                        val wavyRadius = baseRadius + amplitude * sin(frequency * currentAngleRad - phaseOffset)

                        val x = center.x + wavyRadius * cos(currentAngleRad)
                        val y = center.y + wavyRadius * sin(currentAngleRad)

                        if (i == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 3. PROGRESSO ANIMATO E ONDULATO
                drawWavyArc(color = progressColor, startAngleDeg = -90f, sweepAngleDeg = sweepAngle)
            }

            // Testi interni
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeString,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "/ 24h",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun MyApps(
    apps: List<AppInfo>,
    modifier: Modifier = Modifier,
    onAppClick: (AppInfo) -> Unit // ⬅️ NUOVO
){
    // Spostiamo qui la logica di divisione a gruppi di 5
    val chunkedApps = apps.chunked(5)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // Per ogni riga di 5 app...
        chunkedApps.forEach { rowApps ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ...disegniamo le singole app
                rowApps.forEach { app ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f) // Divide la riga in porzioni perfettamente uguali
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onAppClick(app)
                            }
                            .padding(4.dp)
                    ) {
                        // FIX: Evita il crash per le icone vettoriali
                        val safeBitmap = app.icon.toBitmap(width = 150, height = 150).asImageBitmap()

                        Image(
                            bitmap = safeBitmap,
                            contentDescription = "Icona di ${app.appName}",
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = app.appName,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                    }
                }

                // Se l'ultima riga ha meno di 5 app, inseriamo degli Spacer vuoti
                // per mantenere la griglia allineata a sinistra
                repeat(5 - rowApps.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    app: AppInfo,
    initialIsBlocked: Boolean,      // ⬅️ NUOVI PARAMETRI
    initialIsMonitored: Boolean,
    initialTimerMillis: Long,
    onDismiss: () -> Unit,
    onSaveConfig: (app: AppInfo, tabIndex: Int, isActive: Boolean, ore: String, minuti: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTab by remember { mutableStateOf(0) }

    // Stati indipendenti per ogni tab, inizializzati con i valori passati dal mock
    var isBlockedActive by remember { mutableStateOf(initialIsBlocked) }
    var isMonitoredActive by remember { mutableStateOf(initialIsMonitored) }

    // Calcolo ore e minuti per il timer
    val initialHours = initialTimerMillis / 3600000L
    val initialMins = (initialTimerMillis % 3600000L) / 60000L

    var ore by remember { mutableStateOf(if (initialHours > 0) initialHours.toString() else "0") }
    var minuti by remember { mutableStateOf(if (initialMins > 0) initialMins.toString().padStart(2, '0') else "00") }

    val sheetBgColor = Color(0xFFC3D8C2)
    val cardBgColor = Color(0xFFE2EBE0)
    val tabSelectedColor = Color(0xFF9FB99D)
    val darkText = Color(0xFF1E1E1E)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBgColor
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)
        ) {

            Text(text = "Gestisci ${app.appName}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = darkText)
            Spacer(modifier = Modifier.height(24.dp))

            Image(
                bitmap = app.icon.toBitmap(width = 200, height = 200).asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 1. SEGMENTED BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .border(BorderStroke(1.dp, Color.DarkGray), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf("Blocca", "Monitora", "Timer")
                tabs.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (selectedTab == index) tabSelectedColor else Color.Transparent)
                            .clickable { selectedTab = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = darkText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. CONTENUTO DINAMICO
            if (selectedTab == 0 || selectedTab == 1) {
                // Legge lo stato giusto in base al tab selezionato
                val isCurrentlyActive = if (selectedTab == 0) isBlockedActive else isMonitoredActive

                Switch(
                    checked = isCurrentlyActive,
                    onCheckedChange = { newState ->
                        if (selectedTab == 0) isBlockedActive = newState else isMonitoredActive = newState
                        // Salva in tempo reale nel ViewModel mockato
                        onSaveConfig(app, selectedTab, newState, "0", "0")
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = darkText, checkedTrackColor = tabSelectedColor,
                        uncheckedThumbColor = Color.Gray, uncheckedTrackColor = Color.LightGray
                    )
                )
            } else {
                // UI TIMER
                Surface(shape = RoundedCornerShape(16.dp), color = cardBgColor, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Timer giornaliero applicazione", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, modifier = Modifier.align(Alignment.Start))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            OutlinedTextField(
                                value = ore, onValueChange = { if (it.length <= 2) ore = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 28.sp),
                                modifier = Modifier.weight(1f).background(tabSelectedColor, RoundedCornerShape(8.dp)),
                                singleLine = true, shape = RoundedCornerShape(8.dp)
                            )
                            Text(" : ", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                            OutlinedTextField(
                                value = minuti, onValueChange = { if (it.length <= 2) minuti = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 28.sp),
                                modifier = Modifier.weight(1f).background(tabSelectedColor, RoundedCornerShape(8.dp)),
                                singleLine = true, shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceAround) {
                            Text("Hour", fontSize = 10.sp, color = Color.DarkGray)
                            Text("Minute", fontSize = 10.sp, color = Color.DarkGray)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            TextButton(onClick = { onDismiss() }) {
                                Text("Cancel", color = darkText, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = {
                                onSaveConfig(app, selectedTab, true, ore, minuti)
                                onDismiss()
                            }) {
                                Text("OK", color = darkText, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonitoredAppsList(
    monitoredAppsPackages: Set<String>,
    allApps: List<AppInfo>,
    totalScreenTimeMillis: Long,
    onAppClick: (AppInfo) -> Unit
) {
    // Filtriamo solo le app che l'utente ha deciso di monitorare
    val monitoredAppsList = allApps.filter { monitoredAppsPackages.contains(it.packageName) }

    if (monitoredAppsList.isEmpty()) {
        Text(
            text = "Nessuna applicazione monitorata.",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        monitoredAppsList.forEach { app ->

            // ⚠️ MOCK: Qui in futuro leggeremo il VERO tempo speso sull'app dal ViewModel.
            // Per ora fingiamo che l'utente abbia speso su questa app il 25% del tempo totale di oggi.
            val mockAppTimeMillis = if (totalScreenTimeMillis > 0) totalScreenTimeMillis / 4 else 0L
            val progress = if (totalScreenTimeMillis > 0) mockAppTimeMillis.toFloat() / totalScreenTimeMillis.toFloat() else 0f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE2EBE0)) // Sfondo verde chiaro come da mockup
                    .clickable { onAppClick(app) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icona dell'app
                val safeBitmap = app.icon.toBitmap(width = 100, height = 100).asImageBitmap()
                Image(
                    bitmap = safeBitmap,
                    contentDescription = "Icona di ${app.appName}",
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Barra di progresso e Testi
                Column(modifier = Modifier.weight(1f)) {
                    // Nome dell'app (opzionale, ma aiuta)
                    Text(
                        text = app.appName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF67A989),       // Parte riempita
                        trackColor = Color(0xFFC3D8C2)   // Parte vuota
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${formatScreenTime(mockAppTimeMillis)} / ${formatScreenTime(totalScreenTimeMillis)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun TimerAppsList(
    appTimers: Map<String, Long>,
    allApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit
) {
    // Filtriamo solo le app che hanno un timer impostato
    val timerAppsList = allApps.filter { appTimers.containsKey(it.packageName) }

    if (timerAppsList.isEmpty()) {
        Text(
            text = "Nessun timer impostato.",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        timerAppsList.forEach { app ->

            // Il tempo massimo giornaliero concesso per questa app
            val totalAllowedMillis = appTimers[app.packageName] ?: 0L

            // ⚠️ MOCK: Fingiamo che l'utente abbia consumato finora il 30% del tempo concesso.
            // Quando implementeremo il vero UsageStatsManager, questo sarà il VERO tempo usato!
            val usedTimeMillis = (totalAllowedMillis * 0.3).toLong()

            // Calcoliamo il tempo rimanente
            val remainingTimeMillis = (totalAllowedMillis - usedTimeMillis).coerceAtLeast(0L)

            // Progresso per la barra (Tempo usato / Tempo totale)
            val progress = if (totalAllowedMillis > 0) usedTimeMillis.toFloat() / totalAllowedMillis.toFloat() else 0f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE2EBE0)) // Stesso stile delle monitorate
                    .clickable { onAppClick(app) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icona dell'app
                val safeBitmap = app.icon.toBitmap(width = 100, height = 100).asImageBitmap()
                Image(
                    bitmap = safeBitmap,
                    contentDescription = "Icona di ${app.appName}",
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Barra di progresso e Testi
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        // Colore rosso scuro se il timer è quasi scaduto (>80%), altrimenti verde
                        color = if (progress > 0.8f) Color(0xFFC06C6C) else Color(0xFF67A989),
                        trackColor = Color(0xFFC3D8C2)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Testo: "Rimanente: Xh Ym / Totale: Zh Wm"
                    Text(
                        text = "${formatScreenTime(remainingTimeMillis)} rimasti su ${formatScreenTime(totalAllowedMillis)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
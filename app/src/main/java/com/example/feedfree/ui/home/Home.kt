package com.example.feedfree.ui.home

import HomeViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.rotate
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feedfree.models.CustomActivity
import com.example.feedfree.ui.badge.getDrawableRes
import com.example.feedfree.ui.profile.ProfileViewModel
import com.example.feedfree.ui.stats.StatsViewModel
import com.example.feedfree.ui.stats.formatScreenTime
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

val LightGreenBg = Color(0xFFCDE0C9)
val DarkGreenAccent = Color(0xFF8AB895)
val DarkGrayText = Color(0xFF2E2E2E)

@Composable
fun HomeScreen(
    profileViewModel: ProfileViewModel,
    statsViewModel: StatsViewModel,
    homeViewModel: HomeViewModel,
    onNavigateToBacheca: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToStats: () -> Unit,
    onActivityClick: (CustomActivity) -> Unit
) {
    val userState by profileViewModel.uiState.collectAsState()
    val statsState by statsViewModel.uiState.collectAsState()

    val allActivities by profileViewModel.activities.collectAsState()

    val completedActivities = allActivities.filter { activity ->
        if (activity.goals.isNotEmpty()) activity.goals.all { it.isCompleted } else activity.isCompleted
    }
    val pendingActivities = allActivities.filterNot { activity ->
        if (activity.goals.isNotEmpty()) activity.goals.all { it.isCompleted } else activity.isCompleted
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Bentornato, ${userState?.name ?: "Utente"}!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGrayText
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            BadgeCongratulationCard(
                completedActivities = completedActivities,
                onTrophyClick = { selectedActivity ->
                    profileViewModel.selectActivityForDetails(selectedActivity)
                    onNavigateToBacheca()
                }
            )
        }

        item {
            ActivitiesInProgressCard(
                activities = pendingActivities,
                onActivityClick = { activity ->
                    profileViewModel.selectActivityForDetails(activity)
                    onNavigateToBacheca()
                },
            )
        }

        item {
            ScreentimeCard(
                totalScreenTime = statsState?.totalScreenTimeMillis ?: 0L, onClick = onNavigateToStats
            )
        }

        item {
            BlockDistractionsCard(
                hour = homeViewModel.hour,
                minute = homeViewModel.minute,
                isActive = homeViewModel.isTimerActive,
                onTimeSelected = { h, m ->
                    homeViewModel.hour = h
                    homeViewModel.minute = m
                },
                onToggleActive = { checked ->
                    homeViewModel.isTimerActive = checked
                }
            )
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun BadgeCongratulationCard(completedActivities: List<CustomActivity>, onTrophyClick: (CustomActivity) -> Unit) {
    val recentBadges = completedActivities.takeLast(2)
    val totalBadges = completedActivities.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreenBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Congratulazioni!\nHai ottenuto $totalBadges badge totali!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGrayText
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (recentBadges.isEmpty()) {
                Text("Nessun badge recente. Completa un'attività!", color = DarkGrayText.copy(alpha = 0.7f))
            } else {
                recentBadges.forEach { activity ->
                    BadgeListItem(
                        activity = activity,
                        onClick = { onTrophyClick(activity) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun BadgeListItem(activity: CustomActivity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = activity.tier.getDrawableRes()),
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = activity.tier.name, fontSize = 12.sp, color = DarkGrayText.copy(alpha = 0.7f))
            Text(text = activity.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGrayText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Vai", tint = DarkGrayText)
    }
}

@Composable
fun ScreentimeCard(totalScreenTime: Long, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreenBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ScreenTime\ndi oggi:",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGrayText,
                lineHeight = 26.sp
            )

            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                ScreentimeAnimatedGraph(screenTimeMillis = totalScreenTime)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatScreenTime(totalScreenTime),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGrayText
                    )
                    Text(
                        text = "/ 24h",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkGrayText.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ScreentimeAnimatedGraph(screenTimeMillis: Long) {
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

    val trackColor = Color(0xFFA1C19F)
    val progressColor = Color(0xFF67A989)

    val millisIn24h = 24L * 60 * 60 * 1000
    val progressFraction = (screenTimeMillis.toFloat() / millisIn24h).coerceIn(0.05f, 1f)
    val sweepAngle = progressFraction * 360f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = (size.width / 2) - 8.dp.toPx()
        val amplitude = 4.dp.toPx()
        val frequency = 6

        if (sweepAngle < 360f) {
            drawArc(
                color = trackColor,
                startAngle = -90f + sweepAngle + 10f,
                sweepAngle = 360f - sweepAngle - 20f,
                useCenter = false,
                topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                size = Size(baseRadius * 2, baseRadius * 2),
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        if (sweepAngle > 0f) {
            val path = Path()
            val steps = 200
            val startRad = Math.toRadians(-90.0).toFloat()
            val sweepRad = Math.toRadians(sweepAngle.toDouble()).toFloat()

            for (i in 0..steps) {
                val fraction = i / steps.toFloat()
                val currentAngleRad = startRad + fraction * sweepRad
                val wavyRadius = baseRadius + amplitude * sin(frequency * currentAngleRad - phaseOffset)

                val x = center.x + wavyRadius * cos(currentAngleRad)
                val y = center.y + wavyRadius * sin(currentAngleRad)

                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = progressColor,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}


@Composable
fun BlockDistractionsCard(
    hour: Int,
    minute: Int,
    isActive: Boolean,
    onTimeSelected: (Int, Int) -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        WheelTimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onDismiss = { showDialog = false },
            onTimeSelected = { h, m ->

                onTimeSelected(h, m)
                showDialog = false
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreenBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Blocca distrazioni fino alle...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkGrayText
                )

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (isActive) Color(0xFFC88282) else Color.LightGray)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TimeBox(
                        value = hour.toString().padStart(2, '0'),
                        label = "Hour",
                        onClick = { showDialog = true }
                    )

                    Text(" : ", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = DarkGrayText, modifier = Modifier.padding(horizontal = 8.dp))

                    TimeBox(
                        value = minute.toString().padStart(2, '0'),
                        label = "Minute",
                        onClick = { showDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Switch(
                    checked = isActive,
                    onCheckedChange = { checked ->
                        onToggleActive(checked)

                        val minStr = minute.toString().padStart(2, '0')
                        val hourStr = hour.toString().padStart(2, '0')

                        val msg = if (checked) {
                            val now = java.util.Calendar.getInstance()
                            val currentH = now.get(java.util.Calendar.HOUR_OF_DAY)
                            val currentM = now.get(java.util.Calendar.MINUTE)

                            val isTomorrow = hour < currentH || (hour == currentH && minute <= currentM)

                            val suffix = if (isTomorrow) " di domani" else ""

                            "Distrazioni bloccate fino alle $hourStr:$minStr$suffix"
                        } else {
                            "Blocco distrazioni disattivato"
                        }

                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Color(0xFF757575),
                        checkedThumbColor = Color(0xFFE0E0E0),
                        uncheckedTrackColor = Color(0xFFE0E0E0),
                        uncheckedThumbColor = Color(0xFF757575)
                    )
                )
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfiniteWheelPicker(
    items: List<String>,
    initialIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 50.dp
    val visibleItemsCount = 3

    // Calcoliamo un indice di partenza "sicuro" molto alto per simulare l'infinito
    val halfVirtualCount = Int.MAX_VALUE / 2
    val startIndex = halfVirtualCount - (halfVirtualCount % items.size) + initialIndex

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex - 1)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val selectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf initialIndex

            // Troviamo l'elemento più vicino al centro
            val center = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
            val centerItem = visibleItems.minByOrNull { kotlin.math.abs(it.offset + (it.size / 2) - center) }

            centerItem?.index?.let { it % items.size } ?: initialIndex
        }
    }

    LaunchedEffect(selectedIndex) {
        onItemSelected(selectedIndex)
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier.height(itemHeight * visibleItemsCount),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(Int.MAX_VALUE) { index ->
            val actualIndex = index % items.size
            val isSelected = (actualIndex == selectedIndex)

            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = items[actualIndex],
                    fontSize = if (isSelected) 28.sp else 20.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) DarkGreenAccent else Color.Gray.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun WheelTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    // Generiamo le liste di numeri
    val hoursList = (0..23).map { it.toString().padStart(2, '0') }
    val minutesList = (0..59).map { it.toString().padStart(2, '0') }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Imposta orario", fontWeight = FontWeight.Bold, color = DarkGrayText, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rotella Ore
                InfiniteWheelPicker(
                    items = hoursList,
                    initialIndex = initialHour,
                    onItemSelected = { selectedHour = it },
                    modifier = Modifier.weight(1f)
                )

                Text(" : ", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = DarkGrayText)

                // Rotella Minuti
                InfiniteWheelPicker(
                    items = minutesList,
                    initialIndex = initialMinute,
                    onItemSelected = { selectedMinute = it },
                    modifier = Modifier.weight(1f)
                )
            }
        },
        containerColor = Color(0xFFEBEBEB),
        confirmButton = {
            TextButton(onClick = { onTimeSelected(selectedHour, selectedMinute) }) {
                Text("Salva", fontWeight = FontWeight.Bold, color = Color(0xFF67A989), fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla", color = Color.Gray, fontSize = 16.sp) }
        }
    )
}

// -----------------------------------------------------------

@Composable
fun TimeBox(value: String, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DarkGreenAccent.copy(alpha = 0.5f))
                .clickable { onClick() }
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(text = value, fontSize = 32.sp, color = DarkGrayText)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, color = DarkGrayText.copy(alpha = 0.7f))
    }
}

@Composable
fun ActivitiesInProgressCard(
    activities: List<CustomActivity>,
    onActivityClick: (CustomActivity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrow_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false),

        shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LightGreenBg)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expanded = !expanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = com.example.feedfree.R.drawable.baseline_workspace_premium_24),
                            contentDescription = null,
                            tint = DarkGrayText,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Attività in corso", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGrayText)
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Espandi o chiudi",
                        tint = DarkGrayText,
                        modifier = Modifier.rotate(rotation)
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))

                        if (activities.isEmpty()) {
                            Text(text = "Nessuna attività in corso.", fontSize = 14.sp, color = DarkGrayText)
                        } else {
                            activities.forEach { activity ->
                                ActivityNavigationItem(
                                    taskName = activity.name,
                                    onClick = { onActivityClick(activity) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }


@Composable
fun ActivityNavigationItem(
    taskName: String,
    onClick: () -> Unit // Sostituisce onCheckedChange
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = taskName,
            fontSize = 14.sp,
            color = DarkGrayText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(end = 16.dp)
        )

        // Sostituiamo tutto il Box della checkbox con una semplice Icona
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
            contentDescription = "Vai alla sezione badge dell'attività",
            tint = DarkGrayText,
            modifier = Modifier.size(24.dp)
        )
    }
}
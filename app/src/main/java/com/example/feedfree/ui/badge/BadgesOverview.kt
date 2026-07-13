package com.example.feedfree.ui.badge

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feedfree.R
import com.example.feedfree.models.CustomActivity
import com.example.feedfree.models.Tier

val LightGreenBg = Color(0xFFCDE0C9)
val DarkGreenAccent = Color(0xFF86B98F)
val DarkGrayText = Color(0xFF2E2E2E)

fun Tier.getDrawableRes(): Int {
    return when(this) {
        Tier.BRONZE -> R.drawable.bronze_trophy
        Tier.SILVER -> R.drawable.silver_trophy
        Tier.GOLD -> R.drawable.gold_trophy
        Tier.PLATINUM -> R.drawable.plat_trophy
    }
}

fun getIconForActivity(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        lower.contains("legge") -> Icons.Outlined.MenuBook
        lower.contains("studia") || lower.contains("corso") -> Icons.Outlined.Computer
        lower.contains("offline") -> Icons.Outlined.CloudOff
        else -> Icons.Outlined.TaskAlt
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesOverview(
    activities: List<CustomActivity>,
    userLevel: Int,
    onActivityClick: (CustomActivity) -> Unit,
    onHistoryClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Badges", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DarkGrayText)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = DarkGreenAccent,
                contentColor = DarkGrayText,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi")
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            val pendingActivities = activities.filterNot { activity ->
                if (activity.goals.isNotEmpty()) {
                    activity.goals.all { it.isCompleted }
                } else {
                    activity.isCompleted
                }
            }

            val overallProgress = if (pendingActivities.isNotEmpty()) {
                pendingActivities.map { activity ->
                    if (activity.goals.isNotEmpty()) {
                        activity.goals.count { it.isCompleted }.toFloat() / activity.goals.size
                    } else {
                        if (activity.isCompleted) 1f else 0f
                    }
                }.average().toFloat()
            } else {
                0f
            }

            OverallProgressCard(
                level = userLevel,
                progress = overallProgress,
                hasPendingActivities = pendingActivities.isNotEmpty(),
                onHistoryClick = onHistoryClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (pendingActivities.isEmpty()) {
                Text(
                    text = "Nessuna attività in corso.\nPremi il tasto + per aggiungerne una!",
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 32.dp, start = 16.dp, end = 16.dp)
                )
            } else {
                pendingActivities.forEach { activity ->
                    val progress = if (activity.goals.isNotEmpty()) {
                        activity.goals.count { it.isCompleted }.toFloat() / activity.goals.size
                    } else {
                        if (activity.isCompleted) 1f else 0f
                    }

                    ActivitySummaryCard(
                        title = activity.name,
                        icon = getIconForActivity(activity.name),
                        progress = progress,
                        rewardTier = activity.tier,
                        onClick = { onActivityClick(activity) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun OverallProgressCard(
    level: Int,
    progress: Float,
    hasPendingActivities: Boolean,
    onHistoryClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreenBg)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(id = R.drawable.baseline_workspace_premium_24), contentDescription = "Livello", modifier = Modifier.size(28.dp), tint = DarkGrayText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Livello $level", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkGrayText)
                }

                Button(
                    onClick = onHistoryClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreenAccent),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Outlined.EmojiEvents, contentDescription = "Bacheca", modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bacheca", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val labelText = if (hasPendingActivities) "Progresso attività in corso" else "Nessuna attività in corso"

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = labelText, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DarkGrayText)
                Text(text = "${(progress * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkGrayText)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = DarkGreenAccent, trackColor = Color.White, strokeCap = StrokeCap.Round, gapSize = 0.dp, drawStopIndicator = {}
            )
        }
    }
}

@Composable
fun ActivitySummaryCard(
    title: String,
    icon: ImageVector,
    progress: Float,
    rewardTier: Tier,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreenBg)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(28.dp), tint = DarkGrayText)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkGrayText, modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(70.dp)) {
                Image(
                    painter = painterResource(id = rewardTier.getDrawableRes()),
                    contentDescription = "Reward",
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = DarkGreenAccent, trackColor = Color.White, strokeCap = StrokeCap.Round, gapSize = 0.dp, drawStopIndicator = {}
                    )
                }
            }
        }
    }
}
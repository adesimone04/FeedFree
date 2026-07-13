package com.example.feedfree.ui.badge

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feedfree.R
import com.example.feedfree.models.CustomActivity
import com.example.feedfree.models.Tier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrophyHistoryScreen(
    activities: List<CustomActivity>,
    onBackClick: () -> Unit,
    onActivityClick: (CustomActivity) -> Unit
) {
    val completedActivities = activities.filter { activity ->
        if (activity.goals.isNotEmpty()) {
            activity.goals.all { it.isCompleted }
        } else {
            activity.isCompleted
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "La mia Bacheca", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "Indietro", tint = DarkGrayText) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
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

            EarnedTrophiesSummaryCard(completedActivities)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Attività Completate",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGrayText,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp, start = 8.dp)
            )

            if (completedActivities.isEmpty()) {
                Text(
                    text = "Non hai ancora completato nessuna attività. I trofei sbloccati appariranno qui!",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 32.dp, start = 16.dp, end = 16.dp)
                )
            } else {
                completedActivities.forEach { activity ->
                    val rewardTier = activity.tier

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { onActivityClick(activity) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = LightGreenBg)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(painter = painterResource(id = rewardTier.getDrawableRes()), contentDescription = "Trofeo Guadagnato", modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = activity.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGrayText)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Completata al 100%", fontSize = 12.sp, color = DarkGreenAccent, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun EarnedTrophiesSummaryCard(completedActivities: List<CustomActivity>) {
    val platCount = completedActivities.count { it.tier == Tier.PLATINUM }
    val goldCount = completedActivities.count { it.tier == Tier.GOLD }
    val silverCount = completedActivities.count { it.tier == Tier.SILVER }
    val bronzeCount = completedActivities.count { it.tier == Tier.BRONZE }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreenBg)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = "Totale Trofei Sbloccati",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGrayText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrophyCounter(imageRes = R.drawable.plat_trophy, count = platCount)
                TrophyCounter(imageRes = R.drawable.gold_trophy, count = goldCount)
                TrophyCounter(imageRes = R.drawable.silver_trophy, count = silverCount)
                TrophyCounter(imageRes = R.drawable.bronze_trophy, count = bronzeCount)
            }
        }
    }
}

@Composable
fun TrophyCounter(imageRes: Int, count: Int) {
    val alpha = if (count > 0) 1f else 0.4f

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Trofeo",
            modifier = Modifier.size(48.dp),
            alpha = alpha
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = count.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (count > 0) DarkGrayText else Color.Gray
        )
    }
}
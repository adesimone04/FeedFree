package com.example.feedfree.ui.badge

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feedfree.models.CustomActivity
import com.example.feedfree.models.Tier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetail(
    activity: CustomActivity,
    onBackClick: () -> Unit,
    onActivityChange: (CustomActivity) -> Unit,
    onEditClick: () -> Unit // Nuovo parametro
) {
    val progress = if (activity.goals.isNotEmpty()) {
        activity.goals.count { it.isCompleted }.toFloat() / activity.goals.size
    } else {
        if (activity.isCompleted) 1f else 0f
    }

    val rewardTier = activity.tier

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = activity.name, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "Indietro", tint = DarkGrayText) }
                },
                actions = { // Aggiunto il pulsante di modifica
                    IconButton(onClick = onEditClick) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Modifica", tint = DarkGrayText)
                    }
                },

            )
        },

    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            DetailProgressCard(progress = progress, rewardTier = rewardTier)

            Spacer(modifier = Modifier.height(16.dp))

            activity.goals.forEachIndexed { index, goal ->
                GoalCard(
                    title = goal.name,
                    isChecked = goal.isCompleted,
                    onCheckedChange = { isNowChecked ->
                        val updatedGoals = activity.goals.toMutableList()
                        updatedGoals[index] = goal.copy(isCompleted = isNowChecked)

                        val allCompleted = updatedGoals.isNotEmpty() && updatedGoals.all { it.isCompleted }

                        val updatedActivity = activity.copy(
                            goals = updatedGoals,
                            isCompleted = allCompleted || (updatedGoals.isEmpty() && isNowChecked)
                        )

                        onActivityChange(updatedActivity)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun DetailProgressCard(progress: Float, rewardTier: Tier) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreenBg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Progresso:", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkGrayText)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = DarkGreenAccent, trackColor = Color.White, strokeCap = StrokeCap.Butt, gapSize = 0.dp, drawStopIndicator = {}
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Image(painter = painterResource(id = rewardTier.getDrawableRes()), contentDescription = null, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
fun GoalCard(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreenBg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = DarkGrayText, checkmarkColor = Color.White, uncheckedColor = DarkGrayText)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, fontSize = 14.sp, color = DarkGrayText, modifier = Modifier.weight(1f))
        }
    }
}
package com.example.feedfree.ui.badge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feedfree.models.CustomActivity
import com.example.feedfree.models.Goal
import com.example.feedfree.models.Tier
import com.example.feedfree.ui.profile.ProfileViewModel
import java.util.UUID
import androidx.compose.ui.window.Dialog

@Composable
fun BadgesMainScreen(viewModel: ProfileViewModel) {
    // Leggiamo gli stati dal ViewModel condiviso
    val activities by viewModel.activities.collectAsState()
    val selectedActivity by viewModel.selectedActivityForDetails.collectAsState()
    val userState by viewModel.uiState.collectAsState()

    var showHistory by remember { mutableStateOf(false) }
    var showActivityForm by remember { mutableStateOf(false) }
    var activityToEdit by remember { mutableStateOf<CustomActivity?>(null) }

    val completedActivities = activities.filter { activity ->
        if (activity.goals.isNotEmpty()) activity.goals.all { it.isCompleted } else activity.isCompleted
    }

    val calculatedPoints = completedActivities.sumOf { activity ->
        when (activity.tier) {
            Tier.PLATINUM -> 1000
            Tier.GOLD -> 500
            Tier.SILVER -> 200
            Tier.BRONZE -> 100
        }
    }

    val calculatedLevel = maxOf(0, calculatedPoints / 1000)

    when {
        selectedActivity != null -> {
            ActivityDetail(
                activity = selectedActivity!!,
                onBackClick = {
                    viewModel.selectActivityForDetails(null)
                },
                onActivityChange = { updatedActivity ->
                    viewModel.updateActivity(updatedActivity)
                },
                onEditClick = {
                    activityToEdit = selectedActivity
                    showActivityForm = true
                }
            )
        }
        showHistory -> {
            TrophyHistoryScreen(
                activities = activities,
                onBackClick = { showHistory = false },
                onActivityClick = { activity -> viewModel.selectActivityForDetails(activity) }
            )
        }
        else -> {
            BadgesOverview(
                activities = activities,
                userLevel = calculatedLevel,
                onActivityClick = { activity -> viewModel.selectActivityForDetails(activity) },
                onHistoryClick = { showHistory = true },
                onAddClick = {
                    activityToEdit = null
                    showActivityForm = true
                }
            )
        }
    }

    if (showActivityForm) {
        ActivityFormDialog(
            initialActivity = activityToEdit,
            onDismiss = {
                showActivityForm = false
                activityToEdit = null
            },
            onSave = { savedActivity ->
                // Gestisce sia la modifica che l'eventuale aggiornamento
                viewModel.updateActivity(savedActivity)
                showActivityForm = false
                activityToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityFormDialog(
    initialActivity: CustomActivity?,
    onDismiss: () -> Unit,
    onSave: (CustomActivity) -> Unit
) {
    var name by remember { mutableStateOf(initialActivity?.name ?: "") }
    var description by remember { mutableStateOf(initialActivity?.description ?: "") }
    var selectedTier by remember { mutableStateOf(initialActivity?.tier ?: Tier.BRONZE) }
    var goals by remember { mutableStateOf(initialActivity?.goals ?: listOf(Goal(UUID.randomUUID().toString(), "", false))) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (initialActivity == null) "Nuova Attività" else "Modifica Attività",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGrayText,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // CAMPI DI TESTO PRINCIPALI
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Attività", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkGreenAccent,
                        focusedLabelColor = DarkGreenAccent,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrizione (opzionale)", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkGreenAccent,
                        focusedLabelColor = DarkGreenAccent,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // SELETTORE TROFEO ELEGANTE
                Text("Scegli il Trofeo:", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkGrayText)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Tier.values().forEach { tier ->
                        val isSelected = selectedTier == tier
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) LightGreenBg else Color.Transparent)
                                .clickable { selectedTier = tier },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = tier.getDrawableRes()),
                                contentDescription = tier.name,
                                modifier = Modifier.size(if (isSelected) 36.dp else 28.dp),
                                alpha = if (isSelected) 1f else 0.5f
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // LISTA DELLE TASK
                Text("Task:", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkGrayText)
                Spacer(modifier = Modifier.height(8.dp))

                goals.forEachIndexed { index, goal ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = goal.name,
                            onValueChange = { newName ->
                                goals = goals.toMutableList().apply {
                                    this[index] = goal.copy(name = newName)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("Descrizione Task", fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreenAccent,
                                focusedLabelColor = DarkGreenAccent,
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )

                        IconButton(
                            onClick = { goals = goals.toMutableList().apply { removeAt(index) } },
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Rimuovi Task",
                                tint = Color(0xFFB0B0B0) // Grigio al posto del rosso aggressivo
                            )
                        }
                    }
                }

                TextButton(
                    onClick = { goals = goals + Goal(UUID.randomUUID().toString(), "", false) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+ Aggiungi Task", color = DarkGreenAccent, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // PULSANTI DI AZIONE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Annulla", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val newActivity = CustomActivity(
                                id = initialActivity?.id ?: UUID.randomUUID().toString(),
                                name = name,
                                description = description,
                                isCompleted = initialActivity?.isCompleted ?: false,
                                goals = goals.filter { it.name.isNotBlank() },
                                tier = selectedTier
                            )
                            onSave(newActivity)
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkGreenAccent,
                            disabledContainerColor = LightGreenBg
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Salva", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
        }
    }
}
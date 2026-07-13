package com.example.feedfree.ui.badge

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.feedfree.data.MockRepository
import com.example.feedfree.models.CustomActivity
import com.example.feedfree.models.Goal
import com.example.feedfree.models.Tier
import com.example.feedfree.ui.profile.ProfileViewModel
import java.util.UUID

@Composable
fun BadgesMainScreen(viewModel: ProfileViewModel) {
    var selectedActivity by remember { mutableStateOf<CustomActivity?>(null) }
    var showHistory by remember { mutableStateOf(false) }

    // Gestione Form Modifica/Aggiunta
    var showActivityForm by remember { mutableStateOf(false) }
    var activityToEdit by remember { mutableStateOf<CustomActivity?>(null) }

    // STATO CENTRALE
    var activities by remember { mutableStateOf<List<CustomActivity>>(emptyList()) }
    var userLevel by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }

    // Carichiamo TUTTI i dati solo all'avvio (una volta sola)
    LaunchedEffect(Unit) {
        val user = MockRepository.getCurrentUser()
        userLevel = user.level
        activities = MockRepository.getCustomActivities()
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = DarkGreenAccent)
        }
    } else {
        when {
            selectedActivity != null -> {
                ActivityDetail(
                    activity = selectedActivity!!,
                    onBackClick = { selectedActivity = null },
                    onActivityChange = { updatedActivity ->
                        activities = activities.map {
                            if (it.id == updatedActivity.id) updatedActivity else it
                        }
                        selectedActivity = updatedActivity
                    },
                    onEditClick = { // Passiamo la logica di modifica
                        activityToEdit = selectedActivity
                        showActivityForm = true
                    }
                )
            }
            showHistory -> {
                TrophyHistoryScreen(
                    activities = activities,
                    onBackClick = { showHistory = false },
                    onActivityClick = { activity -> selectedActivity = activity }
                )
            }
            else -> {
                BadgesOverview(
                    activities = activities,
                    userLevel = userLevel,
                    onActivityClick = { activity -> selectedActivity = activity },
                    onHistoryClick = { showHistory = true },
                    onAddClick = {
                        activityToEdit = null
                        showActivityForm = true
                    }
                )
            }
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
                if (activityToEdit == null) {
                    activities = activities + savedActivity // Aggiunta
                } else {
                    activities = activities.map { if (it.id == savedActivity.id) savedActivity else it } // Modifica
                }
                showActivityForm = false
                activityToEdit = null
            }
        )
    }
}

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialActivity == null) "Nuova Attività" else "Modifica Attività") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Attività") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrizione (opzionale)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Scegli il Trofeo:")
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Tier.values().forEach { tier ->
                        val isSelected = selectedTier == tier
                        IconButton(onClick = { selectedTier = tier }) {
                            Image(
                                painter = painterResource(id = tier.getDrawableRes()),
                                contentDescription = tier.name,
                                modifier = Modifier.size(if (isSelected) 40.dp else 28.dp),
                                alpha = if (isSelected) 1f else 0.4f
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Task:")
                goals.forEachIndexed { index, goal ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = goal.name,
                            onValueChange = { newName ->
                                goals = goals.toMutableList().apply {
                                    this[index] = goal.copy(name = newName)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("Descrizione Task") }
                        )
                        IconButton(onClick = {
                            goals = goals.toMutableList().apply { removeAt(index) }
                        }) {
                            Icon(Icons.Default.Delete, "Rimuovi Task", tint = Color.Red)
                        }
                    }
                }
                TextButton(onClick = {
                    goals = goals + Goal(UUID.randomUUID().toString(), "", false)
                }) {
                    Text("+ Aggiungi Task")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newActivity = CustomActivity(
                        id = initialActivity?.id ?: UUID.randomUUID().toString(),
                        name = name,
                        description = description,
                        isCompleted = initialActivity?.isCompleted ?: false,
                        goals = goals.filter { it.name.isNotBlank() }, // Evitiamo task vuote
                        tier = selectedTier
                    )
                    onSave(newActivity)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}
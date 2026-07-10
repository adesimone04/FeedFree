package com.example.feedfree.ui.badge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.StrokeCap
import com.example.feedfree.R
import com.example.feedfree.data.MockRepository
import com.example.feedfree.models.Badges
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesPage(
    onBackClick: () -> Unit
) {
    // Stato per contenere la lista dei badges. Inizia vuota.
    var userBadges by remember { mutableStateOf<List<Badges>>(emptyList()) }
    // Stato per il caricamento, utile se il delay del mock è lungo
    var isLoading by remember { mutableStateOf(true) }

    // LaunchedEffect viene eseguito quando il Composable entra nello schermo.
    // Qui andiamo a pescare l'utente dal MockRepository in background.
    LaunchedEffect(Unit) {
        val user = MockRepository.getCurrentUser() // Recupera l'utente mockato
        userBadges = user.badges // Estrae la lista dei trofei[cite: 7]
        isLoading = false // Fine caricamento
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Attività x",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Indietro",
                            tint = DarkGrayText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->

        // Se sta caricando, mostra una rotellina
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkGreenAccent)
            }
        } else {
            // Se ha finito di caricare, mostra i dati
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                DetailProgressCard(badgesList = userBadges)

                Spacer(modifier = Modifier.height(16.dp))

                // Genera la lista in base ai dati recuperati dal mock
                userBadges.forEach { badge ->
                    GoalCard(
                        title = badge.name, // Es: "Pioniere"
                        imageRes = badge.type.getDrawableRes(), // Usa la funzione per abbinare il PNG
                        showCheckbox = false
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun DetailProgressCard(badgesList: List<Badges>) {
    // Conta i trofei per categoria
    val goldCount = badgesList.count { it.type == Tier.GOLD }
    val silverCount = badgesList.count { it.type == Tier.SILVER }
    val bronzeCount = badgesList.count { it.type == Tier.BRONZE }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreenBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Progresso:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkGrayText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { 0.55f },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = DarkGreenAccent,
                        trackColor = Color.White,
                        strokeCap = StrokeCap.Butt,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "55%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniTrophyWithCount(imageRes = R.drawable.gold_trophy, count = goldCount.toString())
                MiniTrophyWithCount(imageRes = R.drawable.silver_trophy, count = silverCount.toString())
                MiniTrophyWithCount(imageRes = R.drawable.bronze_trophy, count = bronzeCount.toString())
            }
        }
    }
}

@Composable
fun MiniTrophyWithCount(imageRes: Int, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Trofeo",
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = count, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun GoalCard(
    title: String,
    imageRes: Int,
    showCheckbox: Boolean,
    initialCheckState: Boolean = false
) {
    val (isChecked, setChecked) = remember { mutableStateOf(initialCheckState) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreenBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Icona Trofeo",
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                color = DarkGrayText,
                modifier = Modifier.weight(1f)
            )

            if (showCheckbox) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { setChecked(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = DarkGrayText,
                        checkmarkColor = Color.White,
                        uncheckedColor = DarkGrayText
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BadgesPagePreview() {
    BadgesPage(onBackClick = { })
}
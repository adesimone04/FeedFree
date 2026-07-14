package com.example.feedfree.ui.profile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feedfree.R
import com.example.feedfree.models.CustomActivity
import com.example.feedfree.models.Tier
import com.example.feedfree.models.User
import com.example.feedfree.ui.badge.getDrawableRes

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val userState by viewModel.uiState.collectAsState()
    // Sincronizziamo la pagina con lo stato dinamico delle attività globale
    val allActivities by viewModel.activities.collectAsState()

    if (userState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Caricamento in corso...", fontSize = 16.sp, color = Color.Gray)
        }
        return
    }

    val user = userState!!

    // Calcolo delle attività realmente completate
    val completedActivities = allActivities.filter { activity ->
        if (activity.goals.isNotEmpty()) activity.goals.all { it.isCompleted } else activity.isCompleted
    }

    // Filtro per i trofei di Platino
    val platinumActivities = completedActivities.filter { it.tier == Tier.PLATINUM }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileHeader(user = user)

        LevelBanner(user = user)

        // Passiamo il vero numero di trofei completati
        UserStatsRow(user = user, totalTrophies = completedActivities.size)

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FriendsLeaderboard(user = user)

            Spacer(modifier = Modifier.height(24.dp))

            // La card viene mostrata SOLO se ci sono attività Platino completate
            if (platinumActivities.isNotEmpty()) {
                Platinums(platinumActivities = platinumActivities)
                Spacer(modifier = Modifier.height(40.dp))
            } else {
                // Spazio extra in basso se la card dei platini è nascosta
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader(user: User) {
    val context = LocalContext.current
    val customGreen = Color(0xFF91C09C)
    val partialPillShape = RoundedCornerShape(
        bottomStart = 24.dp,
        bottomEnd = 24.dp
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = partialPillShape, clip = false)
            .background(color = customGreen, shape = partialPillShape)
            .padding(25.dp)
    ) {
        IconButton(
            onClick = {
                // Feedback per schermata non ancora implementata
                Toast.makeText(context, "Impostazioni in arrivo nelle prossime versioni", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Impostazioni",
                tint = Color.Black
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                shadowElevation = 8.dp
            ) {
                val avatarImage = user.avatarResId ?: R.drawable.ic_launcher_foreground

                Image(
                    painter = painterResource(id = avatarImage),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = user.username,
                fontSize = 16.sp,
                color = Color(0xFF2E2E2E)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LevelBanner(user: User) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .offset(y = (-24).dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false)
            .background(Color(0xFFB7CEB5), shape = RoundedCornerShape(24.dp))
            .fillMaxWidth(0.9f)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Icona Livello",
            tint = Color(0xFF2E2E2E),
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Livello ${user.level}",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF2E2E2E)
        )
    }
}

@Composable
fun UserStatsRow(user: User, totalTrophies: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .offset(y = (-12).dp)
            .background(Color(0xFFE2EBE0), RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(label = "Punti", value = user.points.toString())

        Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color.Gray.copy(alpha = 0.3f)))

        StatItem(label = "Amici", value = (user.friends?.size ?: 0).toString())

        Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color.Gray.copy(alpha = 0.3f)))

        StatItem(label = "Trofei", value = totalTrophies.toString()) // Utilizza il dato dinamico
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E2E2E))
    }
}

@Composable
fun FriendsLeaderboard(user: User) {
    val customGreen = Color(0xFFB7CEB5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false)
            .background(color = customGreen, shape = RoundedCornerShape(24.dp))
            .padding(25.dp)
    ) {
        Text(
            text = "Classifica Amici",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E2E2E),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val sortedFriends = (user.friends ?: emptyList()).sortedByDescending { it.points }

        if (sortedFriends.isEmpty()) {
            Text("Nessun amico aggiunto.", color = Color.DarkGray, fontSize = 14.sp)
        } else {
            sortedFriends.forEachIndexed { index, amico ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "${index + 1}°",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E2E2E),
                        modifier = Modifier.width(30.dp)
                    )

                    val friendAvatar = amico.avatarResId ?: R.drawable.ic_launcher_foreground
                    Image(
                        painter = painterResource(id = friendAvatar),
                        contentDescription = "Immagine di ${amico.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = amico.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Livello ${amico.level} • ${amico.points} pt",
                            fontSize = 14.sp,
                            color = Color(0xFF4A4A4A),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Platinums(platinumActivities: List<CustomActivity>) {
    val customGreen = Color(0xFFB7CEB5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false)
            .background(color = customGreen, shape = RoundedCornerShape(24.dp))
            .padding(25.dp),
        horizontalAlignment = Alignment.Start // Allineamento coerente con la leaderboard
    ) {
        Text(
            text = "Platini Ottenuti",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E2E2E)
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(platinumActivities) { activity ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // Usiamo una larghezza fissa in modo che i testi lunghi vadano accapo bene
                    modifier = Modifier.width(90.dp)
                ) {
                    Image(
                        painter = painterResource(id = activity.tier.getDrawableRes()),
                        contentDescription = "Badge Platino",
                        modifier = Modifier.size(56.dp) // Leggermente ingrandito per spiccare
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = activity.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E2E2E),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp, // Migliora la spaziatura quando il testo va accapo
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(viewModel = androidx.lifecycle.viewmodel.compose.viewModel())
}
package com.example.feedfree.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feedfree.R
import com.example.feedfree.models.Tier

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    // Osserviamo i dati dal ViewModel in tempo reale
    val userState by viewModel.uiState.collectAsState()

    // Se i dati non sono ancora arrivati (i famosi 800ms di delay), mostriamo un caricamento
    if (userState == null) {
        Text("Caricamento in corso...", modifier = Modifier.padding(16.dp))
        return
    }

    val user = userState!!

    // Inizio della grafica
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        ProfileHeader(user = user)

        Level(user = user)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                // ECCO IL PADDING LATERALE PER GLI ALTRI BLOCCHI:
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            FriendsLeaderboard(user = user)

            Spacer(modifier = Modifier.height(32.dp))

            Platinums(user = user)

            Spacer(modifier = Modifier.height(32.dp))

        }
    }
}

@Composable
fun ProfileHeader(user: com.example.feedfree.models.User) {
    // Definizione del colore verde e della forma personalizzata
    val customGreen = Color(0xFF91C09C) // Il tuo colore verde
    val partialPillShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = 24.dp, // Regola il raggio qui per la curvatura in basso
        bottomEnd = 24.dp
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = partialPillShape,
                clip = false
            )
            .background(color = customGreen, shape = partialPillShape)
            .padding(25.dp) // Il padding vale per tutto ciò che è dentro il Box
    ) {

        // 2. Tasto Impostazioni (Allineato in alto a destra)
        IconButton(
            onClick = { /* TODO: Aggiungi navigazione o azione impostazioni */ },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings, // Usa l'icona di default di Material
                contentDescription = "Impostazioni",
                tint = Color.Black // Colore dell'icona
            )
        }

        // 3. La tua Column originale che centra l'avatar e il testo
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                shadowElevation = 8.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
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
                color = Color.DarkGray // Ho scurito leggermente rispetto a Gray per maggiore leggibilità
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun Level(user: com.example.feedfree.models.User){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            // 1. IL SEGRETO DELL'OVERLAP: Sposta la card verso l'alto (regola il valore a piacimento)
            .offset(y = (-24).dp)
            // 2. Ombra con angoli più ampi (24.dp si avvicina di più al design in foto)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false
            )
            // 3. Sfondo
            .background(Color(0xFFB7CEB5), shape = RoundedCornerShape(24.dp))
            // 4. Larghezza e padding
            .fillMaxWidth(0.9f) // Rende la card larga il 90% dello schermo (come in foto)
            .padding(horizontal = 24.dp, vertical = 30.dp) // Spazio interno della card
    ) {

        // Icona della medaglia (Sostituisci R.drawable.ic_medal con l'ID della tua icona)
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Icona Livello",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Testo del livello
        Text(
            text = "Livello ${user.points/100}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun FriendsLeaderboard(user: com.example.feedfree.models.User){

    // Definizione del colore verde e della forma personalizzata
    val customGreen = Color(0xFFB7CEB5) // Il tuo colore verde
    val partialPillShape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 24.dp, // Regola il raggio qui per la curvatura in basso
        bottomEnd = 24.dp
    )

    // Contenitore principale a Colonna con lo sfondo e la forma personalizzati
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false // Evita che l'ombra venga tagliata
            )
            .background(color = customGreen, shape = partialPillShape)
            .padding(25.dp), // Padding interno per distanziare gli elementi dai bordi
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 2. Per ogni amico
        user.friends?.forEach { amico ->
            // Creiamo una riga per disporre l'immagine a sinistra e i testi a destra
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {

                // IMMAGINE
                // Nota: Se l'immagine viene da un URL, ti consiglio di usare la libreria Coil con AsyncImage.
                // Qui uso Image con painterResource assumendo che sia una risorsa locale (es. R.drawable.avatar).
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Immagine di ${amico.name}",
                    modifier = Modifier.size(50.dp)
                )

                Spacer(modifier = Modifier.width(16.dp)) // Spazio tra immagine e testo
                // NOME E PUNTI (Incolonnati verticalmente)
                Column {
                    // Nome
                    Text(
                        text = amico.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black // Leggibile sul verde
                    )

                    // Punti
                    Text(
                        text = "Livello: ${amico.points/100}",
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun Platinums(user: com.example.feedfree.models.User) {

    val customGreen = Color(0xFFB7CEB5)
    val partialPillShape = RoundedCornerShape(24.dp) // Forma arrotondata su tutti i lati

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false // Evita che l'ombra venga tagliata
            )
            .background(color = customGreen, shape = partialPillShape)
            .padding(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Titolo opzionale per la sezione
        Text(
            text = "Platini Ottenuti",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Filtriamo la lista per tenere solo i Platini.
        // Usiamo elvis operator (?: emptyList()) nel caso badges sia null
        val platinumBadges = user.badges?.filter { it.type == Tier.PLATINUM } ?: emptyList()

        if (platinumBadges.isEmpty()) {
            // Se non ha platini, possiamo mostrare un messaggio
            Text(text = "Nessun platino ottenuto ancora.", color = Color.DarkGray)
        } else {
            // 2. Usiamo LazyRow per lo scroll orizzontale
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                // horizontalArrangement distanzia automaticamente gli elementi tra loro
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Iteriamo sulla lista filtrata
                items(platinumBadges) { badge ->
                    // Design del singolo Badge di platino
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Immagine del badge (Sostituisci badge.imageId con la proprietà corretta)
                        // Se l'immagine viene da internet, ricorda di usare AsyncImage (Coil)
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Badge Platino",
                            modifier = Modifier.size(60.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Nome del gioco o del badge
                        Text(
                            text = badge.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    // Creiamo "al volo" un'istanza del ViewModel solo per far felice l'anteprima
    ProfileScreen(viewModel = androidx.lifecycle.viewmodel.compose.viewModel())
}

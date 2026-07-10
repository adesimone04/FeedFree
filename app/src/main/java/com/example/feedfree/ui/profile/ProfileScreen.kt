package com.example.feedfree.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feedfree.R

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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileHeader(user = user)

        Spacer(modifier = Modifier.height(12.dp))

        // 3. La "Pillola" Verde dei punti (Sostituisce il Drawable XML)
        Text(
            text = "Livello ${user.points/100}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .background(Color(0xFFB7CEB5), shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Lista dei Badge (Sostituisce la RecyclerView)
        Text(
            text = "I Miei Badge",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(user.badges) { badge ->
                // Qui disegneremo la riga del singolo badge
                Text(text = "🏅 ${badge.name}", modifier = Modifier.padding(vertical = 8.dp))
            }
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

    // Contenitore principale a Colonna con lo sfondo e la forma personalizzati
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = customGreen, shape = partialPillShape)
            .padding(25.dp), // Padding interno per distanziare gli elementi dai bordi
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Avatar Circolare con ombra
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            shadowElevation = 8.dp
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Sostituisci con la tua risorsa avatar
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Nome dell'Utente
        Text(
            text = user.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black // Assicurati che il colore sia leggibile sul verde
        )

        // 3. Username
        Text(
            text = user.username,
            fontSize = 16.sp,
            color = Color.Gray // O un colore simile che si distingua
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    // Creiamo "al volo" un'istanza del ViewModel solo per far felice l'anteprima
    ProfileScreen(viewModel = androidx.lifecycle.viewmodel.compose.viewModel())
}

package com.example.feedfree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.feedfree.ui.profile.ProfileScreen
import com.example.feedfree.ui.profile.ProfileViewModel
import com.example.feedfree.ui.theme.FeedFreeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FeedFreeTheme {
                FeedFreeApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun FeedFreeApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val profileViewModel: ProfileViewModel = viewModel()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> {
                    // Schermata temporanea per la Home
                    Greeting(
                        name = "Home Screen",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                AppDestinations.FAVORITES -> {
                    // Schermata temporanea per i Preferiti
                    Greeting(
                        name = "Favorites Screen",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                AppDestinations.PROFILE -> {
                    // 3. ECCO LA TUA PAGINA!
                    // Wrappiamo la schermata in un Box e gli passiamo 'innerPadding'.
                    // Questo è fondamentale affinché la tua ProfileScreen non finisca
                    // graficamente "sotto" la barra di navigazione in basso.
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ProfileScreen(viewModel = profileViewModel)
                    }
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    FAVORITES("Favorites", R.drawable.ic_favorite),
    PROFILE("Profile", R.drawable.ic_account_box),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FeedFreeTheme {
        Greeting("Android")
    }
}
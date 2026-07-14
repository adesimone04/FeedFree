package com.example.feedfree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.feedfree.ui.badge.BadgesOverview
import com.example.feedfree.ui.badge.BadgesMainScreen
import com.example.feedfree.ui.home.HomeScreen
import com.example.feedfree.ui.profile.ProfileScreen
import com.example.feedfree.ui.profile.ProfileViewModel
import com.example.feedfree.ui.stats.StatsScreen
import com.example.feedfree.ui.stats.StatsViewModel
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

    val customItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            indicatorColor = Color(0xFF8AB895) // Il tuo verde personalizzato
        )
    )

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
                    onClick = { currentDestination = it },
                    colors = customItemColors
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> {
                    val profileViewModel: ProfileViewModel = viewModel()
                    val statsViewModel: StatsViewModel = viewModel()

                    Box(modifier = Modifier.padding(innerPadding)) {
                        HomeScreen(
                            profileViewModel = profileViewModel,
                            statsViewModel = statsViewModel,
                            onNavigateToBacheca = {
                                // Questo cambia la destinazione della bottom bar simulando il tap
                                currentDestination = AppDestinations.BADGE
                            }
                        )
                    }
                }
                AppDestinations.BADGE -> {
                    val profileViewModel: ProfileViewModel = viewModel()
                    Box(modifier = Modifier.padding(innerPadding)) {

                        BadgesMainScreen(viewModel = profileViewModel)
                    }
                }
                AppDestinations.STATISTICHE -> {
                    // ViewModel inizializzato solo quando serve
                    val statsViewModel: StatsViewModel = viewModel()
                    Box(modifier = Modifier.padding(innerPadding)) {
                        StatsScreen(viewModel = statsViewModel)
                    }
                }
                AppDestinations.PROFILE -> {
                    // ViewModel inizializzato solo quando serve
                    val profileViewModel: ProfileViewModel = viewModel()
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
    HOME("Home", R.drawable.baseline_home_24),
    BADGE("Badges", R.drawable.baseline_workspace_premium_24),
    STATISTICHE("Statistiche", R.drawable.outline_bar_chart_24),
    PROFILE("Profilo", R.drawable.baseline_account_circle_24),
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
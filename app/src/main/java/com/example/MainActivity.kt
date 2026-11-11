package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Destination
import com.example.ui.AppNavTab
import com.example.ui.NomadikaViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SafariGold
import com.example.ui.theme.SavannahGreen
import com.example.ui.theme.TerracottaPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NomadikaMainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NomadikaMainApp(viewModel: NomadikaViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedDestination by viewModel.selectedDestination.collectAsState()
    val savedDestinations by viewModel.savedDestinations.collectAsState()
    val groupTrips by viewModel.groupTrips.collectAsState()

    var prefilledItineraryDestination by remember { mutableStateOf<Destination?>(null) }

    // Intercept back button when in detail view or secondary screens
    BackHandler(enabled = selectedDestination != null || currentTab != AppNavTab.EXPLORE) {
        if (selectedDestination != null) {
            viewModel.selectDestination(null)
        } else if (currentTab != AppNavTab.EXPLORE) {
            viewModel.setTab(AppNavTab.EXPLORE)
        }
    }

    if (selectedDestination != null) {
        DestinationDetailScreen(
            destination = selectedDestination!!,
            viewModel = viewModel,
            onBack = { viewModel.selectDestination(null) },
            onOpenBudget = {
                viewModel.selectDestination(null)
                viewModel.setTab(AppNavTab.BUDGET)
            },
            onOpenItineraryBuilder = { dest ->
                prefilledItineraryDestination = dest
                viewModel.selectDestination(null)
                viewModel.setTab(AppNavTab.ITINERARIES)
            }
        )
    } else {
        Scaffold(
            topBar = {
                if (currentTab != AppNavTab.EXPLORE) {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (currentTab) {
                                    AppNavTab.ITINERARIES -> "Trip Itineraries"
                                    AppNavTab.BUDGET -> "Budget Estimator"
                                    AppNavTab.MAP -> "Interactive Kenya Map"
                                    AppNavTab.OPERATORS -> "Local Tour Operators"
                                    AppNavTab.GROUP_TRIPS -> "Groups & Wishlist"
                                    else -> "Nomadika"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.setTab(AppNavTab.EXPLORE) }) {
                                Icon(Icons.Default.Home, contentDescription = "Home")
                            }
                        },
                        actions = {
                            if (currentTab != AppNavTab.GROUP_TRIPS) {
                                IconButton(
                                    onClick = { viewModel.setTab(AppNavTab.GROUP_TRIPS) },
                                    modifier = Modifier.testTag("topbar_group_trips_button")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            val count = savedDestinations.size + groupTrips.size
                                            if (count > 0) {
                                                Badge(containerColor = TerracottaPrimary) {
                                                    Text(count.toString())
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Groups,
                                            contentDescription = "Groups and Saved"
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.EXPLORE,
                        onClick = { viewModel.setTab(AppNavTab.EXPLORE) },
                        icon = { Icon(Icons.Default.Explore, contentDescription = "Discover") },
                        label = { Text("Discover") },
                        modifier = Modifier.testTag("nav_explore")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.ITINERARIES,
                        onClick = { viewModel.setTab(AppNavTab.ITINERARIES) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Itineraries") },
                        label = { Text("Itineraries") },
                        modifier = Modifier.testTag("nav_itineraries")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.BUDGET,
                        onClick = { viewModel.setTab(AppNavTab.BUDGET) },
                        icon = { Icon(Icons.Default.Calculate, contentDescription = "Budget") },
                        label = { Text("Budget") },
                        modifier = Modifier.testTag("nav_budget")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.MAP,
                        onClick = { viewModel.setTab(AppNavTab.MAP) },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                        label = { Text("Map") },
                        modifier = Modifier.testTag("nav_map")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.OPERATORS,
                        onClick = { viewModel.setTab(AppNavTab.OPERATORS) },
                        icon = { Icon(Icons.Default.SupportAgent, contentDescription = "Guides") },
                        label = { Text("Guides") },
                        modifier = Modifier.testTag("nav_operators")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        AppNavTab.EXPLORE -> ExploreScreen(
                            viewModel = viewModel,
                            onDestinationClick = { destination ->
                                viewModel.selectDestination(destination)
                            }
                        )
                        AppNavTab.ITINERARIES -> ItinerariesScreen(
                            viewModel = viewModel,
                            prefilledDestination = prefilledItineraryDestination,
                            onClearPrefilledDestination = { prefilledItineraryDestination = null }
                        )
                        AppNavTab.BUDGET -> BudgetEstimatorScreen(
                            viewModel = viewModel,
                            onSaveToItinerary = { destination, totalBudget, days, transport, lodging ->
                                viewModel.createItinerary(
                                    title = "${destination.name} Trip",
                                    destinationId = destination.id,
                                    destinationName = destination.name,
                                    startDate = "Upcoming Dates",
                                    durationDays = days,
                                    numberOfTravelers = 2,
                                    transportMode = transport,
                                    accommodationTier = lodging,
                                    estimatedBudgetKES = totalBudget,
                                    notes = "Generated from Nomadika Budget Estimator",
                                    activitiesText = "Day 1: Arrival & Park Gate Check-in\nDay 2: Full Day Safari / Activities\nDay 3: Sunrise Highlights & Departure"
                                )
                            }
                        )
                        AppNavTab.MAP -> InteractiveMapScreen(
                            viewModel = viewModel,
                            onOpenDestinationDetail = { destination ->
                                viewModel.selectDestination(destination)
                            }
                        )
                        AppNavTab.OPERATORS -> TourOperatorsScreen(
                            viewModel = viewModel
                        )
                        AppNavTab.GROUP_TRIPS -> GroupAndSavedScreen(
                            viewModel = viewModel,
                            onOpenDestination = { destination ->
                                viewModel.selectDestination(destination)
                            }
                        )
                    }
                }
            }
        }
    }
}

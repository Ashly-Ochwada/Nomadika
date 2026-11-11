package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ItineraryEntity
import com.example.data.model.Destination
import com.example.ui.NomadikaViewModel
import com.example.ui.theme.SafariGold
import com.example.ui.theme.SavannahGreen
import com.example.ui.theme.TerracottaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItinerariesScreen(
    viewModel: NomadikaViewModel,
    prefilledDestination: Destination? = null,
    onClearPrefilledDestination: () -> Unit = {}
) {
    val context = LocalContext.current
    val itineraries by viewModel.itineraries.collectAsState()
    var showCreateDialog by remember { mutableStateOf(prefilledDestination != null) }

    LaunchedEffect(prefilledDestination) {
        if (prefilledDestination != null) {
            showCreateDialog = true
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Itinerary") },
                containerColor = TerracottaPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("create_itinerary_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("itineraries_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Trip Itineraries",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Day-by-day plans, transport, and shareable routes",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (itineraries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No itineraries created yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Start planning your dream safari, coastal vacation, or mountain trek.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Itinerary")
                            }
                        }
                    }
                }
            } else {
                items(itineraries, key = { it.id }) { itinerary ->
                    ItineraryCard(
                        itinerary = itinerary,
                        onShare = { viewModel.shareItinerary(context, itinerary) },
                        onDelete = { viewModel.deleteItinerary(itinerary) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateItineraryDialog(
            destinations = viewModel.allDestinations,
            initialDestination = prefilledDestination,
            onDismiss = {
                showCreateDialog = false
                onClearPrefilledDestination()
            },
            onConfirm = { title, destId, destName, dates, days, travelers, transport, lodging, budget, notes, activities ->
                viewModel.createItinerary(
                    title = title,
                    destinationId = destId,
                    destinationName = destName,
                    startDate = dates,
                    durationDays = days,
                    numberOfTravelers = travelers,
                    transportMode = transport,
                    accommodationTier = lodging,
                    estimatedBudgetKES = budget,
                    notes = notes,
                    activitiesText = activities
                )
                showCreateDialog = false
                onClearPrefilledDestination()
            }
        )
    }
}

@Composable
fun ItineraryCard(
    itinerary: ItineraryEntity,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("itinerary_card_${itinerary.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = itinerary.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "📍 ${itinerary.destinationName}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Surface(
                    color = SavannahGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${itinerary.durationDays} Days",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SavannahGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${itinerary.numberOfTravelers} People", fontSize = 11.sp)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = itinerary.transportMode, fontSize = 11.sp)
                    }
                }

                Surface(
                    color = SafariGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "KES ${"%,d".format(itinerary.estimatedBudgetKES)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (itinerary.notes.isNotBlank()) {
                Text(
                    text = itinerary.notes,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Day-by-Day Schedule:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = itinerary.activitiesJson,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { isExpanded = !isExpanded }) {
                    Text(text = if (isExpanded) "Hide Schedule" else "View Schedule")
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    FilledTonalButton(
                        onClick = onShare,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("share_itinerary_${itinerary.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Share Plan", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItineraryDialog(
    destinations: List<Destination>,
    initialDestination: Destination?,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        destId: String,
        destName: String,
        dates: String,
        days: Int,
        travelers: Int,
        transport: String,
        lodging: String,
        budget: Int,
        notes: String,
        activities: String
    ) -> Unit
) {
    var selectedDest by remember { mutableStateOf(initialDestination ?: destinations.firstOrNull()) }
    var tripTitle by remember { mutableStateOf(selectedDest?.let { "${it.name} Expedition" } ?: "") }
    var dates by remember { mutableStateOf("Next Weekend") }
    var daysCount by remember { mutableIntStateOf(selectedDest?.recommendedDays ?: 3) }
    var travelersCount by remember { mutableIntStateOf(2) }
    var transportMode by remember { mutableStateOf("4x4 Safari Cruiser") }
    var lodgingTier by remember { mutableStateOf("Mid-Range Lodge") }
    var budgetEstimateKES by remember { mutableIntStateOf(45000) }
    var notes by remember { mutableStateOf("") }
    var activitiesText by remember {
        mutableStateOf(
            "Day 1: Departure from Nairobi -> Scenic Stop at Great Rift Valley Viewpoint -> Park Entry & Afternoon Game Drive\n" +
            "Day 2: Full Day Wildlife Exploration & Picnic Bush Lunch -> Sunset Bonfire\n" +
            "Day 3: Sunrise Nature Walk -> Souvenir Crafts -> Return Trip"
        )
    }

    val transportOptions = listOf("4x4 Safari Cruiser", "Tour Van (HiAce)", "SGR Madaraka Express", "Self Drive / Rental", "Domestic Flight")
    val lodgingOptions = listOf("Budget Campsite", "Mid-Range Safari Lodge", "Luxury Tented Resort")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .testTag("create_itinerary_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Plan New Itinerary",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Custom schedule, budget, and travel mode",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Destination Picker
                    Text(text = "Destination:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    var expandedDestDropdown by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedDestDropdown,
                        onExpandedChange = { expandedDestDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = selectedDest?.name ?: "Select Destination",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDestDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDestDropdown,
                            onDismissRequest = { expandedDestDropdown = false }
                        ) {
                            destinations.forEach { dest ->
                                DropdownMenuItem(
                                    text = { Text(dest.name) },
                                    onClick = {
                                        selectedDest = dest
                                        tripTitle = "${dest.name} Trip"
                                        daysCount = dest.recommendedDays
                                        expandedDestDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Trip Title
                    OutlinedTextField(
                        value = tripTitle,
                        onValueChange = { tripTitle = it },
                        label = { Text("Trip Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Dates
                    OutlinedTextField(
                        value = dates,
                        onValueChange = { dates = it },
                        label = { Text("Dates / Season") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Duration & Travelers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = daysCount.toString(),
                            onValueChange = { daysCount = it.toIntOrNull() ?: 1 },
                            label = { Text("Days") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = travelersCount.toString(),
                            onValueChange = { travelersCount = it.toIntOrNull() ?: 1 },
                            label = { Text("Travelers") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Transport Mode
                    Text(text = "Transport Mode:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    var expandedTransport by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedTransport,
                        onExpandedChange = { expandedTransport = it }
                    ) {
                        OutlinedTextField(
                            value = transportMode,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTransport) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTransport,
                            onDismissRequest = { expandedTransport = false }
                        ) {
                            transportOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        transportMode = opt
                                        expandedTransport = false
                                    }
                                )
                            }
                        }
                    }

                    // Lodging Tier
                    Text(text = "Accommodation Style:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    var expandedLodging by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedLodging,
                        onExpandedChange = { expandedLodging = it }
                    ) {
                        OutlinedTextField(
                            value = lodgingTier,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLodging) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedLodging,
                            onDismissRequest = { expandedLodging = false }
                        ) {
                            lodgingOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        lodgingTier = opt
                                        expandedLodging = false
                                    }
                                )
                            }
                        }
                    }

                    // Estimated Budget (KES)
                    OutlinedTextField(
                        value = budgetEstimateKES.toString(),
                        onValueChange = { budgetEstimateKES = it.toIntOrNull() ?: 0 },
                        label = { Text("Estimated Budget (KES)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Activities Text
                    OutlinedTextField(
                        value = activitiesText,
                        onValueChange = { activitiesText = it },
                        label = { Text("Day-by-Day Activities") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Packing & Travel Notes") },
                        placeholder = { Text("e.g. Carry binoculars, yellow fever card, M-Pesa cash") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedDest?.let { dest ->
                                onConfirm(
                                    tripTitle,
                                    dest.id,
                                    dest.name,
                                    dates,
                                    daysCount,
                                    travelersCount,
                                    transportMode,
                                    lodgingTier,
                                    budgetEstimateKES,
                                    notes,
                                    activitiesText
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        modifier = Modifier.testTag("save_itinerary_confirm_button")
                    ) {
                        Text("Save Itinerary")
                    }
                }
            }
        }
    }
}

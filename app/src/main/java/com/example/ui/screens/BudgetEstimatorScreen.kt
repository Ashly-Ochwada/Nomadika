package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.Destination
import com.example.ui.NomadikaViewModel
import com.example.ui.theme.SafariGold
import com.example.ui.theme.SavannahGreen
import com.example.ui.theme.TerracottaPrimary

enum class TravelerStatus(val label: String, val subtitle: String) {
    CITIZEN("Citizen", "Kenya National ID"),
    RESIDENT("Resident", "EAC / Work Permit"),
    NON_RESIDENT("Non-Resident", "International Tourist")
}

enum class LodgingTier(val label: String, val costPerPersonPerNightKES: Int) {
    BUDGET_CAMPING("Budget Camping / Bandas", 2500),
    MID_RANGE("Mid-Range Lodge / Hotel", 11500),
    LUXURY("Luxury Tented Camp / Resort", 34000)
}

enum class TransportOption(val label: String, val isDailyVehicleFee: Boolean, val rateKES: Int) {
    PUBLIC_OR_SGR("SGR Train / Express Shuttle", false, 3000), // per person
    SELF_DRIVE("Self-Drive SUV + Fuel Pool", true, 8000),      // per day total vehicle
    SAFARI_VAN("Safari Tour Van with Guide", true, 14000),     // per day total vehicle
    LAND_CRUISER("4x4 Land Cruiser + Pro Guide", true, 24000)  // per day total vehicle
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEstimatorScreen(
    viewModel: NomadikaViewModel,
    onSaveToItinerary: (Destination, Int, Int, String, String) -> Unit
) {
    val context = LocalContext.current
    val destinations = viewModel.allDestinations
    val currency by viewModel.selectedCurrency.collectAsState()

    var selectedDestination by remember { mutableStateOf(destinations.first()) }
    var travelerStatus by remember { mutableStateOf(TravelerStatus.CITIZEN) }
    var travelersCount by remember { mutableIntStateOf(2) }
    var tripDays by remember { mutableIntStateOf(selectedDestination.recommendedDays) }
    var lodgingTier by remember { mutableStateOf(LodgingTier.MID_RANGE) }
    var transportOption by remember { mutableStateOf(TransportOption.SAFARI_VAN) }
    var includeBalloonSafari by remember { mutableStateOf(false) }
    var includeMarineOrMountainActivity by remember { mutableStateOf(false) }
    var fullBoardMeals by remember { mutableStateOf(true) }

    // Computations
    val nights = (tripDays - 1).coerceAtLeast(1)

    // 1. Park Entry
    val dailyParkFeePerPersonKES = when (travelerStatus) {
        TravelerStatus.CITIZEN -> selectedDestination.citizenEntryFeeKES
        TravelerStatus.RESIDENT -> selectedDestination.residentEntryFeeKES
        TravelerStatus.NON_RESIDENT -> (selectedDestination.nonResidentEntryFeeUSD * viewModel.exchangeRateUsdToKes).toInt()
    }
    val totalParkFeesKES = dailyParkFeePerPersonKES * travelersCount * tripDays

    // 2. Transport
    val totalTransportKES = if (transportOption.isDailyVehicleFee) {
        transportOption.rateKES * tripDays
    } else {
        transportOption.rateKES * travelersCount
    }

    // 3. Accommodation
    val totalLodgingKES = lodgingTier.costPerPersonPerNightKES * travelersCount * nights

    // 4. Meals
    val mealCostPerPersonPerDay = if (fullBoardMeals) 3500 else 1500
    val totalMealsKES = mealCostPerPersonPerDay * travelersCount * tripDays

    // 5. Activities
    var totalActivitiesKES = 0
    if (includeBalloonSafari) {
        totalActivitiesKES += (58000 * travelersCount) // Balloon safari
    }
    if (includeMarineOrMountainActivity) {
        totalActivitiesKES += (5500 * travelersCount) // Snorkeling, diving, or mountain porters
    }

    // Subtotal & 7% emergency buffer
    val subtotalKES = totalParkFeesKES + totalTransportKES + totalLodgingKES + totalMealsKES + totalActivitiesKES
    val contingencyBufferKES = (subtotalKES * 0.07).toInt()
    val grandTotalKES = subtotalKES + contingencyBufferKES
    val costPerPersonKES = if (travelersCount > 0) grandTotalKES / travelersCount else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("budget_estimator_screen"),
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
                        text = "Trip Budget Estimator",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Accurate Kenya park fees, lodging, transport & group split",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = { viewModel.toggleCurrency() },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("budget_currency_button")
                ) {
                    Text(text = currency, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Summary Card (Total & Per Person)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("budget_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SavannahGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ESTIMATED TOTAL TRIP BUDGET",
                        color = SafariGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = viewModel.formatMoney(grandTotalKES),
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black
                        )
                        Surface(
                            color = TerracottaPrimary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${viewModel.formatMoney(costPerPersonKES)} / person",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    )

                    // Quick category pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiniCostTag("Park Entry", viewModel.formatMoney(totalParkFeesKES))
                        MiniCostTag("Transport", viewModel.formatMoney(totalTransportKES))
                        MiniCostTag("Lodging", viewModel.formatMoney(totalLodgingKES))
                        MiniCostTag("Buffer (7%)", viewModel.formatMoney(contingencyBufferKES))
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Destination Selection
        item {
            Text(text = "1. Target Destination", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            var expandedDest by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedDest,
                onExpandedChange = { expandedDest = it }
            ) {
                OutlinedTextField(
                    value = selectedDestination.name,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDest) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedDest,
                    onDismissRequest = { expandedDest = false }
                ) {
                    destinations.forEach { dest ->
                        DropdownMenuItem(
                            text = { Text("${dest.name} (${dest.county})") },
                            onClick = {
                                selectedDestination = dest
                                tripDays = dest.recommendedDays
                                expandedDest = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Resident Status (Affects KWS & County Fees)
        item {
            Text(text = "2. Traveler Residency Status", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Entry fees vary significantly by Kenya ID, EA Resident, or International Visitor",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TravelerStatus.values().forEach { status ->
                    val isSelected = travelerStatus == status
                    OutlinedCard(
                        onClick = { travelerStatus = status },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("status_chip_${status.name}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) TerracottaPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = status.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = status.subtitle,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Party Size & Duration
        item {
            Text(text = "3. Trip Duration & Party Size", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CounterCard(
                    title = "Travelers",
                    count = travelersCount,
                    onDecrement = { if (travelersCount > 1) travelersCount-- },
                    onIncrement = { travelersCount++ },
                    modifier = Modifier.weight(1f)
                )

                CounterCard(
                    title = "Days",
                    count = tripDays,
                    onDecrement = { if (tripDays > 1) tripDays-- },
                    onIncrement = { tripDays++ },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Accommodation Tier
        item {
            Text(text = "4. Accommodation Style", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LodgingTier.values().forEach { tier ->
                    val isSelected = lodgingTier == tier
                    Card(
                        onClick = { lodgingTier = tier },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SavannahGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isSelected) SavannahGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = tier.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SavannahGreen else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${viewModel.formatMoney(tier.costPerPersonPerNightKES)} / person / night",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { lodgingTier = tier }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Transport Option
        item {
            Text(text = "5. Transport Mode", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TransportOption.values().forEach { opt ->
                    val isSelected = transportOption == opt
                    Card(
                        onClick = { transportOption = opt },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) TerracottaPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = opt.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                val rateDesc = if (opt.isDailyVehicleFee) {
                                    "${viewModel.formatMoney(opt.rateKES)} / day (split across party)"
                                } else {
                                    "${viewModel.formatMoney(opt.rateKES)} / traveler roundtrip"
                                }
                                Text(
                                    text = rateDesc,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { transportOption = opt }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Activities & Upgrades
        item {
            Text(text = "6. Optional Activities & Dining", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Dawn Balloon Safari", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "+ KES 58,000 / $450 per person (with bush breakfast)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = includeBalloonSafari,
                            onCheckedChange = { includeBalloonSafari = it }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Special Guided Excursion", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "+ KES 5,500 per person (Dolphin dhow, canyon guide, or porters)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = includeMarineOrMountainActivity,
                            onCheckedChange = { includeMarineOrMountainActivity = it }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Actions: Share & Save to Itinerary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val shareText = """
                            🇰🇪 Nomadika Trip Budget Estimate:
                            📍 Destination: ${selectedDestination.name}
                            👥 Travelers: $travelersCount (${travelerStatus.label})
                            🗓 Duration: $tripDays Days ($nights Nights)
                            🏨 Lodging: ${lodgingTier.label} (${viewModel.formatMoney(totalLodgingKES)})
                            🚗 Transport: ${transportOption.label} (${viewModel.formatMoney(totalTransportKES)})
                            🎟 Park Entry: ${viewModel.formatMoney(totalParkFeesKES)}
                            🍽 Food & Sundries: ${viewModel.formatMoney(totalMealsKES)}
                            🛡 Contingency Buffer: ${viewModel.formatMoney(contingencyBufferKES)}
                            ====================================
                            💰 Total Estimated: ${viewModel.formatMoney(grandTotalKES)}
                            👤 Cost Per Person: ${viewModel.formatMoney(costPerPersonKES)}
                            
                            Calculated with Nomadika - Kenya Travel Planner
                        """.trimIndent()

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Budget Breakdown"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_budget_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Split")
                }

                Button(
                    onClick = {
                        onSaveToItinerary(
                            selectedDestination,
                            grandTotalKES,
                            tripDays,
                            transportOption.label,
                            lodgingTier.label
                        )
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("save_as_itinerary_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Itinerary")
                }
            }
        }
    }
}

@Composable
fun MiniCostTag(label: String, amount: String) {
    Column {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
        Text(text = amount, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CounterCard(
    title: String,
    count: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }
                Text(
                    text = count.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }
    }
}

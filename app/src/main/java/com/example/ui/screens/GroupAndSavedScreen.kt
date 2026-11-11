package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.local.GroupTripEntity
import com.example.data.model.Destination
import com.example.ui.AppNavTab
import com.example.ui.NomadikaViewModel
import com.example.ui.theme.SafariGold
import com.example.ui.theme.SavannahGreen
import com.example.ui.theme.TerracottaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupAndSavedScreen(
    viewModel: NomadikaViewModel,
    onOpenDestination: (Destination) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Group Trips, 1: Saved Wishlist
    val groupTrips by viewModel.groupTrips.collectAsState()
    val savedEntities by viewModel.savedDestinations.collectAsState()
    var showCreateGroupDialog by remember { mutableStateOf(false) }

    val savedDestinations = remember(savedEntities) {
        val savedIds = savedEntities.map { it.destinationId }.toSet()
        viewModel.allDestinations.filter { savedIds.contains(it.id) }
    }

    Scaffold(
        floatingActionButton = {
            if (selectedSubTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateGroupDialog = true },
                    icon = { Icon(Icons.Default.GroupAdd, contentDescription = null) },
                    text = { Text("New Group Trip") },
                    containerColor = SavannahGreen,
                    contentColor = Color.White,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("create_group_trip_fab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("group_and_saved_screen")
        ) {
            // Header
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                Text(
                    text = "Groups & Saved",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Coordinate friends, split travel costs, and manage wishlist",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedSubTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = TerracottaPrimary
                ) {
                    Tab(
                        selected = selectedSubTab == 0,
                        onClick = { selectedSubTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Group Trips (${groupTrips.size})")
                            }
                        }
                    )
                    Tab(
                        selected = selectedSubTab == 1,
                        onClick = { selectedSubTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Wishlist (${savedDestinations.size})")
                            }
                        }
                    )
                }
            }

            // Body
            if (selectedSubTab == 0) {
                // Group Trips Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (groupTrips.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.Groups,
                                title = "No group trips active",
                                subtitle = "Create a shared trip with friends, split costs, assign roles, and track checklists.",
                                actionLabel = "Start a Group Trip",
                                onAction = { showCreateGroupDialog = true }
                            )
                        }
                    } else {
                        items(groupTrips, key = { it.id }) { trip ->
                            GroupTripCard(
                                trip = trip,
                                viewModel = viewModel,
                                onShareInvite = {
                                    val shareText = """
                                        🇰🇪 Join our Kenya Group Adventure: ${trip.tripName}!
                                        📍 Destination: ${trip.destinationName}
                                        🗓 Departure: ${trip.departureDate}
                                        🔑 Nomadika Invite Code: ${trip.inviteCode}
                                        💰 Target Budget Pool: KES ${"%,d".format(trip.totalEstimatedBudgetKES)}
                                        👥 Crew: ${trip.membersSummary}
                                        
                                        Open Nomadika app to view itinerary, budget split & checklists!
                                    """.trimIndent()
                                    val intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Group Invite"))
                                },
                                onCopyCode = {
                                    clipboardManager.setText(AnnotatedString(trip.inviteCode))
                                },
                                onDelete = { viewModel.deleteGroupTrip(trip) }
                            )
                        }
                    }
                }
            } else {
                // Saved Wishlist Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (savedDestinations.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.BookmarkBorder,
                                title = "Your Kenya wishlist is empty",
                                subtitle = "Explore national parks, marine reserves, and mountain peaks and tap the bookmark icon to save them.",
                                actionLabel = "Explore Destinations",
                                onAction = { viewModel.setTab(AppNavTab.EXPLORE) }
                            )
                        }
                    } else {
                        items(savedDestinations, key = { it.id }) { destination ->
                            SavedDestinationItem(
                                destination = destination,
                                onOpen = { onOpenDestination(destination) },
                                onRemove = { viewModel.toggleSaveDestination(destination.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateGroupDialog) {
        CreateGroupTripDialog(
            destinations = viewModel.allDestinations,
            onDismiss = { showCreateGroupDialog = false },
            onConfirm = { tripName, destName, departureDate, budget, members, checklist ->
                viewModel.createGroupTrip(
                    tripName = tripName,
                    destinationName = destName,
                    departureDate = departureDate,
                    targetBudgetKES = budget,
                    members = members,
                    checklist = checklist
                )
                showCreateGroupDialog = false
            }
        )
    }
}

@Composable
fun GroupTripCard(
    trip: GroupTripEntity,
    viewModel: NomadikaViewModel,
    onShareInvite: () -> Unit,
    onCopyCode: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (trip.totalEstimatedBudgetKES > 0) {
        (trip.currentCollectedKES.toFloat() / trip.totalEstimatedBudgetKES.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("group_trip_card_${trip.id}"),
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
                        text = trip.tripName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "📍 ${trip.destinationName} • 🗓 ${trip.departureDate}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = SafariGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onCopyCode() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(12.dp), tint = SavannahGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = trip.inviteCode,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SavannahGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Budget Pool Progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Group Budget Pool",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% Funded",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SavannahGreen
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SavannahGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Collected: KES ${"%,d".format(trip.currentCollectedKES)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Target: KES ${"%,d".format(trip.totalEstimatedBudgetKES)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Members / Crew roles
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp), tint = TerracottaPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Travel Crew & Roles", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = trip.membersSummary,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Shared Checklist Summary
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp), tint = SavannahGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Group Checklist", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = trip.checklistSummary,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
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
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Button(
                    onClick = onShareInvite,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SavannahGreen)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Invite Code", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SavedDestinationItem(
    destination: Destination,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("saved_destination_item_${destination.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imgRes = destination.drawableRes ?: R.drawable.img_kenya_safari
            Image(
                painter = painterResource(id = imgRes),
                contentDescription = destination.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = destination.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${destination.county} • ${destination.category.label}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${destination.distanceFromNairobiKm} km from Nairobi",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.BookmarkRemove,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SavannahGreen,
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                lineHeight = 18.sp
            )
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = SavannahGreen)
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun CreateGroupTripDialog(
    destinations: List<Destination>,
    onDismiss: () -> Unit,
    onConfirm: (
        tripName: String,
        destName: String,
        departureDate: String,
        budget: Int,
        members: String,
        checklist: String
    ) -> Unit
) {
    var selectedDest by remember { mutableStateOf(destinations.first()) }
    var tripName by remember { mutableStateOf("Weekend at ${selectedDest.name}") }
    var departureDate by remember { mutableStateOf("Saturday, Dec 12") }
    var targetBudget by remember { mutableIntStateOf(48000) }
    var members by remember { mutableStateOf("You (Lead), Alex (Treasurer), Wanjiku (Navigator), Juma (Driver)") }
    var checklist by remember { mutableStateOf("Park Entry: Paid on eCitizen, Binoculars: Packed, First Aid Kit: Ready, Car Serviced: Done") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_group_trip_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Start a Group Trip", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = tripName,
                    onValueChange = { tripName = it },
                    label = { Text("Group Trip Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = departureDate,
                    onValueChange = { departureDate = it },
                    label = { Text("Departure Date") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetBudget.toString(),
                    onValueChange = { targetBudget = it.toIntOrNull() ?: 10000 },
                    label = { Text("Target Budget Pool (KES)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = members,
                    onValueChange = { members = it },
                    label = { Text("Crew Members & Roles") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = checklist,
                    onValueChange = { checklist = it },
                    label = { Text("Initial Checklist") },
                    modifier = Modifier.fillMaxWidth()
                )

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
                            onConfirm(tripName, selectedDest.name, departureDate, targetBudget, members, checklist)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SavannahGreen)
                    ) {
                        Text("Create Trip")
                    }
                }
            }
        }
    }
}

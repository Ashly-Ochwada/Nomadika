package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.TourOperator
import com.example.ui.NomadikaViewModel
import com.example.ui.theme.SafariGold
import com.example.ui.theme.SavannahGreen
import com.example.ui.theme.TerracottaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourOperatorsScreen(
    viewModel: NomadikaViewModel
) {
    val context = LocalContext.current
    val operators = viewModel.allOperators
    val quoteOperator by viewModel.quoteOperator.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val filterOptions = listOf("All", "Safari & Big 5", "Coastal & Dhow", "Mountaineering", "Eco & Biking")

    val filteredOperators = remember(selectedFilter) {
        when (selectedFilter) {
            "Safari & Big 5" -> operators.filter { it.specialties.any { s -> s.contains("Safari") || s.contains("Migration") } }
            "Coastal & Dhow" -> operators.filter { it.specialties.any { s -> s.contains("Dolphin") || s.contains("Dhow") } }
            "Mountaineering" -> operators.filter { it.specialties.any { s -> s.contains("Mountain") || s.contains("Summit") } }
            "Eco & Biking" -> operators.filter { it.specialties.any { s -> s.contains("Biking") || s.contains("Eco") } }
            else -> operators
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tour_operators_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Local Tour Operators",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "KATO-verified Kenya guides, private 4x4 fleets & direct booking",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Trust Banner / Guarantee
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SavannahGreen.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = SavannahGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Verified KATO & Tourism Regulatory Authority Partners",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SavannahGreen
                        )
                        Text(
                            text = "Direct operator rates with zero middleman markups & 24/7 safari support.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filterOptions.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TerracottaPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        items(filteredOperators, key = { it.id }) { operator ->
            OperatorCard(
                operator = operator,
                viewModel = viewModel,
                onWhatsApp = { viewModel.contactOperatorViaWhatsApp(context, operator) },
                onCall = { viewModel.contactOperatorViaPhone(context, operator) },
                onRequestQuote = { viewModel.openQuoteDialog(operator) }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
    }

    // Quote Request Dialog
    quoteOperator?.let { operator ->
        QuoteRequestDialog(
            operator = operator,
            onDismiss = { viewModel.openQuoteDialog(null) },
            onSubmitQuote = { travelDates, groupSize, requirements ->
                val emailBody = """
                    Nomadika Tour Quote Request:
                    ----------------------------------
                    Operator: ${operator.name}
                    Package: ${operator.popularPackage}
                    Dates: $travelDates
                    Travelers: $groupSize
                    Special Requests: $requirements
                    
                    Sent via Nomadika Travel Planner App
                """.trimIndent()

                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${operator.email}")
                    putExtra(Intent.EXTRA_SUBJECT, "Inquiry: ${operator.popularPackage} via Nomadika")
                    putExtra(Intent.EXTRA_TEXT, emailBody)
                }
                try {
                    context.startActivity(Intent.createChooser(emailIntent, "Send Quote Request"))
                } catch (e: Exception) {
                    viewModel.contactOperatorViaWhatsApp(context, operator)
                }
                viewModel.openQuoteDialog(null)
            }
        )
    }
}

@Composable
fun OperatorCard(
    operator: TourOperator,
    viewModel: NomadikaViewModel,
    onWhatsApp: () -> Unit,
    onCall: () -> Unit,
    onRequestQuote: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("operator_card_${operator.id}"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = operator.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (operator.verifiedBadge) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = SavannahGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "📍 ${operator.location}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = SafariGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = SafariGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "%.1f (%d)".format(operator.rating, operator.reviewCount),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = operator.tagLine,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = operator.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            // Fleet Info Card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fleet: ${operator.fleetType}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Featured Package & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Featured Package",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = operator.popularPackage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Starting from",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = viewModel.formatMoney(operator.startingPriceKES),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SavannahGreen
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            // Action Buttons (WhatsApp, Call, Quote)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("call_operator_${operator.id}")
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = onWhatsApp,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF25D366).copy(alpha = 0.15f),
                        contentColor = Color(0xFF075E54)
                    ),
                    modifier = Modifier.testTag("whatsapp_operator_${operator.id}")
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 12.sp)
                }

                Button(
                    onClick = onRequestQuote,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quote_operator_${operator.id}"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Get Quote", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun QuoteRequestDialog(
    operator: TourOperator,
    onDismiss: () -> Unit,
    onSubmitQuote: (travelDates: String, groupSize: String, specialRequests: String) -> Unit
) {
    var travelDates by remember { mutableStateOf("December Peak Season") }
    var groupSize by remember { mutableStateOf("4 Adults, 2 Kids") }
    var specialRequests by remember {
        mutableStateOf("We want 4x4 Land Cruiser with pop-up photography roof and dietary requirements for vegetarians.")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quote_request_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Request Safari Quote",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Operator: ${operator.name}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = travelDates,
                    onValueChange = { travelDates = it },
                    label = { Text("Target Travel Dates") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = groupSize,
                    onValueChange = { groupSize = it },
                    label = { Text("Group Size & Travelers") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = specialRequests,
                    onValueChange = { specialRequests = it },
                    label = { Text("Custom Needs / Questions") },
                    minLines = 3,
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
                        onClick = { onSubmitQuote(travelDates, groupSize, specialRequests) },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        Text("Submit Request")
                    }
                }
            }
        }
    }
}

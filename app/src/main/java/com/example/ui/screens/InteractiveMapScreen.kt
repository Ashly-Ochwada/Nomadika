package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveMapScreen(
    viewModel: NomadikaViewModel,
    onOpenDestinationDetail: (Destination) -> Unit
) {
    val context = LocalContext.current
    val destinations = viewModel.allDestinations
    var selectedCircuit by remember { mutableStateOf("All Circuits") }
    var activeDestination by remember { mutableStateOf(destinations.first()) }

    val circuits = listOf(
        "All Circuits",
        "Southern Safari Circuit",
        "Coastal Strip",
        "Central Highlands",
        "Rift Valley Circuit",
        "Northern Frontier",
        "Nairobi Metro"
    )

    val displayedDestinations = remember(selectedCircuit) {
        if (selectedCircuit == "All Circuits") destinations
        else destinations.filter { it.circuit == selectedCircuit }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("interactive_map_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp)
        ) {
            // Header bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Interactive Kenya Map",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap pins to inspect circuits, distance & routes",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = TerracottaPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Nairobi Base (0, 0)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(circuits) { circuit ->
                            val isSelected = selectedCircuit == circuit
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCircuit = circuit },
                                label = { Text(circuit, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SavannahGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Map Area Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFEDE6DA)) // Warm parchment map tone
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(displayedDestinations) {
                            detectTapGestures { tapOffset ->
                                // Kenya bounds:
                                // minLat = -4.8, maxLat = 4.8
                                // minLng = 33.8, maxLng = 42.0
                                val width = size.width
                                val height = size.height

                                // Find closest destination within 40dp radius
                                val closest = displayedDestinations.minByOrNull { dest ->
                                    val x = ((dest.lng - 33.8) / (42.0 - 33.8)).toFloat() * width
                                    val y = ((4.8 - dest.lat) / (4.8 - (-4.8))).toFloat() * height
                                    val dx = tapOffset.x - x
                                    val dy = tapOffset.y - y
                                    dx * dx + dy * dy
                                }

                                closest?.let {
                                    val x = ((it.lng - 33.8) / (42.0 - 33.8)).toFloat() * width
                                    val y = ((4.8 - it.lat) / (4.8 - (-4.8))).toFloat() * height
                                    val distSq = (tapOffset.x - x) * (tapOffset.x - x) + (tapOffset.y - y) * (tapOffset.y - y)
                                    if (distSq < 4500) { // roughly ~65 pixels
                                        activeDestination = it
                                    }
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    fun project(lat: Double, lng: Double): Offset {
                        val x = ((lng - 33.8) / (42.0 - 33.8)).toFloat() * w
                        val y = ((4.8 - lat) / (4.8 - (-4.8))).toFloat() * h
                        return Offset(x, y)
                    }

                    // 1. Draw Indian Ocean (Southeast corner)
                    val oceanPath = Path().apply {
                        val start = project(-2.0, 41.5)
                        moveTo(w, h * 0.55f)
                        lineTo(start.x, start.y)
                        val lamu = project(-2.27, 40.9)
                        lineTo(lamu.x, lamu.y)
                        val malindi = project(-3.2, 40.1)
                        lineTo(malindi.x, malindi.y)
                        val mombasa = project(-4.05, 39.66)
                        lineTo(mombasa.x, mombasa.y)
                        val diani = project(-4.5, 39.3)
                        lineTo(diani.x, diani.y)
                        lineTo(w, h)
                        close()
                    }
                    drawPath(oceanPath, color = Color(0xFF90CAF9).copy(alpha = 0.5f))

                    // 2. Draw Lake Victoria (Western corner)
                    val lakeVicPath = Path().apply {
                        val p1 = project(-0.1, 34.0)
                        val p2 = project(-0.6, 34.2)
                        val p3 = project(-1.2, 34.1)
                        val p4 = project(-1.0, 33.8)
                        moveTo(0f, p1.y)
                        lineTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        lineTo(p3.x, p3.y)
                        lineTo(0f, p4.y)
                        close()
                    }
                    drawPath(lakeVicPath, color = Color(0xFF90CAF9).copy(alpha = 0.6f))

                    // 3. Draw Equator Line
                    val equatorY = project(0.0, 37.0).y
                    drawLine(
                        color = Color(0xFFB0A294),
                        start = Offset(0f, equatorY),
                        end = Offset(w, equatorY),
                        strokeWidth = 2f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
                    )

                    // 4. Nairobi Hub Pin & route rays to active destination
                    val nairobiPos = project(-1.286389, 36.817223)
                    val activePos = project(activeDestination.lat, activeDestination.lng)

                    // Draw connection route line from Nairobi
                    drawLine(
                        color = TerracottaPrimary.copy(alpha = 0.7f),
                        start = nairobiPos,
                        end = activePos,
                        strokeWidth = 3f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    // Nairobi center marker
                    drawCircle(
                        color = Color(0xFF212121),
                        radius = 8f,
                        center = nairobiPos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.5f,
                        center = nairobiPos
                    )

                    // 5. Draw all destination pins
                    displayedDestinations.forEach { dest ->
                        val pos = project(dest.lat, dest.lng)
                        val isSelected = dest.id == activeDestination.id

                        if (isSelected) {
                            // Pulsing halo
                            drawCircle(
                                color = TerracottaPrimary.copy(alpha = 0.35f),
                                radius = 24f,
                                center = pos
                            )
                            drawCircle(
                                color = TerracottaPrimary,
                                radius = 12f,
                                center = pos
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 5f,
                                center = pos
                            )
                        } else {
                            drawCircle(
                                color = SavannahGreen,
                                radius = 8f,
                                center = pos
                            )
                            drawCircle(
                                color = SafariGold,
                                radius = 3.5f,
                                center = pos
                            )
                        }
                    }
                }

                // Map Legend Overlay
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF212121), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Nairobi Capital", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(SavannahGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "National Parks & Beaches", fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(TerracottaPrimary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Selected Pin", fontSize = 10.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bottom Selected Destination Detail Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("map_selected_destination_card"),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = SavannahGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = activeDestination.circuit,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SavannahGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = activeDestination.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = "${activeDestination.distanceFromNairobiKm} km from Nairobi • ${activeDestination.travelTimeFromNairobi}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                val gmmIntentUri = Uri.parse("geo:${activeDestination.lat},${activeDestination.lng}?q=${Uri.encode(activeDestination.name)}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                context.startActivity(mapIntent)
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Navigate",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val gmmIntentUri = Uri.parse("geo:${activeDestination.lat},${activeDestination.lng}?q=${Uri.encode(activeDestination.name)}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                context.startActivity(mapIntent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Turn-by-Turn", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onOpenDestinationDetail(activeDestination) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                        ) {
                            Text("Full Guide", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

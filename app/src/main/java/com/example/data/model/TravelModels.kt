package com.example.data.model

import androidx.annotation.DrawableRes
import com.example.R

enum class DestinationCategory(val label: String, val iconName: String) {
    SAFARI_WILDLIFE("Wildlife & Safari", "Pets"),
    BEACH_COASTAL("Coastal & Beaches", "BeachAccess"),
    MOUNTAIN_HIKING("Mountain & Hiking", "Terrain"),
    CULTURE_HERITAGE("Culture & Heritage", "Museum"),
    LAKES_SCENIC("Lakes & Scenic", "Water"),
    ADVENTURE_EXTREME("Adventure & Action", "DirectionsRun")
}

data class Destination(
    val id: String,
    val name: String,
    val tagline: String,
    val county: String,
    val circuit: String,
    val category: DestinationCategory,
    val description: String,
    val highlights: List<String>,
    val citizenEntryFeeKES: Int,
    val residentEntryFeeKES: Int,
    val nonResidentEntryFeeUSD: Int,
    val recommendedDays: Int,
    val lat: Double,
    val lng: Double,
    val distanceFromNairobiKm: Int,
    val travelTimeFromNairobi: String,
    val bestMonths: String,
    val transportModes: List<String>,
    val localFood: List<String>,
    val travelTips: List<String>,
    @DrawableRes val drawableRes: Int? = null,
    val budgetTier: String,
    val rating: Float,
    val reviewCount: Int,
    val isPopular: Boolean = false
)

data class ActivityItem(
    val id: String,
    val dayNumber: Int,
    val timeSlot: String, // "Morning", "Afternoon", "Sunset/Evening"
    val title: String,
    val description: String,
    val estimatedCostKES: Int,
    val location: String,
    val isCompleted: Boolean = false
)

data class TourOperator(
    val id: String,
    val name: String,
    val tagLine: String,
    val location: String,
    val verifiedBadge: Boolean = true,
    val katoMember: Boolean = true, // Kenya Association of Tour Operators
    val rating: Float,
    val reviewCount: Int,
    val fleetType: String, // e.g. "4x4 Safari Land Cruisers & Customized Tour Vans"
    val specialties: List<String>,
    val startingPriceKES: Int,
    val startingPriceUSD: Int,
    val phone: String,
    val whatsapp: String,
    val email: String,
    val description: String,
    val popularPackage: String
)

data class GroupMember(
    val name: String,
    val role: String, // "Trip Leader", "Treasurer", "Driver/Navigator", "Explorer"
    val hasConfirmed: Boolean = true,
    val paidContributionKES: Int = 0
)

data class ChecklistItem(
    val id: String,
    val title: String,
    val category: String, // "Documents", "Safari Gear", "Health & Safety", "Clothing"
    val isChecked: Boolean = false,
    val assignedTo: String = "Anyone"
)

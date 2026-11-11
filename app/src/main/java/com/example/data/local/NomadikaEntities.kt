package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_destinations")
data class SavedDestinationEntity(
    @PrimaryKey val destinationId: String,
    val savedAtMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val wishlistCategory: String = "Must Visit" // "Must Visit", "Weekend Getaways", "Family Trip"
)

@Entity(tableName = "custom_itineraries")
data class ItineraryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val destinationId: String,
    val destinationName: String,
    val startDate: String,
    val durationDays: Int,
    val numberOfTravelers: Int,
    val transportMode: String,
    val accommodationTier: String,
    val estimatedBudgetKES: Int,
    val notes: String,
    val activitiesJson: String, // Stored as formatted text/json
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "group_trips")
data class GroupTripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripName: String,
    val destinationName: String,
    val departureDate: String,
    val inviteCode: String,
    val totalEstimatedBudgetKES: Int,
    val currentCollectedKES: Int,
    val membersSummary: String, // e.g. "Alice (Lead), Bob (Treas.), Clara (Nav)"
    val checklistSummary: String // e.g. "Safari Hats: Done, Binoculars: Pending"
)

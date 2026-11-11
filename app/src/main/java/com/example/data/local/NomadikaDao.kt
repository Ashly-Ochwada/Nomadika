package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NomadikaDao {

    // Saved destinations
    @Query("SELECT * FROM saved_destinations ORDER BY savedAtMillis DESC")
    fun getAllSavedDestinations(): Flow<List<SavedDestinationEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_destinations WHERE destinationId = :id)")
    fun isDestinationSaved(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDestination(entity: SavedDestinationEntity)

    @Query("DELETE FROM saved_destinations WHERE destinationId = :id")
    suspend fun removeSavedDestination(id: String)

    // Itineraries
    @Query("SELECT * FROM custom_itineraries ORDER BY id DESC")
    fun getAllItineraries(): Flow<List<ItineraryEntity>>

    @Query("SELECT * FROM custom_itineraries WHERE id = :id")
    suspend fun getItineraryById(id: Long): ItineraryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItinerary(itinerary: ItineraryEntity): Long

    @Update
    suspend fun updateItinerary(itinerary: ItineraryEntity)

    @Delete
    suspend fun deleteItinerary(itinerary: ItineraryEntity)

    // Group Trips
    @Query("SELECT * FROM group_trips ORDER BY id DESC")
    fun getAllGroupTrips(): Flow<List<GroupTripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupTrip(trip: GroupTripEntity): Long

    @Update
    suspend fun updateGroupTrip(trip: GroupTripEntity)

    @Delete
    suspend fun deleteGroupTrip(trip: GroupTripEntity)
}

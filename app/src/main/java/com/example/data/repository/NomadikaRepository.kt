package com.example.data.repository

import com.example.data.local.GroupTripEntity
import com.example.data.local.ItineraryEntity
import com.example.data.local.NomadikaDao
import com.example.data.local.SavedDestinationEntity
import com.example.data.model.Destination
import com.example.data.model.DestinationCategory
import com.example.data.model.KenyaDestinationsData
import com.example.data.model.TourOperator
import com.example.data.model.TourOperatorsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class NomadikaRepository(private val dao: NomadikaDao) {

    fun getAllDestinations(): List<Destination> = KenyaDestinationsData.allDestinations

    fun getDestinationById(id: String): Destination? =
        KenyaDestinationsData.allDestinations.find { it.id == id }

    fun filterDestinations(
        searchQuery: String,
        category: DestinationCategory?,
        circuit: String?,
        maxDistanceKm: Int?
    ): List<Destination> {
        return KenyaDestinationsData.allDestinations.filter { dest ->
            val matchesSearch = searchQuery.isBlank() ||
                dest.name.contains(searchQuery, ignoreCase = true) ||
                dest.county.contains(searchQuery, ignoreCase = true) ||
                dest.tagline.contains(searchQuery, ignoreCase = true) ||
                dest.description.contains(searchQuery, ignoreCase = true)

            val matchesCategory = category == null || dest.category == category
            val matchesCircuit = circuit == null || circuit == "All Circuits" || dest.circuit == circuit
            val matchesDistance = maxDistanceKm == null || dest.distanceFromNairobiKm <= maxDistanceKm

            matchesSearch && matchesCategory && matchesCircuit && matchesDistance
        }
    }

    fun getAllTourOperators(): List<TourOperator> = TourOperatorsData.operators

    fun getTourOperatorById(id: String): TourOperator? =
        TourOperatorsData.operators.find { it.id == id }

    // Saved Bookmarks
    val savedDestinations: Flow<List<SavedDestinationEntity>> = dao.getAllSavedDestinations()

    fun isDestinationSaved(id: String): Flow<Boolean> = dao.isDestinationSaved(id)

    suspend fun toggleSaveDestination(id: String, isCurrentlySaved: Boolean) {
        if (isCurrentlySaved) {
            dao.removeSavedDestination(id)
        } else {
            dao.saveDestination(SavedDestinationEntity(destinationId = id))
        }
    }

    // Itineraries
    val itineraries: Flow<List<ItineraryEntity>> = dao.getAllItineraries()

    suspend fun createItinerary(itinerary: ItineraryEntity): Long = dao.insertItinerary(itinerary)

    suspend fun updateItinerary(itinerary: ItineraryEntity) = dao.updateItinerary(itinerary)

    suspend fun deleteItinerary(itinerary: ItineraryEntity) = dao.deleteItinerary(itinerary)

    suspend fun getItinerary(id: Long): ItineraryEntity? = dao.getItineraryById(id)

    // Group Trips
    val groupTrips: Flow<List<GroupTripEntity>> = dao.getAllGroupTrips()

    suspend fun createGroupTrip(trip: GroupTripEntity): Long = dao.insertGroupTrip(trip)

    suspend fun updateGroupTrip(trip: GroupTripEntity) = dao.updateGroupTrip(trip)

    suspend fun deleteGroupTrip(trip: GroupTripEntity) = dao.deleteGroupTrip(trip)

    suspend fun seedInitialDataIfEmpty() {
        val existingSaved = dao.getAllSavedDestinations().firstOrNull()
        if (existingSaved.isNullOrEmpty()) {
            dao.saveDestination(SavedDestinationEntity(destinationId = "maasai-mara", wishlistCategory = "Bucket List"))
            dao.saveDestination(SavedDestinationEntity(destinationId = "diani-beach", wishlistCategory = "Weekend Getaways"))
        }

        val existingItineraries = dao.getAllItineraries().firstOrNull()
        if (existingItineraries.isNullOrEmpty()) {
            dao.insertItinerary(
                ItineraryEntity(
                    title = "Maasai Mara 3-Day Great Migration Safari",
                    destinationId = "maasai-mara",
                    destinationName = "Maasai Mara National Reserve",
                    startDate = "Aug 15 - Aug 17",
                    durationDays = 3,
                    numberOfTravelers = 4,
                    transportMode = "4x4 Safari Land Cruiser",
                    accommodationTier = "Mid-Range Tented Camp",
                    estimatedBudgetKES = 124000,
                    notes = "Early departure from Nairobi at 6:30 AM via Mai Mahiu escarpment viewpoint. Binoculars and telephoto lens packed.",
                    activitiesJson = """
                        Day 1: Morning drive via Great Rift Valley Escarpment -> Afternoon arrival & check-in -> Evening Sunset Game Drive (Spotted pride of lions near Talek River)
                        Day 2: 6:00 AM Dawn Balloon Safari with Bush Champagne Breakfast -> Full Day Mara Triangle Game Drive -> Mara River crossing watch -> Evening bonfire & Maasai storytelling
                        Day 3: Sunrise birdwatching & final predator tracking -> Midday souvenir stop at Narok town -> Late afternoon return to Nairobi
                    """.trimIndent()
                )
            )
            dao.insertItinerary(
                ItineraryEntity(
                    title = "Diani Beach Coastal Escape & Marine Safari",
                    destinationId = "diani-beach",
                    destinationName = "Diani Beach & South Coast",
                    startDate = "Oct 20 - Oct 23",
                    durationDays = 4,
                    numberOfTravelers = 2,
                    transportMode = "Madaraka Express SGR + Transfer",
                    accommodationTier = "Beachfront Boutique Resort",
                    estimatedBudgetKES = 78000,
                    notes = "SGR tickets booked 30 days ahead on e-Citizen / Kenya Railways. Fresh seafood and sunset dhow cruise on day 2.",
                    activitiesJson = """
                        Day 1: Madaraka Express SGR from Nairobi to Mombasa -> Scenic ferry / bypass to Diani -> Afternoon coconut water & beach walk
                        Day 2: Full day Kisite-Mpunguti Marine Park -> Snorkeling with dolphins -> Wasini Island Swahili crab lunch
                        Day 3: Morning kitesurfing lesson -> Visit Colobus Monkey Conservation Center -> Sunset dhow cruise with cocktails
                        Day 4: Morning swim in the warm Indian Ocean -> Souvenir crafts shopping -> Afternoon flight back from Ukunda airstrip
                    """.trimIndent()
                )
            )
        }

        val existingGroupTrips = dao.getAllGroupTrips().firstOrNull()
        if (existingGroupTrips.isNullOrEmpty()) {
            dao.insertGroupTrip(
                GroupTripEntity(
                    tripName = "Choma & Cycling at Hell's Gate",
                    destinationName = "Hell's Gate National Park & Naivasha",
                    departureDate = "Saturday, Nov 21",
                    inviteCode = "NOMAD-7749",
                    totalEstimatedBudgetKES = 32000,
                    currentCollectedKES = 24000,
                    membersSummary = "Brian (Lead), Achieng (Treasurer), Kevin (Driver), Mwangi (Explorer)",
                    checklistSummary = "Bicycles: Reserved, Sunscreen: Packed, Geothermal Swimwear: Ready, Portable Speakers: Packed"
                )
            )
        }
    }
}

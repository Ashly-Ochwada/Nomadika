package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GroupTripEntity
import com.example.data.local.ItineraryEntity
import com.example.data.local.NomadikaDatabase
import com.example.data.model.Destination
import com.example.data.model.DestinationCategory
import com.example.data.model.TourOperator
import com.example.data.repository.NomadikaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String) {
    EXPLORE("Discover"),
    ITINERARIES("Itineraries"),
    BUDGET("Budget Calc"),
    MAP("Explore Map"),
    OPERATORS("Tour Guides"),
    GROUP_TRIPS("Group Trips")
}

class NomadikaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NomadikaRepository

    private val _currentTab = MutableStateFlow(AppNavTab.EXPLORE)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    private val _selectedDestination = MutableStateFlow<Destination?>(null)
    val selectedDestination: StateFlow<Destination?> = _selectedDestination.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<DestinationCategory?>(null)
    val selectedCategory: StateFlow<DestinationCategory?> = _selectedCategory.asStateFlow()

    private val _selectedCircuit = MutableStateFlow("All Circuits")
    val selectedCircuit: StateFlow<String> = _selectedCircuit.asStateFlow()

    // Currency toggle: KES or USD (Exchange rate ~ 1 USD = 129 KES)
    private val _selectedCurrency = MutableStateFlow("KES")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()
    val exchangeRateUsdToKes = 129.0

    // Quote dialog state
    private val _quoteOperator = MutableStateFlow<TourOperator?>(null)
    val quoteOperator: StateFlow<TourOperator?> = _quoteOperator.asStateFlow()

    init {
        val db = NomadikaDatabase.getDatabase(application)
        repository = NomadikaRepository(db.nomadikaDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val allDestinations = repository.getAllDestinations()
    val allOperators = repository.getAllTourOperators()

    // Filtered destinations stream
    val filteredDestinations = combine(
        _searchQuery,
        _selectedCategory,
        _selectedCircuit
    ) { query, category, circuit ->
        repository.filterDestinations(
            searchQuery = query,
            category = category,
            circuit = if (circuit == "All Circuits") null else circuit,
            maxDistanceKm = null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allDestinations)

    val savedDestinations = repository.savedDestinations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val itineraries = repository.itineraries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupTrips = repository.groupTrips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun selectDestination(destination: Destination?) {
        _selectedDestination.value = destination
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: DestinationCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun selectCircuit(circuit: String) {
        _selectedCircuit.value = circuit
    }

    fun toggleCurrency() {
        _selectedCurrency.value = if (_selectedCurrency.value == "KES") "USD" else "KES"
    }

    fun formatMoney(amountKES: Int): String {
        return if (_selectedCurrency.value == "KES") {
            "KES ${"%,d".format(amountKES)}"
        } else {
            val usd = (amountKES / exchangeRateUsdToKes).toInt().coerceAtLeast(1)
            "USD $$usd"
        }
    }

    fun isDestinationSaved(id: String): Boolean {
        return savedDestinations.value.any { it.destinationId == id }
    }

    fun toggleSaveDestination(id: String) {
        viewModelScope.launch {
            val currentlySaved = isDestinationSaved(id)
            repository.toggleSaveDestination(id, currentlySaved)
            val msg = if (currentlySaved) "Removed from saved" else "Saved to your Kenya wishlist!"
            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun createItinerary(
        title: String,
        destinationId: String,
        destinationName: String,
        startDate: String,
        durationDays: Int,
        numberOfTravelers: Int,
        transportMode: String,
        accommodationTier: String,
        estimatedBudgetKES: Int,
        notes: String,
        activitiesText: String
    ) {
        viewModelScope.launch {
            val newItinerary = ItineraryEntity(
                title = title.ifBlank { "$destinationName Adventure" },
                destinationId = destinationId,
                destinationName = destinationName,
                startDate = startDate.ifBlank { "Flexible Dates" },
                durationDays = durationDays.coerceAtLeast(1),
                numberOfTravelers = numberOfTravelers.coerceAtLeast(1),
                transportMode = transportMode,
                accommodationTier = accommodationTier,
                estimatedBudgetKES = estimatedBudgetKES,
                notes = notes,
                activitiesJson = activitiesText.ifBlank {
                    "Day 1: Departure & Arrival -> Afternoon Orientation & Exploration\nDay 2: Full day adventures and activities\nDay 3: Sunrise highlights & Journey back"
                }
            )
            repository.createItinerary(newItinerary)
            Toast.makeText(getApplication(), "Trip itinerary created!", Toast.LENGTH_SHORT).show()
            _currentTab.value = AppNavTab.ITINERARIES
        }
    }

    fun deleteItinerary(itinerary: ItineraryEntity) {
        viewModelScope.launch {
            repository.deleteItinerary(itinerary)
            Toast.makeText(getApplication(), "Itinerary removed", Toast.LENGTH_SHORT).show()
        }
    }

    fun createGroupTrip(
        tripName: String,
        destinationName: String,
        departureDate: String,
        targetBudgetKES: Int,
        members: String,
        checklist: String
    ) {
        viewModelScope.launch {
            val randomCode = "NOMAD-${(1000..9999).random()}"
            val trip = GroupTripEntity(
                tripName = tripName.ifBlank { "$destinationName Group Getaway" },
                destinationName = destinationName,
                departureDate = departureDate.ifBlank { "Upcoming Weekend" },
                inviteCode = randomCode,
                totalEstimatedBudgetKES = targetBudgetKES.coerceAtLeast(5000),
                currentCollectedKES = (targetBudgetKES * 0.4).toInt(),
                membersSummary = members.ifBlank { "You (Organizer), Sarah (Treasurer), David (Driver)" },
                checklistSummary = checklist.ifBlank { "Park Tickets: Pending, Gear: Packed, Car Fuel Pool: Sorted" }
            )
            repository.createGroupTrip(trip)
            Toast.makeText(getApplication(), "Group Trip '$tripName' created!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteGroupTrip(trip: GroupTripEntity) {
        viewModelScope.launch {
            repository.deleteGroupTrip(trip)
            Toast.makeText(getApplication(), "Group trip removed", Toast.LENGTH_SHORT).show()
        }
    }

    fun openQuoteDialog(operator: TourOperator?) {
        _quoteOperator.value = operator
    }

    fun shareItinerary(context: Context, itinerary: ItineraryEntity) {
        val shareText = """
            🇰🇪 Nomadika Trip Plan: ${itinerary.title}
            📍 Destination: ${itinerary.destinationName}
            🗓 Duration: ${itinerary.durationDays} Days (${itinerary.startDate})
            👥 Travelers: ${itinerary.numberOfTravelers}
            💰 Est. Total Budget: KES ${"%,d".format(itinerary.estimatedBudgetKES)}
            🚗 Transport: ${itinerary.transportMode}
            🏨 Lodging: ${itinerary.accommodationTier}
            
            📋 Plan:
            ${itinerary.activitiesJson}
            
            📝 Notes: ${itinerary.notes}
            
            Created with Nomadika - Local Kenya Travel Planner
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Itinerary with Friends")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun contactOperatorViaWhatsApp(context: Context, operator: TourOperator) {
        val cleanPhone = operator.whatsapp.replace("+", "").replace(" ", "").trim()
        val text = Uri.encode("Hello ${operator.name}, I am planning a trip in Kenya using Nomadika and would like an inquiry quote regarding: ${operator.popularPackage}")
        val url = "https://wa.me/$cleanPhone?text=$text"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: +${cleanPhone}", Toast.LENGTH_SHORT).show()
        }
    }

    fun contactOperatorViaPhone(context: Context, operator: TourOperator) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${operator.phone}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to dial phone number", Toast.LENGTH_SHORT).show()
        }
    }
}

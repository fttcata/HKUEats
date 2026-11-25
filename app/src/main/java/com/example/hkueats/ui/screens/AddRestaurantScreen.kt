package com.example.hkueats.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hkueats.data.AddressResult
import com.example.hkueats.data.AddressSearchService
import com.example.hkueats.data.AddressSuggestion
import com.example.hkueats.data.AppContainer
import com.example.hkueats.data.CampusPlace
import com.example.hkueats.data.Coordinates
import com.example.hkueats.data.Restaurant
import com.example.hkueats.data.RestaurantSaveException
import com.example.hkueats.data.loadCampusPlaces
import com.example.hkueats.data.suggestionsFor
import com.example.hkueats.rememberCurrentLocationRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRestaurantScreen(
    existingRestaurants: List<Restaurant>,
    onAdded: (Restaurant) -> Unit,
) {
    val restaurantRepository = AppContainer.restaurantRepository
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val campusPlaces = remember(context) { loadCampusPlaces(context) }
    val addressSearch = remember { AddressSearchService() }
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedPlace by remember { mutableStateOf<CampusPlace?>(null) }
    var currentCoordinates by remember { mutableStateOf<Coordinates?>(null) }
    var buildingNotOnCampus by remember { mutableStateOf(false) }
    var addressSuggestions by remember { mutableStateOf<List<AddressSuggestion>>(emptyList()) }
    var addressResult by remember { mutableStateOf<AddressResult?>(null) }
    var addressSearchError by remember { mutableStateOf<String?>(null) }
    var isResolvingAddress by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    var cuisine by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("$$") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    val locationRequest = rememberCurrentLocationRequest { coordinates ->
        if (selectedPlace == null) {
            currentCoordinates = coordinates
            errorMessage = null
        }
    }
    val suggestions = remember(campusPlaces, location, selectedPlace) {
        if (!buildingNotOnCampus && selectedPlace == null) {
            campusPlaces.suggestionsFor(location)
        } else {
            emptyList()
        }
    }
    val suggestionsExpanded = showSuggestions && suggestions.isNotEmpty() && !isSubmitting
    val addressSuggestionsExpanded =
        buildingNotOnCampus && showSuggestions && addressSuggestions.isNotEmpty() && !isSubmitting

    fun searchExternalLocation() {
        val query = location.trim()
        if (query.length < 3) {
            addressSearchError = "Enter at least three characters to search."
            return
        }
        if (isResolvingAddress) return

        isResolvingAddress = true
        addressSearchError = null
        addressSuggestions = emptyList()
        showSuggestions = false
        coroutineScope.launch {
            try {
                addressSuggestions = addressSearch.search(query)
                if (addressSuggestions.isEmpty()) {
                    addressSearchError = "No matching places found in Hong Kong. Try a fuller name or address."
                } else {
                    showSuggestions = true
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                addressSearchError = "OpenStreetMap search failed. Check your connection and try again."
            } finally {
                isResolvingAddress = false
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Add a food spot", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(name, { name = it; errorMessage = null }, label = { Text("Restaurant name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Checkbox(
                checked = buildingNotOnCampus,
                enabled = !isSubmitting && !isResolvingAddress,
                onCheckedChange = { checked ->
                    buildingNotOnCampus = checked
                    location = ""
                    selectedPlace = null
                    currentCoordinates = null
                    addressResult = null
                    addressSuggestions = emptyList()
                    addressSearchError = null
                    showSuggestions = false
                    errorMessage = null
                },
            )
            Text("Building or restaurant is not on campus")
        }
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = if (buildingNotOnCampus) addressSuggestionsExpanded else suggestionsExpanded,
            onExpandedChange = { showSuggestions = it },
        ) {
            OutlinedTextField(
                value = location,
                onValueChange = {
                    location = it
                    selectedPlace = null
                    addressResult = null
                    addressSearchError = null
                    addressSuggestions = emptyList()
                    showSuggestions = false
                    errorMessage = null
                },
                label = {
                    Text(if (buildingNotOnCampus) "Restaurant or address" else "HKU building name")
                },
                singleLine = true,
                enabled = !isSubmitting && !isResolvingAddress,
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable, enabled = !isSubmitting && !isResolvingAddress),
            )
            ExposedDropdownMenu(
                expanded = if (buildingNotOnCampus) addressSuggestionsExpanded else suggestionsExpanded,
                onDismissRequest = { showSuggestions = false },
            ) {
                if (buildingNotOnCampus) {
                    addressSuggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion.description) },
                            onClick = {
                                showSuggestions = false
                                isResolvingAddress = true
                                errorMessage = null
                                addressSearchError = null
                                coroutineScope.launch {
                                    try {
                                        val resolved = addressSearch.resolve(suggestion)
                                        if (resolved == null) {
                                            errorMessage = "Could not find coordinates for this address."
                                        } else {
                                            location = resolved.formattedAddress
                                            addressResult = resolved
                                        }
                                    } catch (cancelled: CancellationException) {
                                        throw cancelled
                                    } catch (_: Exception) {
                                        errorMessage = "Could not use this result. Search again and choose another place."
                                    } finally {
                                        isResolvingAddress = false
                                    }
                                }
                            },
                        )
                    }
                } else {
                    suggestions.forEach { place ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(place.name)
                                    Text(place.campus, style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            onClick = {
                                location = place.name
                                selectedPlace = place
                                currentCoordinates = null
                                showSuggestions = false
                                errorMessage = null
                            },
                        )
                    }
                }
            }
        }
        if (buildingNotOnCampus) {
            OutlinedButton(
                onClick = { searchExternalLocation() },
                enabled = !isSubmitting && !isResolvingAddress,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                Text(if (isResolvingAddress) "Searching OpenStreetMap…" else "Search OpenStreetMap")
            }
            Text("© OpenStreetMap contributors", style = MaterialTheme.typography.bodySmall)
            if (isResolvingAddress) {
                Spacer(Modifier.height(8.dp))
                Text("Searching for places…", style = MaterialTheme.typography.bodySmall)
            } else if (addressResult != null) {
                Spacer(Modifier.height(8.dp))
                Text("Map location confirmed", style = MaterialTheme.typography.bodySmall)
            } else if (addressSearchError != null) {
                Spacer(Modifier.height(8.dp))
                Text(addressSearchError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            } else if (location.trim().length >= 3 && addressSuggestions.isEmpty() && !isResolvingAddress) {
                Spacer(Modifier.height(8.dp))
                Text("Search for a restaurant or address, then choose a result to confirm its map pin.", style = MaterialTheme.typography.bodySmall)
            }
        } else if (selectedPlace == null) {
            val hasCurrentLocation = currentCoordinates != null
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    if (hasCurrentLocation) currentCoordinates = null
                    else locationRequest.launch()
                },
                enabled = !isSubmitting && !isResolvingAddress && !locationRequest.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    when {
                        hasCurrentLocation -> "Remove current location"
                        locationRequest.isLoading -> "Finding location…"
                        else -> "Use my current location"
                    },
                )
            }
            if (!hasCurrentLocation) {
                locationRequest.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(cuisine, { cuisine = it }, label = { Text("Cuisine (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Text("Price range", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            listOf("$", "$$", "$$$").forEach { option ->
                FilterChip(selected = price == option, onClick = { price = option }, label = { Text(option) })
            }
        }
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) }
        Button(
            onClick = {
                val cleanName = name.trim()
                val cleanLocation = location.trim()
                when {
                    cleanName.isBlank() -> errorMessage = "Enter a restaurant name."
                    cleanLocation.isBlank() ->
                        errorMessage = if (buildingNotOnCampus) "Enter an address." else "Enter a building name."
                    buildingNotOnCampus && addressResult == null ->
                        errorMessage = "Search OpenStreetMap and select a restaurant or address before saving."
                    existingRestaurants.any { it.name.equals(cleanName, ignoreCase = true) } ->
                        errorMessage = "This restaurant is already listed."
                    isSubmitting -> Unit
                    else -> coroutineScope.launch {
                        isSubmitting = true
                        errorMessage = try {
                            val resolvedAddress = if (buildingNotOnCampus) {
                                addressResult
                            } else {
                                null
                            }
                            val coordinates = if (buildingNotOnCampus) {
                                resolvedAddress?.coordinates
                            } else {
                                selectedPlace?.coordinates ?: currentCoordinates
                            }
                            val finalLocation = if (buildingNotOnCampus) {
                                resolvedAddress?.formattedAddress ?: cleanLocation
                            } else {
                                cleanLocation
                            }
                            if (coordinates == null) {
                                "Select a valid address or add a location pin before saving."
                            } else {
                            val restaurant = restaurantRepository.addRestaurant(
                                name = cleanName,
                                location = finalLocation,
                                coordinates = coordinates,
                                cuisine = cuisine.trim().ifBlank { "Unspecified" },
                                price = price,
                            )
                            if (restaurant == null) "Could not add this restaurant. Please try again."
                            else {
                                onAdded(restaurant)
                                null
                            }
                            }
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (error: RestaurantSaveException) {
                            error.message ?: "Could not add this restaurant. Please try again."
                        } catch (_: Exception) {
                            "Could not add this restaurant. Please try again."
                        } finally {
                            isSubmitting = false
                        }
                    }
                }
            },
            enabled = !isSubmitting && !locationRequest.isLoading && !isResolvingAddress,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (isSubmitting) "Adding…" else "Add restaurant") }
    }
}

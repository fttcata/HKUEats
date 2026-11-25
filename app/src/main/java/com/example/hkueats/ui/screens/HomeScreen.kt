package com.example.hkueats.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.example.hkueats.DistanceOrigin
import com.example.hkueats.data.Coordinates
import com.example.hkueats.data.Restaurant
import com.example.hkueats.distanceTo
import com.example.hkueats.getLocationForDistances
import com.example.hkueats.hasLocationPermission
import com.example.hkueats.hasPreciseLocationPermission
import com.example.hkueats.rememberCurrentLocationRequest
import com.example.hkueats.ui.components.RestaurantCard
import com.example.hkueats.ui.formatRating
import kotlin.math.roundToInt

private const val NEARBY_RADIUS_METERS = 500
private const val QUICK_TRIP_MAX_WALK_MINUTES = 5.0

private data class QuickTripResult(
    val restaurant: Restaurant,
    val distanceMeters: Float,
    val walkMinutes: Double,
)

private fun estimatedQuickTripWalkMinutes(distanceMeters: Float): Double =
    distanceMeters * 1.3 / 80.0

@Composable
fun HomeScreen(
    allRestaurants: List<Restaurant>,
    onRestaurantClick: (Restaurant) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selectedCuisine by remember { mutableStateOf("All") }
    var selectedLocation by remember { mutableStateOf("All") }
    var showQuickTrip by remember { mutableStateOf(false) }
    var quickTripOrigin by remember { mutableStateOf<Coordinates?>(null) }
    var nearbyOrigin by remember { mutableStateOf<Coordinates?>(null) }
    var allRestaurantsOrigin by remember { mutableStateOf<DistanceOrigin?>(null) }
    val context = LocalContext.current
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val locationRequest = rememberCurrentLocationRequest { coordinates ->
        nearbyOrigin = coordinates
        allRestaurantsOrigin = DistanceOrigin(coordinates)
    }
    val quickTripLocationRequest = rememberCurrentLocationRequest { coordinates ->
        quickTripOrigin = coordinates
    }
    LaunchedEffect(lifecycleState, selectedLocation, nearbyOrigin) {
        if (lifecycleState != Lifecycle.State.RESUMED) return@LaunchedEffect
        if (!context.hasPreciseLocationPermission()) nearbyOrigin = null
        if (!context.hasLocationPermission()) {
            allRestaurantsOrigin = null
        } else if (selectedLocation == "All" && nearbyOrigin == null) {
            allRestaurantsOrigin = getLocationForDistances(context)
        }
    }
    val cuisines = listOf("All") + allRestaurants.map { it.cuisine }.distinct().sorted().filterNot { it == "All" }
    val locations = listOf("All") + allRestaurants.map { it.location }.distinct().sorted().filterNot { it == "All" }
    val restaurants = allRestaurants.filter {
        (selectedCuisine == "All" || it.cuisine == selectedCuisine) &&
                (selectedLocation == "All" || it.location == selectedLocation) &&
                (query.isBlank() ||
                        it.name.contains(query, ignoreCase = true) ||
                        it.cuisine.contains(query, ignoreCase = true) ||
                        it.location.contains(query, ignoreCase = true))
    }
    val distanceOrigin = nearbyOrigin?.let { DistanceOrigin(it) }
        ?: allRestaurantsOrigin.takeIf { selectedLocation == "All" }
    val restaurantsWithDistances = restaurants.map { restaurant ->
        restaurant to distanceOrigin?.let { origin -> restaurant.coordinates?.distanceTo(origin.coordinates) }
    }
    val displayedRestaurants = if (nearbyOrigin != null) {
        restaurantsWithDistances.filter { (_, distance) ->
            distance != null && distance <= NEARBY_RADIUS_METERS
        }.sortedBy { it.second }
    } else {
        restaurantsWithDistances
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text("Good afternoon!", style = MaterialTheme.typography.headlineMedium)
        Text("Popular around HKU", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text("Search restaurants") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(
            onClick = {
                showQuickTrip = true
                quickTripOrigin = null
                quickTripLocationRequest.launch()
            },
            enabled = !quickTripLocationRequest.isLoading,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text(if (quickTripLocationRequest.isLoading) "Finding your location…" else "⚡ Quick Trip · under 5 min walk")
        }
        Text("Cuisine", style = MaterialTheme.typography.labelLarge)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            items(cuisines) { cuisine ->
                FilterChip(
                    selected = selectedCuisine == cuisine,
                    onClick = { selectedCuisine = cuisine },
                    label = { Text(cuisine) },
                )
            }
        }
        Text("Location", style = MaterialTheme.typography.labelLarge)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            item {
                FilterChip(
                    selected = nearbyOrigin != null,
                    onClick = {
                        if (nearbyOrigin == null) locationRequest.launch() else nearbyOrigin = null
                    },
                    enabled = !locationRequest.isLoading,
                    label = {
                        Text(if (locationRequest.isLoading) "Finding location…" else "Near me · ${NEARBY_RADIUS_METERS} m")
                    },
                )
            }
            items(locations) { location ->
                FilterChip(
                    selected = selectedLocation == location,
                    onClick = { selectedLocation = location },
                    label = { Text(location) },
                )
            }
        }
        locationRequest.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (displayedRestaurants.isEmpty()) {
                item {
                    Text(
                        if (nearbyOrigin == null) "No restaurants match these filters."
                        else "No matching restaurants within ${NEARBY_RADIUS_METERS} m.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }
            items(displayedRestaurants, key = { it.first.id }) { (restaurant, distanceMeters) ->
                RestaurantCard(
                    restaurant,
                    distanceMeters,
                    distanceOrigin?.approximate == true,
                    onClick = { onRestaurantClick(restaurant) },
                )
            }
        }
    }

    if (showQuickTrip) {
        val quickTripRestaurants = quickTripOrigin?.let { origin ->
            allRestaurants.mapNotNull { restaurant ->
                val distance = restaurant.coordinates?.distanceTo(origin) ?: return@mapNotNull null
                val walkMinutes = estimatedQuickTripWalkMinutes(distance)
                if (walkMinutes >= QUICK_TRIP_MAX_WALK_MINUTES) return@mapNotNull null
                QuickTripResult(restaurant, distance, walkMinutes)
            }.sortedWith(compareBy<QuickTripResult> { it.walkMinutes }.thenByDescending { it.restaurant.rating })
        }.orEmpty()

        AlertDialog(
            onDismissRequest = { showQuickTrip = false },
            title = { Text("Quick Trip") },
            text = {
                Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Nearby places with an estimated walk under 5 minutes.")
                    Text("Times use straight-line distance and may differ from actual walking routes.", style = MaterialTheme.typography.bodySmall)
                    when {
                        quickTripOrigin == null -> Text(
                            if (quickTripLocationRequest.isLoading) "Getting your live location…" else "Your live location is needed to find a quick trip.",
                            color = if (quickTripLocationRequest.error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        quickTripRestaurants.isEmpty() -> Text("No restaurants with location data are under a 5-minute walk from here.")
                        else -> {
                            Text("Quick trips near you", style = MaterialTheme.typography.titleSmall)
                            quickTripRestaurants.take(3).forEach { trip ->
                                Card(
                                    onClick = {
                                        showQuickTrip = false
                                        onRestaurantClick(trip.restaurant)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(trip.restaurant.name, style = MaterialTheme.typography.titleMedium)
                                        Text("Under 5 min walk · ${trip.distanceMeters.roundToInt()} m · ${trip.restaurant.price} · ★ ${trip.restaurant.rating.formatRating()}")
                                        Text(trip.restaurant.location, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                    quickTripLocationRequest.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                if (quickTripOrigin == null && !quickTripLocationRequest.isLoading) {
                    TextButton(
                        onClick = {
                            quickTripOrigin = null
                            quickTripLocationRequest.launch()
                        },
                    ) { Text("Try location again") }
                } else {
                    TextButton(onClick = { showQuickTrip = false }) { Text("Done") }
                }
            },
            dismissButton = { TextButton(onClick = { showQuickTrip = false }) { Text("Close") } },
        )
    }
}

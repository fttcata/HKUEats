package com.example.hkueats

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material3.Checkbox
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch
import com.example.hkueats.data.AppContainer
import com.example.hkueats.data.AddressResult
import com.example.hkueats.data.AddressSearchService
import com.example.hkueats.data.AddressSuggestion
import com.example.hkueats.data.CampusPlace
import com.example.hkueats.data.Coordinates
import com.example.hkueats.data.Restaurant
import com.example.hkueats.data.RestaurantSaveException
import com.example.hkueats.data.Review
import com.example.hkueats.data.UserLoginResult
import com.example.hkueats.data.loadCampusPlaces
import com.example.hkueats.data.suggestionsFor
import com.example.hkueats.ui.theme.HKUEatsTheme
import kotlin.math.roundToInt
import kotlin.math.ceil
import kotlinx.coroutines.CancellationException

private val restaurantRepository = AppContainer.restaurantRepository
private val userRepository = AppContainer.userRepository
private const val NEARBY_RADIUS_METERS = 500

private enum class Screen { Login, Home, Details, AddReview, AddRestaurant, Account }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HKUEatsTheme { HKUEatsApp() } }
    }
}

@Composable
private fun HKUEatsApp() {
    var screen by remember { mutableStateOf(Screen.Login) }
    var currentUserEmail by remember { mutableStateOf("student@connect.hku.hk") }
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    var restaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            restaurants = restaurantRepository.getRestaurants()
        } catch (_: Throwable) {
            // Ignore error and prevent crash on launch
        }
    }

    when (screen) {
        Screen.Login -> LoginScreen { email ->
            currentUserEmail = email
            screen = Screen.Home
        }
        Screen.Home -> AppScaffold(
            screen = screen,
            onNavigate = { screen = it },
            content = {
                HomeScreen(restaurants) { restaurant ->
                    selectedRestaurant = restaurant
                    screen = Screen.Details
                }
            },
        )
        Screen.Details -> AppScaffold(
            screen = screen,
            onNavigate = { screen = it },
            content = {
                selectedRestaurant?.let { restaurant ->
                    RestaurantDetailsScreen(
                        restaurant = restaurant,
                        onBack = { screen = Screen.Home },
                        onWriteReview = { screen = Screen.AddReview },
                    )
                }
            },
        )
        Screen.AddReview -> AppScaffold(
            screen = screen,
            onNavigate = { screen = it },
            content = {
                ReviewScreen(
                    restaurantName = selectedRestaurant?.name.orEmpty(),
                    currentUserEmail = currentUserEmail, // <--- Pass real logged-in user
                    onBack = { screen = Screen.Details },
                    onSubmitted = { restaurant ->
                        selectedRestaurant = restaurant
                        restaurants = restaurants.map {
                            if (it.id == restaurant.id) restaurant else it
                        }
                        screen = Screen.Details
                    },
                )
            },
        )
        Screen.AddRestaurant -> AppScaffold(
            screen = screen,
            onNavigate = { screen = it },
            content = {
                AddRestaurantScreen(
                    existingRestaurants = restaurants,
                    onAdded = { restaurant ->
                        restaurants = restaurants + restaurant
                        screen = Screen.Home
                    },
                )
            },
        )
        Screen.Account -> AppScaffold(
            screen = screen,
            onNavigate = { screen = it },
            content = {
                AccountScreen(
                    email = currentUserEmail,
                    allRestaurants = restaurants, // <--- Pass live restaurant/review data
                    onSignOut = { screen = Screen.Login }
                )
            },
        )
    }
}

@Composable
private fun AppScaffold(
    screen: Screen,
    onNavigate: (Screen) -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = screen == Screen.Home || screen == Screen.Details || screen == Screen.AddReview,
                    onClick = { onNavigate(Screen.Home) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Explore") },
                )
                NavigationBarItem(
                    selected = screen == Screen.AddRestaurant,
                    onClick = { onNavigate(Screen.AddRestaurant) },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    label = { Text("Add place") },
                )
                NavigationBarItem(
                    selected = screen == Screen.Account,
                    onClick = { onNavigate(Screen.Account) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Account") },
                    label = { Text("Account") },
                )
            }
        },
        content = { padding ->
            Column(Modifier.padding(padding)) { content() }
        },
    )
}

@Composable
private fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var emailInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("HKUEats", style = MaterialTheme.typography.headlineLarge)
        Text("Find your next student-friendly meal.", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text("HKU email (@connect.hku.hk)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val trimmedEmail = emailInput.trim()
                when (userRepository.validateHkuEmail(trimmedEmail)) {
                    UserLoginResult.Success -> onLoginSuccess(trimmedEmail)
                    UserLoginResult.InvalidEmail -> {
                        errorMessage = "Access restricted to HKU accounts (@connect.hku.hk or @hku.hk)."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue with email")
        }

        errorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Text(
            "Demo access only. Microsoft sign-in is not connected yet.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun HomeScreen(
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
                                        Text(pick.restaurant.name, style = MaterialTheme.typography.titleMedium)
                                        Text("About ${pick.walkMinutes} min · ${pick.distanceMeters.roundToInt()} m · ${pick.restaurant.price} · ★ ${pick.restaurant.rating}")
                                        Text(pick.restaurant.location, style = MaterialTheme.typography.bodySmall)
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

private data class QuickTripResult(
    val restaurant: Restaurant,
    val distanceMeters: Float,
    val walkMinutes: Double,
)

private fun estimatedQuickTripWalkMinutes(distanceMeters: Float): Double =
    distanceMeters * 1.3 / 80.0

@Composable
private fun RestaurantCard(
    restaurant: Restaurant,
    distanceMeters: Float?,
    approximateDistance: Boolean,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            restaurant.imageResId?.let { imageResId ->
                Image(
                    painter = painterResource(imageResId),
                    contentDescription = "${restaurant.name} photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(bottom = 12.dp),
                )
            }
            Text(restaurant.name, style = MaterialTheme.typography.titleLarge)
            Text("${restaurant.cuisine} · ${restaurant.price}", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("★ ${restaurant.rating.formatRating()}", style = MaterialTheme.typography.bodyLarge)
                Text("  ·  ${restaurant.location}", style = MaterialTheme.typography.bodyMedium)
            }
            if (distanceMeters != null) {
                val distanceLabel = if (distanceMeters < 1_000) {
                    "${distanceMeters.roundToInt()} m away"
                } else {
                    String.format(Locale.getDefault(), "%.1f km away", distanceMeters / 1_000)
                }
                Text(
                    if (approximateDistance) "About ${distanceLabel}" else distanceLabel,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RestaurantDetailsScreen(
    restaurant: Restaurant,
    onBack: () -> Unit,
    onWriteReview: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(restaurant.name) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
        )
        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            item {
                restaurant.imageResId?.let { imageResId ->
                    Image(
                        painter = painterResource(imageResId),
                        contentDescription = "${restaurant.name} photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(bottom = 12.dp),
                    )
                }
                Text("${restaurant.cuisine} · ${restaurant.price}", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("★ ${restaurant.rating.formatRating()}", style = MaterialTheme.typography.headlineSmall)
                    Text("  Based on ${restaurant.reviews.size} student reviews")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Text("  ${restaurant.location}")
                }
                Button(onClick = onWriteReview, modifier = Modifier.fillMaxWidth()) { Text("Write a review") }
                Spacer(Modifier.height(16.dp))
                Text("Recent reviews", style = MaterialTheme.typography.titleLarge)
            }
            items(restaurant.reviews) { review ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(review.author, style = MaterialTheme.typography.titleMedium)
                        Text("★ ${review.rating.formatRating()}", style = MaterialTheme.typography.bodyLarge)
                        if (review.visitDate.isNotBlank()) {
                            Text("Visited ${review.visitDate}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(review.comment, Modifier.padding(top = 4.dp))
                        if (review.imageUri != null) {
                            Text("Photo attached", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ReviewScreen(
    restaurantName: String,
    currentUserEmail: String,
    onBack: () -> Unit,
    onSubmitted: (Restaurant) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var visitDate by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            imageUri = uri.toString()
            errorMessage = null
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        TopAppBar(
            title = { Text("Write a review") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
        )
        Text(restaurantName, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Text("Your rating", style = MaterialTheme.typography.labelLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            (1..5).forEach { value ->
                FilterChip(
                    selected = rating == value,
                    onClick = { rating = value },
                    label = { Text("$value ★") },
                )
            }
        }
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it; errorMessage = null },
            label = { Text("Comment") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        calendar.set(year, month, day)
                        visitDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                ).show()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (visitDate.isBlank()) "Select visit date" else "Visit date: $visitDate")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { imagePicker.launch(arrayOf("image/*")) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (imageUri == null) "Add an optional photo" else "Photo selected")
        }
        if (imageUri != null) {
            OutlinedButton(
                onClick = { imageUri = null },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Remove photo") }
        }
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                when {
                    rating == 0 -> errorMessage = "Please select a rating."
                    comment.isBlank() -> errorMessage = "Please enter a comment."
                    visitDate.isBlank() -> errorMessage = "Please select your visit date."
                    isSubmitting -> Unit
                    else -> coroutineScope.launch {
                        isSubmitting = true
                        errorMessage = try {
                            val updatedRestaurant = restaurantRepository.addReview(
                                restaurantName = restaurantName,
                                authorEmail = currentUserEmail,
                                rating = rating.toDouble(),
                                comment = comment.trim(),
                                visitDate = visitDate,
                                imageUri = imageUri,
                            )
                            if (updatedRestaurant == null) "Could not save your review. Please try again."
                            else {
                                onSubmitted(updatedRestaurant)
                                null
                            }
                        } catch (_: Exception) {
                            "Could not save your review. Please try again."
                        }
                        isSubmitting = false
                    }
                }
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isSubmitting) "Submitting…" else "Submit review")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRestaurantScreen(
    existingRestaurants: List<Restaurant>,
    onAdded: (Restaurant) -> Unit,
) {
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

@Composable
private fun AccountScreen(
    email: String,
    allRestaurants: List<Restaurant>,
    onSignOut: () -> Unit
) {
    val userHandle = email.substringBefore("@").replaceFirstChar { it.uppercase() }

    // Filter all reviews across all restaurants to find ones written by this user
    val userReviews: List<Pair<String, Review>> = remember(allRestaurants, email) {
        allRestaurants.flatMap { restaurant ->
            restaurant.reviews
                .filter { review ->
                    review.author.equals(email, ignoreCase = true) ||
                            review.author.equals(userHandle, ignoreCase = true)
                }
                .map { review -> restaurant.name to review }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Account and settings", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))

            // Profile Info Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Column {
                        Text(userHandle, style = MaterialTheme.typography.titleLarge)
                        Text(
                            email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text("Your reviews (${userReviews.size})", style = MaterialTheme.typography.titleMedium)
        }

        if (userReviews.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "You haven't written any reviews yet. Explore restaurants to share your first review!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(userReviews) { (restaurantName, review) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "$restaurantName · ★ ${review.rating.formatRating()}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (review.visitDate.isNotBlank()) {
                            Text(
                                "Visited ${review.visitDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            review.comment,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Out")
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "HKUEats · COMP3330 Group Project",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

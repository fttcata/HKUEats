package com.example.hkueats

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.hkueats.data.Restaurant
import com.example.hkueats.data.RestaurantRepository
import com.example.hkueats.ui.theme.HKUEatsTheme

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
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }

    when (screen) {
        Screen.Login -> LoginScreen { screen = Screen.Home }
        Screen.Home -> AppScaffold(
            screen = screen,
            onNavigate = { screen = it },
            content = {
                HomeScreen { restaurant ->
                    selectedRestaurant = restaurant
                    screen = Screen.Details
                }
            },
        )
        Screen.Details -> AppScaffold(
            screen = screen,
            onNavigate = { screen = it },
            content = {
                RestaurantDetailsScreen(
                    restaurant = selectedRestaurant ?: RestaurantRepository.restaurants.first(),
                    onBack = { screen = Screen.Home },
                    onWriteReview = { screen = Screen.AddReview },
                )
            },
        )
        Screen.AddReview -> AppScaffold(
            screen = screen,
            onNavigate = { screen = it },
            content = {
                ReviewScreen(
                    restaurantName = selectedRestaurant?.name
                        ?: RestaurantRepository.restaurants.first().name,
                    onBack = { screen = Screen.Details },
                    onSubmitted = { restaurant ->
                        selectedRestaurant = restaurant
                        screen = Screen.Details
                    },
                )
            },
        )
        Screen.AddRestaurant -> AppScaffold(
            screen = screen,
            onNavigate = { screen = it },
            content = { AddRestaurantScreen() },
        )
        Screen.Account -> AppScaffold(
            screen = screen,
            onNavigate = { screen = it },
            content = { AccountScreen() },
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
private fun LoginScreen(onLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("HKUEats", style = MaterialTheme.typography.headlineLarge)
        Text("Find your next student-friendly meal.", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("HKU email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
            Text("Sign in with HKU")
        }
        Text(
            "Microsoft OAuth will replace this prototype form.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun HomeScreen(onRestaurantClick: (Restaurant) -> Unit) {
    var query by remember { mutableStateOf("") }
    var selectedCuisine by remember { mutableStateOf("All") }
    var selectedLocation by remember { mutableStateOf("All") }
    val cuisines = listOf("All", "Western", "Chinese", "Cafe", "Hong Kong")
    val locations = listOf("All", "HKU Main Campus", "Pok Fu Lam", "Sai Ying Pun", "Kennedy Town")
    val restaurants = RestaurantRepository.restaurants.filter {
        (selectedCuisine == "All" || it.cuisine == selectedCuisine) &&
            (selectedLocation == "All" || it.location == selectedLocation) &&
            (query.isBlank() ||
                it.name.contains(query, ignoreCase = true) ||
                it.cuisine.contains(query, ignoreCase = true) ||
                it.location.contains(query, ignoreCase = true))
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
        Text("Cuisine", style = MaterialTheme.typography.labelLarge)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            cuisines.forEach { cuisine ->
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
            locations.forEach { location ->
                FilterChip(
                    selected = selectedLocation == location,
                    onClick = { selectedLocation = location },
                    label = { Text(location) },
                )
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (restaurants.isEmpty()) {
                item {
                    Text(
                        "No restaurants match these filters.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }
            items(restaurants, key = { it.name }) { restaurant ->
                RestaurantCard(restaurant, onClick = { onRestaurantClick(restaurant) })
            }
        }
    }
}

@Composable
private fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(restaurant.name, style = MaterialTheme.typography.titleLarge)
            Text("${restaurant.cuisine} · ${restaurant.price}", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("★ ${restaurant.rating}", style = MaterialTheme.typography.bodyLarge)
                Text("  ·  ${restaurant.location}", style = MaterialTheme.typography.bodyMedium)
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
                Text("${restaurant.cuisine} · ${restaurant.price}", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("★ ${restaurant.rating}", style = MaterialTheme.typography.headlineSmall)
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
                        Text("★ ${review.rating}", style = MaterialTheme.typography.bodyLarge)
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
    onBack: () -> Unit,
    onSubmitted: (Restaurant) -> Unit,
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var visitDate by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        imageUri = uri?.toString()
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
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
            onValueChange = { comment = it },
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
                    else -> RestaurantRepository.addReview(
                        restaurantName = restaurantName,
                        rating = rating.toDouble(),
                        comment = comment.trim(),
                        visitDate = visitDate,
                        imageUri = imageUri,
                    )?.let(onSubmitted)
                }
            },
            enabled = rating > 0 && comment.isNotBlank() && visitDate.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Submit review")
        }
    }
}

@Composable
private fun AddRestaurantScreen() {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Add a food spot", style = MaterialTheme.typography.headlineMedium)
        Text("Help other HKU students discover it.")
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Restaurant name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(location, { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Use current location") }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { submitted = true },
            enabled = name.isNotBlank() && location.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Submit for review") }
        if (submitted) Text("Thanks! Your suggestion is ready for verification.", Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun AccountScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Account and settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        Text("HKU student", style = MaterialTheme.typography.titleLarge)
        Text("student@hku.hk", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Text("Your reviews", style = MaterialTheme.typography.titleMedium)
        Text("Reviews and saved places will appear here once connected to Supabase.")
    }
}

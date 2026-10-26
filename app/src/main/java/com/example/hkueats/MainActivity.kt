package com.example.hkueats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.hkueats.ui.theme.HKUEatsTheme

data class Restaurant(
    val name: String,
    val cuisine: String,
    val location: String,
    val rating: Double,
    val price: String,
)

private val sampleRestaurants = listOf(
    Restaurant("Jaspas Beach Club", "Western", "Pok Fu Lam", 4.4, "$$"),
    Restaurant("Mido Cafe", "Hong Kong", "Sai Ying Pun", 4.2, "$"),
    Restaurant("The Caffeine Laboratory", "Cafe", "Kennedy Town", 4.6, "$$"),
    Restaurant("Wang Fu", "Chinese", "HKU Main Campus", 4.1, "$"),
)

private enum class Screen { Login, Home, Details, AddRestaurant, Account }

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
                    restaurant = selectedRestaurant ?: sampleRestaurants.first(),
                    onBack = { screen = Screen.Home },
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
                    selected = screen == Screen.Home || screen == Screen.Details,
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
    val cuisines = listOf("All", "Western", "Chinese", "Cafe", "Hong Kong")
    val restaurants = sampleRestaurants.filter {
        (selectedCuisine == "All" || it.cuisine == selectedCuisine) &&
            (query.isBlank() || it.name.contains(query, ignoreCase = true))
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 12.dp)) {
            cuisines.forEach { cuisine ->
                FilterChip(
                    selected = selectedCuisine == cuisine,
                    onClick = { selectedCuisine = cuisine },
                    label = { Text(cuisine) },
                )
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(restaurants) { restaurant ->
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
private fun RestaurantDetailsScreen(restaurant: Restaurant, onBack: () -> Unit) {
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
                    Text("  Based on student reviews")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Text("  ${restaurant.location}")
                }
                Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Write a review") }
                Spacer(Modifier.height(16.dp))
                Text("Recent reviews", style = MaterialTheme.typography.titleLarge)
            }
            items(listOf("Great value and friendly staff.", "A convenient option after class.")) { review ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text("Anonymous HKU student\n★ 4.0\n$review", Modifier.padding(12.dp))
                }
            }
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

package com.example.hkueats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.hkueats.data.AppContainer
import com.example.hkueats.data.Restaurant
import com.example.hkueats.ui.components.AppScaffold
import com.example.hkueats.ui.navigation.Screen
import com.example.hkueats.ui.screens.AccountScreen
import com.example.hkueats.ui.screens.AddRestaurantScreen
import com.example.hkueats.ui.screens.HomeScreen
import com.example.hkueats.ui.screens.LoginScreen
import com.example.hkueats.ui.screens.RestaurantDetailsScreen
import com.example.hkueats.ui.screens.ReviewScreen
import com.example.hkueats.ui.theme.HKUEatsTheme
import kotlinx.coroutines.CancellationException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HKUEatsTheme { HKUEatsApp() } }
    }
}

@Composable
private fun HKUEatsApp() {
    val restaurantRepository = AppContainer.restaurantRepository
    var screen by remember { mutableStateOf(Screen.Login) }
    var currentUserEmail by remember { mutableStateOf("student@connect.hku.hk") }
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    var restaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    var restaurantLoadError by remember { mutableStateOf<String?>(null) }
    var loadAttempt by remember { mutableStateOf(0) }

    LaunchedEffect(loadAttempt) {
        try {
            restaurants = restaurantRepository.getRestaurants()
            restaurantLoadError = null
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            restaurantLoadError = "Could not load restaurants. Check your connection and try again."
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
                    currentUserEmail = currentUserEmail,
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
                    allRestaurants = restaurants,
                    onSignOut = { screen = Screen.Login }
                )
            },
        )
    }

    restaurantLoadError?.let { message ->
        AlertDialog(
            onDismissRequest = { restaurantLoadError = null },
            title = { Text("Unable to load restaurants") },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        restaurantLoadError = null
                        loadAttempt += 1
                    },
                ) { Text("Retry") }
            },
            dismissButton = {
                TextButton(onClick = { restaurantLoadError = null }) { Text("Dismiss") }
            },
        )
    }
}

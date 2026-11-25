package com.example.hkueats.ui.screens

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hkueats.data.Restaurant
import com.example.hkueats.data.Review
import com.example.hkueats.ui.formatRating

@Composable
fun AccountScreen(
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

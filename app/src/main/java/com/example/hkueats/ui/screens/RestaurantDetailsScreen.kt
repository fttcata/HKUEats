package com.example.hkueats.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hkueats.data.Restaurant
import com.example.hkueats.ui.formatRating

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RestaurantDetailsScreen(
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

package com.example.hkueats.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hkueats.data.Restaurant
import com.example.hkueats.ui.formatRating
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    distanceMeters: Float?,
    approximateDistance: Boolean,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            if (restaurant.imageUri != null || restaurant.imageResId != null) {
                RestaurantPhoto(
                    imageUri = restaurant.imageUri,
                    imageResId = restaurant.imageResId,
                    contentDescription = "${restaurant.name} photo",
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

package com.example.hkueats.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage

@Composable
fun RestaurantPhoto(
    imageUri: String?,
    @DrawableRes imageResId: Int?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    if (!imageUri.isNullOrBlank()) {
        AsyncImage(
            model = imageUri,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        imageResId?.let {
            Image(
                painter = painterResource(it),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier,
            )
        }
    }
}
package com.example.hkueats.ui.screens

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hkueats.data.AppContainer
import com.example.hkueats.data.Restaurant
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReviewScreen(
    restaurantName: String,
    currentUserEmail: String,
    onBack: () -> Unit,
    onSubmitted: (Restaurant) -> Unit,
) {
    val restaurantRepository = AppContainer.restaurantRepository
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

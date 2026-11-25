package com.example.hkueats.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.hkueats.ui.navigation.Screen

@Composable
fun AppScaffold(
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

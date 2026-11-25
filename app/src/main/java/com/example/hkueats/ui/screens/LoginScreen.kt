package com.example.hkueats.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hkueats.data.AppContainer
import com.example.hkueats.data.UserLoginResult

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    val userRepository = AppContainer.userRepository
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

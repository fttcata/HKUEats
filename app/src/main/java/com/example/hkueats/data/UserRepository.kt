package com.example.hkueats.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class UserProfileRow(
    val id: String,
    val email: String,
    @SerialName("display_name") val displayName: String? = null,
)

interface UserRepository {
    suspend fun handleUserLogin(userId: String, email: String, displayName: String?): Boolean
}

class SupabaseUserRepository(
    private val httpClient: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }
    },
) : UserRepository {

    override suspend fun handleUserLogin(userId: String, email: String, displayName: String?): Boolean {
        // Enforce HKU Domain Boundary
        if (!email.endsWith("@connect.hku.hk") && !email.endsWith("@hku.hk")) {
            return false // Reject non-HKU emails
        }

        if (!SupabaseConfig.isConfigured) {
            // Mock success for local testing before Supabase keys are populated
            return true
        }

        return try {
            val profile = UserProfileRow(
                id = userId,
                email = email,
                displayName = displayName ?: email.substringBefore("@"),
            )

            // Upsert user profile into Supabase PostgreSQL database
            httpClient.post("${SupabaseConfig.baseUrl}/rest/v1/profiles") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.apiKey}")
                header("apikey", SupabaseConfig.apiKey)
                header("Prefer", "resolution=merge-duplicates") // Upsert behavior
                setBody(profile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
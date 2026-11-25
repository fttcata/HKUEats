package com.example.hkueats.data

import android.os.SystemClock
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class AddressSuggestion(
    val placeId: String,
    val description: String,
    val coordinates: Coordinates,
)

data class AddressResult(
    val formattedAddress: String,
    val coordinates: Coordinates,
)

@Serializable
private data class NominatimPlace(
    @SerialName("place_id") val placeId: Long = 0,
    @SerialName("display_name") val displayName: String = "",
    val lat: String = "",
    val lon: String = "",
)

/** Public, keyless place lookup for the prototype's explicitly requested searches. */
class AddressSearchService(
    private val httpClient: HttpClient = HttpClient(Android) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }
    },
) {
    private var lastRequestAtMillis = 0L
    private val cache = mutableMapOf<String, List<AddressSuggestion>>()

    suspend fun search(query: String): List<AddressSuggestion> {
        val normalizedQuery = query.trim()
        require(normalizedQuery.length >= 3) { "Enter at least three characters to search." }
        val cacheKey = normalizedQuery.lowercase()
        cache[cacheKey]?.let { return it }

        val waitMillis = MIN_REQUEST_INTERVAL_MS - (SystemClock.elapsedRealtime() - lastRequestAtMillis)
        if (waitMillis > 0) delay(waitMillis)
        lastRequestAtMillis = SystemClock.elapsedRealtime()

        val places = httpClient
            .get("https://nominatim.openstreetmap.org/search") {
                header(HttpHeaders.UserAgent, USER_AGENT)
                accept(ContentType.Application.Json)
                parameter("q", normalizedQuery)
                parameter("format", "jsonv2")
                parameter("countrycodes", "hk")
                parameter("limit", 5)
                parameter("accept-language", "en")
            }
            .body<List<NominatimPlace>>()

        val results = places.mapNotNull { place ->
            val coordinates = Coordinates.from(place.lat.toDoubleOrNull(), place.lon.toDoubleOrNull())
                ?: return@mapNotNull null
            if (place.displayName.isBlank()) return@mapNotNull null
            AddressSuggestion(
                placeId = place.placeId.toString(),
                description = place.displayName,
                coordinates = coordinates,
            )
        }
        cache[cacheKey] = results
        return results
    }

    fun resolve(suggestion: AddressSuggestion): AddressResult =
        AddressResult(suggestion.description, suggestion.coordinates)

    private companion object {
        const val MIN_REQUEST_INTERVAL_MS = 1_100L
        const val USER_AGENT = "HKUEats/0.1 (Android student project)"
    }
}

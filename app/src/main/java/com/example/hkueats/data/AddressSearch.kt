package com.example.hkueats.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class AddressSuggestion(
    val placeId: String,
    val description: String,
)

data class AddressResult(
    val formattedAddress: String,
    val coordinates: Coordinates,
)

@Serializable
private data class PlaceAutocompleteResponse(
    val predictions: List<PlacePrediction> = emptyList(),
)

@Serializable
private data class PlacePrediction(
    @SerialName("place_id") val placeId: String = "",
    val description: String = "",
)

@Serializable
private data class PlaceDetailsResponse(
    val result: PlaceDetailsResult? = null,
)

@Serializable
private data class PlaceDetailsResult(
    val geometry: PlaceGeometry? = null,
    @SerialName("formatted_address") val formattedAddress: String = "",
)

@Serializable
private data class GeocodingResponse(
    val results: List<GeocodingResult> = emptyList(),
)

@Serializable
private data class GeocodingResult(
    val geometry: PlaceGeometry? = null,
    @SerialName("formatted_address") val formattedAddress: String = "",
)

@Serializable
private data class PlaceGeometry(
    val location: PlaceLocation? = null,
)

@Serializable
private data class PlaceLocation(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
)

class AddressSearchService(
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
) {
    suspend fun search(query: String): List<AddressSuggestion> {
        if (!GoogleMapsConfig.isConfigured) {
            throw IllegalStateException("Google Maps API key is not configured.")
        }

        val response = httpClient
            .get("https://maps.googleapis.com/maps/api/place/autocomplete/json") {
                parameter("input", query)
                parameter("types", "address")
                parameter("key", GoogleMapsConfig.apiKey)
            }
            .body<PlaceAutocompleteResponse>()

        return response.predictions
            .filter { it.placeId.isNotBlank() && it.description.isNotBlank() }
            .map { AddressSuggestion(it.placeId, it.description) }
            .take(5)
    }

    suspend fun resolve(suggestion: AddressSuggestion): AddressResult? {
        if (!GoogleMapsConfig.isConfigured) return null

        val details = httpClient
            .get("https://maps.googleapis.com/maps/api/place/details/json") {
                parameter("place_id", suggestion.placeId)
                parameter("fields", "geometry,formatted_address")
                parameter("key", GoogleMapsConfig.apiKey)
            }
            .body<PlaceDetailsResponse>()
            .result

        val location = details?.geometry?.location
        val coordinates = location?.let { Coordinates.from(it.lat, it.lng) }
        if (coordinates != null) {
            return AddressResult(
                formattedAddress = details?.formattedAddress?.ifBlank { suggestion.description }
                    ?: suggestion.description,
                coordinates = coordinates,
            )
        }

        return geocode(suggestion.description)
    }

    suspend fun geocode(address: String): AddressResult? {
        if (!GoogleMapsConfig.isConfigured) return null

        val result = httpClient
            .get("https://maps.googleapis.com/maps/api/geocode/json") {
                parameter("address", address)
                parameter("key", GoogleMapsConfig.apiKey)
            }
            .body<GeocodingResponse>()
            .results
            .firstOrNull()

        val location = result?.geometry?.location ?: return null
        val coordinates = Coordinates.from(location.lat, location.lng) ?: return null
        return AddressResult(
            formattedAddress = result.formattedAddress.ifBlank { address },
            coordinates = coordinates,
        )
    }
}

package com.example.hkueats.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import java.util.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class RestaurantRow(
    val id: String = "",
    val name: String = "",
    val cuisine: String = "Unspecified",
    val location: String = "",
    val price: String = "$$",
    val rating: Double = 0.0,
)

@Serializable
private data class ReviewRow(
    val id: String = "",
    @SerialName("restaurant_id") val restaurantId: String = "",
    val author: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    @SerialName("visit_date") val visitDate: String? = null,
    @SerialName("image_uri") val imageUri: String? = null,
)

private fun RestaurantRow.toDomain(reviews: List<Review>): Restaurant = Restaurant(
    id = id,
    name = name,
    cuisine = cuisine,
    location = location,
    rating = rating,
    price = price,
    reviews = reviews,
)

private fun ReviewRow.toDomain(): Review = Review(
    id = id,
    author = author,
    rating = rating,
    comment = comment,
    visitDate = visitDate.orEmpty(),
    imageUri = imageUri,
)

class SupabaseRestaurantRepository(
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
) : RestaurantRepository {

    private val fallbackRepository = MockRestaurantRepository()

    override suspend fun getRestaurants(): List<Restaurant> {
        if (!SupabaseConfig.isConfigured) {
            return fallbackRepository.getRestaurants()
        }

        return try {
            val restaurants = httpClient
                .get("${SupabaseConfig.baseUrl}/rest/v1/restaurants") {
                    accept(io.ktor.http.ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.apiKey}")
                    header("apikey", SupabaseConfig.apiKey)
                    parameter("select", "id,name,cuisine,location,price,rating")
                }
                .body<List<RestaurantRow>>()

            val reviews = httpClient
                .get("${SupabaseConfig.baseUrl}/rest/v1/reviews") {
                    accept(io.ktor.http.ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.apiKey}")
                    header("apikey", SupabaseConfig.apiKey)
                    parameter("select", "id,restaurant_id,author,rating,comment,visit_date,image_uri")
                }
                .body<List<ReviewRow>>()

            restaurants.map { restaurantRow ->
                val restaurantReviews = reviews
                    .filter { it.restaurantId == restaurantRow.id }
                    .map(ReviewRow::toDomain)
                restaurantRow.toDomain(restaurantReviews)
            }
        } catch (_: Exception) {
            fallbackRepository.getRestaurants()
        }
    }

    override suspend fun getRestaurantByName(name: String): Restaurant? =
        getRestaurants().firstOrNull { it.name.equals(name, ignoreCase = true) }

    override suspend fun addReview(
        restaurantName: String,
        rating: Double,
        comment: String,
        visitDate: String,
        imageUri: String?,
    ): Restaurant? {
        if (!SupabaseConfig.isConfigured) {
            return fallbackRepository.addReview(restaurantName, rating, comment, visitDate, imageUri)
        }

        return try {
            val restaurant = getRestaurantByName(restaurantName) ?: return null
            val reviewInsert = ReviewRow(
                id = UUID.randomUUID().toString(),
                restaurantId = restaurant.id,
                author = "You",
                rating = rating,
                comment = comment,
                visitDate = visitDate,
                imageUri = imageUri,
            )

            httpClient.post("${SupabaseConfig.baseUrl}/rest/v1/reviews") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.apiKey}")
                header("apikey", SupabaseConfig.apiKey)
                setBody(reviewInsert)
            }

            getRestaurantByName(restaurantName)
        } catch (_: Exception) {
            fallbackRepository.addReview(restaurantName, rating, comment, visitDate, imageUri)
        }
    }

    override suspend fun addRestaurant(
        name: String,
        location: String,
        cuisine: String,
        price: String,
    ): Restaurant? {
        if (!SupabaseConfig.isConfigured) {
            return fallbackRepository.addRestaurant(name, location, cuisine, price)
        }

        return try {
            val restaurantInsert = RestaurantRow(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                cuisine = cuisine.ifBlank { "Unspecified" },
                location = location.trim(),
                price = price.ifBlank { "$$" },
                rating = 0.0,
            )

            httpClient.post("${SupabaseConfig.baseUrl}/rest/v1/restaurants") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${SupabaseConfig.apiKey}")
                header("apikey", SupabaseConfig.apiKey)
                setBody(restaurantInsert)
            }

            getRestaurantByName(name)
        } catch (_: Exception) {
            fallbackRepository.addRestaurant(name, location, cuisine, price)
        }
    }
}

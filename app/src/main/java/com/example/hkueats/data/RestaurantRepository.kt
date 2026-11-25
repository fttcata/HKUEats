package com.example.hkueats.data

import androidx.annotation.DrawableRes
import java.util.UUID
import kotlin.math.roundToInt

private const val DEFAULT_RESTAURANT_CUISINE = "Unspecified"
private const val DEFAULT_RESTAURANT_PRICE = "$$"

data class Review(
    val id: String = UUID.randomUUID().toString(),
    val author: String,
    val rating: Double,
    val comment: String,
    val visitDate: String = "",
    val imageUri: String? = null,
)

@ConsistentCopyVisibility
data class Coordinates private constructor(val latitude: Double, val longitude: Double) {
    companion object {
        fun from(latitude: Double?, longitude: Double?): Coordinates? {
            if (latitude == null || longitude == null) return null
            if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null
            return Coordinates(latitude, longitude)
        }
    }
}

data class Restaurant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val cuisine: String,
    val location: String,
    val rating: Double,
    val price: String,
    val reviews: List<Review>,
    val coordinates: Coordinates? = null,
    @DrawableRes val imageResId: Int? = null,
    val imageUri: String? = null,
)

data class RestaurantPhotoUpload(
    val sourceUri: String,
    val bytes: ByteArray,
    val contentType: String,
)

interface RestaurantRepository {
    suspend fun getRestaurants(): List<Restaurant>
    suspend fun getRestaurantByName(name: String): Restaurant?
    suspend fun getReviewsByAuthor(authorEmail: String): List<Pair<String, Review>>
    suspend fun addReview(
        restaurantName: String,
        authorEmail: String,
        rating: Double,
        comment: String,
        visitDate: String,
        imageUri: String?,
    ): Restaurant?

    suspend fun addRestaurant(
        name: String,
        location: String,
        coordinates: Coordinates,
        cuisine: String = DEFAULT_RESTAURANT_CUISINE,
        price: String = DEFAULT_RESTAURANT_PRICE,
        photo: RestaurantPhotoUpload? = null,
    ): Restaurant?
    suspend fun uploadReviewImage(bytes: ByteArray, mimeType: String): String?
}

class MockRestaurantRepository : RestaurantRepository {
    private val restaurants = mutableListOf(
        Restaurant(
            id = "restaurant-cym",
            name = "CYM Canteen",
            cuisine = "Hong Kong",
            location = "HKU Main Campus",
            coordinates = Coordinates.from(22.28271, 114.13914),
            imageResId = com.example.hkueats.R.drawable.cym_canteen,
            rating = 4.4,
            price = "$$",
            reviews = listOf(
                Review(id = "review-cym-1", author = "mina@connect.hku.hk", rating = 4.5, comment = "Generous portions for a campus lunch."),
                Review(id = "review-cym-2", author = "student@connect.hku.hk", rating = 4.0, comment = "A good choice for a group after class."),
            ),
        ),
        Restaurant(
            id = "restaurant-mido",
            name = "Mido Cafe",
            cuisine = "Hong Kong",
            location = "Yau Ma Tei",
            coordinates = Coordinates.from(22.31023, 114.17037),
            rating = 4.2,
            price = "$",
            reviews = listOf(
                Review(id = "review-mido-1", author = "jason@connect.hku.hk", rating = 4.0, comment = "Fast service and a very student-friendly price."),
                Review(id = "review-mido-2", author = "student@connect.hku.hk", rating = 4.5, comment = "The classic cafe food is reliable."),
            ),
        ),
        Restaurant(
            id = "restaurant-starbucks-composite",
            name = "Starbucks Coffee (Composite Building)",
            cuisine = "Cafe",
            location = "HKU Main Campus",
            coordinates = Coordinates.from(22.28317, 114.13590),
            imageResId = com.example.hkueats.R.drawable.starbucks_composite,
            rating = 4.6,
            price = "$$",
            reviews = listOf(
                Review(id = "review-starbucks-1", author = "sofia@connect.hku.hk", rating = 5.0, comment = "Great coffee and plenty of space to study."),
                Review(id = "review-starbucks-2", author = "student@connect.hku.hk", rating = 4.0, comment = "Quiet in the afternoon and easy to reach."),
            ),
        ),
        Restaurant(
            id = "restaurant-bijas",
            name = "BIJAS Vegetarian",
            cuisine = "Vegetarian",
            location = "HKU Centennial Campus",
            coordinates = Coordinates.from(22.28372, 114.13443),
            rating = 4.1,
            price = "$",
            reviews = listOf(
                Review(id = "review-bijas-1", author = "chris@connect.hku.hk", rating = 4.0, comment = "Convenient between lectures and good value."),
                Review(id = "review-bijas-2", author = "student@connect.hku.hk", rating = 4.0, comment = "The closest option when I have a short break."),
            ),
        ),
    )

    override suspend fun getRestaurants(): List<Restaurant> = restaurants.toList()

    override suspend fun getRestaurantByName(name: String): Restaurant? =
        restaurants.firstOrNull { it.name.equals(name, ignoreCase = true) }

    override suspend fun getReviewsByAuthor(authorEmail: String): List<Pair<String, Review>> {
        return restaurants.flatMap { restaurant ->
            restaurant.reviews
                .filter { it.author.equals(authorEmail, ignoreCase = true) }
                .map { review -> restaurant.name to review }
        }
    }

    override suspend fun addReview(
        restaurantName: String,
        authorEmail: String,
        rating: Double,
        comment: String,
        visitDate: String,
        imageUri: String?,
    ): Restaurant? {
        val index = restaurants.indexOfFirst { it.name.equals(restaurantName, ignoreCase = true) }
        if (index == -1) return null

        val restaurant = restaurants[index]
        val updatedReviews = restaurant.reviews + Review(
            author = authorEmail.ifBlank { "student@connect.hku.hk" },
            rating = rating,
            comment = comment,
            visitDate = visitDate,
            imageUri = imageUri,
        )
        val rawAverage = updatedReviews.map { it.rating }.average()
        val updatedRating = (rawAverage * 10.0).roundToInt() / 10.0
        val updatedRestaurant = restaurant.copy(
            rating = updatedRating,
            reviews = updatedReviews,
        )
        restaurants[index] = updatedRestaurant
        return updatedRestaurant
    }

    override suspend fun addRestaurant(
        name: String,
        location: String,
        coordinates: Coordinates,
        cuisine: String,
        price: String,
        photo: RestaurantPhotoUpload?,
    ): Restaurant? {
        val trimmedName = name.trim()
        val trimmedLocation = location.trim()

        if (trimmedName.isBlank() || trimmedLocation.isBlank()) return null
        if (restaurants.any { it.name.equals(trimmedName, ignoreCase = true) }) return null

        val newRestaurant = Restaurant(
            id = "restaurant-${UUID.randomUUID()}",
            name = trimmedName,
            cuisine = cuisine.ifBlank { DEFAULT_RESTAURANT_CUISINE },
            location = trimmedLocation,
            coordinates = coordinates,
            rating = 0.0,
            price = price.ifBlank { DEFAULT_RESTAURANT_PRICE },
            reviews = emptyList(),
            imageUri = photo?.sourceUri,
        )
        restaurants.add(newRestaurant)
        return newRestaurant
    }

    // The offline demo repository has no remote storage; callers keep the local photo URI.
    override suspend fun uploadReviewImage(bytes: ByteArray, mimeType: String): String? = null
}

object AppContainer {
    val restaurantRepository: RestaurantRepository = if (SupabaseConfig.isConfigured) {
        SupabaseRestaurantRepository()
    } else {
        MockRestaurantRepository()
    }

    val userRepository: UserRepository = HkuEmailUserRepository()
}

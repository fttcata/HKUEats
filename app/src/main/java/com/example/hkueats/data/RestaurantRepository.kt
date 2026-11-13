package com.example.hkueats.data

import java.util.UUID

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

data class Restaurant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val cuisine: String,
    val location: String,
    val rating: Double,
    val price: String,
    val reviews: List<Review>,
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
        cuisine: String = DEFAULT_RESTAURANT_CUISINE,
        price: String = DEFAULT_RESTAURANT_PRICE,
    ): Restaurant?
}

class MockRestaurantRepository : RestaurantRepository {
    private val restaurants = mutableListOf(
        Restaurant(
            id = "restaurant-jaspas",
            name = "Jaspas Beach Club",
            cuisine = "Western",
            location = "Pok Fu Lam",
            rating = 4.4,
            price = "$$",
            reviews = listOf(
                Review(id = "review-jaspas-1", author = "mina@connect.hku.hk", rating = 4.5, comment = "Relaxed atmosphere and generous portions."),
                Review(id = "review-jaspas-2", author = "student@connect.hku.hk", rating = 4.0, comment = "A good choice for a group after class."),
            ),
        ),
        Restaurant(
            id = "restaurant-mido",
            name = "Mido Cafe",
            cuisine = "Hong Kong",
            location = "Sai Ying Pun",
            rating = 4.2,
            price = "$",
            reviews = listOf(
                Review(id = "review-mido-1", author = "jason@connect.hku.hk", rating = 4.0, comment = "Fast service and a very student-friendly price."),
                Review(id = "review-mido-2", author = "student@connect.hku.hk", rating = 4.5, comment = "The classic cafe food is reliable."),
            ),
        ),
        Restaurant(
            id = "restaurant-caffeine",
            name = "The Caffeine Laboratory",
            cuisine = "Cafe",
            location = "Kennedy Town",
            rating = 4.6,
            price = "$$",
            reviews = listOf(
                Review(id = "review-caffeine-1", author = "sofia@connect.hku.hk", rating = 5.0, comment = "Great coffee and plenty of space to study."),
                Review(id = "review-caffeine-2", author = "student@connect.hku.hk", rating = 4.0, comment = "Quiet in the afternoon and easy to reach."),
            ),
        ),
        Restaurant(
            id = "restaurant-wang-fu",
            name = "Wang Fu",
            cuisine = "Chinese",
            location = "HKU Main Campus",
            rating = 4.1,
            price = "$",
            reviews = listOf(
                Review(id = "review-wang-fu-1", author = "chris@connect.hku.hk", rating = 4.0, comment = "Convenient between lectures and good value."),
                Review(id = "review-wang-fu-2", author = "student@connect.hku.hk", rating = 4.0, comment = "The closest option when I have a short break."),
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
        val updatedRating = updatedReviews.map { it.rating }.average()
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
        cuisine: String,
        price: String,
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
            rating = 0.0,
            price = price.ifBlank { DEFAULT_RESTAURANT_PRICE },
            reviews = emptyList(),
        )
        restaurants.add(newRestaurant)
        return newRestaurant
    }
}

object AppContainer {
    val restaurantRepository: RestaurantRepository = if (SupabaseConfig.isConfigured) {
        SupabaseRestaurantRepository()
    } else {
        MockRestaurantRepository()
    }

    val userRepository: UserRepository = HkuEmailUserRepository()
}
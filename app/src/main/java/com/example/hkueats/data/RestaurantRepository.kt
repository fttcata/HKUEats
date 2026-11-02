package com.example.hkueats.data

data class Review(
    val author: String,
    val rating: Double,
    val comment: String,
)

data class Restaurant(
    val name: String,
    val cuisine: String,
    val location: String,
    val rating: Double,
    val price: String,
    val reviews: List<Review>,
)

object RestaurantRepository {
    val restaurants = listOf(
        Restaurant(
            name = "Jaspas Beach Club",
            cuisine = "Western",
            location = "Pok Fu Lam",
            rating = 4.4,
            price = "$$",
            reviews = listOf(
                Review("Mina, Year 2", 4.5, "Relaxed atmosphere and generous portions."),
                Review("Anonymous HKU student", 4.0, "A good choice for a group after class."),
            ),
        ),
        Restaurant(
            name = "Mido Cafe",
            cuisine = "Hong Kong",
            location = "Sai Ying Pun",
            rating = 4.2,
            price = "$",
            reviews = listOf(
                Review("Jason, Year 1", 4.0, "Fast service and a very student-friendly price."),
                Review("Anonymous HKU student", 4.5, "The classic cafe food is reliable."),
            ),
        ),
        Restaurant(
            name = "The Caffeine Laboratory",
            cuisine = "Cafe",
            location = "Kennedy Town",
            rating = 4.6,
            price = "$$",
            reviews = listOf(
                Review("Sofia, Year 3", 5.0, "Great coffee and plenty of space to study."),
                Review("Anonymous HKU student", 4.0, "Quiet in the afternoon and easy to reach."),
            ),
        ),
        Restaurant(
            name = "Wang Fu",
            cuisine = "Chinese",
            location = "HKU Main Campus",
            rating = 4.1,
            price = "$",
            reviews = listOf(
                Review("Chris, Year 4", 4.0, "Convenient between lectures and good value."),
                Review("Anonymous HKU student", 4.0, "The closest option when I have a short break."),
            ),
        ),
    )
}

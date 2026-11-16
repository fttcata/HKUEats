package com.example.hkueats.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class CampusPlace(
    val name: String,
    val campus: String,
    val latitude: Double,
    val longitude: Double,
    val aliases: List<String> = emptyList(),
) {
    val coordinates: Coordinates
        get() = requireNotNull(Coordinates.from(latitude, longitude))
}

internal fun loadCampusPlaces(context: Context): List<CampusPlace> =
    context.assets.open("hku-campus-places.json").bufferedReader().use {
        Json.decodeFromString<List<CampusPlace>>(it.readText())
    }

internal fun List<CampusPlace>.suggestionsFor(input: String): List<CampusPlace> {
    val query = input.searchText()
    if (query.isBlank()) return emptyList()
    val words = query.split(' ')
    val noMatch = 3
    return mapNotNull { place ->
        val rank = (listOf(place.name) + place.aliases).minOf { label ->
            val searchLabel = label.searchText()
            when {
                searchLabel == query -> 0
                searchLabel.startsWith(query) -> 1
                words.all { it in searchLabel } -> 2
                else -> noMatch
            }
        }
        if (rank == noMatch) null else place to rank
    }.sortedBy { it.second }.take(5).map { it.first }
}

private val NON_WORD = Regex("[^\\p{L}\\p{N}]+")

private fun String.searchText(): String = lowercase().replace(NON_WORD, " ").trim()

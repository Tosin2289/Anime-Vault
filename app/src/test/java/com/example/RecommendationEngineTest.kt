package com.example

import com.example.data.model.AnimeItem
import com.example.data.model.WatchStatus
import com.example.domain.recommendation.RecommendationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationEngineTest {

    @Test
    fun testTasteProfileCalculation() {
        val userLibrary = listOf(
            AnimeItem(
                malId = 1,
                title = "Attack on Titan",
                imageUrl = "https://example.com/aot.jpg",
                synopsis = "Titans vs humanity",
                genres = listOf("Action", "Drama"),
                themes = listOf("Dark Fantasy"),
                episodesWatched = 25,
                episodesTotal = 25,
                userRating = 10,
                userStatus = WatchStatus.COMPLETED,
                isFavorite = true
            ),
            AnimeItem(
                malId = 2,
                title = "Vinland Saga",
                imageUrl = "https://example.com/vinland.jpg",
                synopsis = "Vikings story",
                genres = listOf("Action", "Adventure"),
                themes = listOf("Historical"),
                episodesWatched = 24,
                episodesTotal = 24,
                userRating = 9,
                userStatus = WatchStatus.COMPLETED
            )
        )

        val profile = RecommendationEngine.calculateTasteProfile(userLibrary)
        assertTrue(profile.hasSufficientData)
        assertEquals(49, profile.totalEpisodesWatched)
        assertEquals(9.5, profile.averageRating, 0.1)
        assertTrue(profile.genrePercentages.containsKey("Action"))
        assertTrue((profile.genrePercentages["Action"] ?: 0) >= 80)
    }

    @Test
    fun testDeterministicRecommendationScoring() {
        val userLibrary = listOf(
            AnimeItem(
                malId = 1,
                title = "Attack on Titan",
                imageUrl = "https://example.com/aot.jpg",
                synopsis = "Titans",
                genres = listOf("Action", "Drama"),
                themes = listOf("Dark Fantasy"),
                episodesWatched = 25,
                userRating = 10,
                isFavorite = true
            )
        )

        val candidates = listOf(
            AnimeItem(
                malId = 2,
                title = "Jujutsu Kaisen",
                imageUrl = "https://example.com/jjk.jpg",
                synopsis = "Cursed energy",
                genres = listOf("Action", "Drama"),
                themes = listOf("Dark Fantasy"),
                score = 8.6
            ),
            AnimeItem(
                malId = 3,
                title = "Romance Show",
                imageUrl = "https://example.com/romance.jpg",
                synopsis = "Love story",
                genres = listOf("Romance", "Slice of Life"),
                score = 7.0
            )
        )

        val recommendations = RecommendationEngine.generateRecommendations(candidates, userLibrary)
        assertEquals(2, recommendations.size)
        // Jujutsu Kaisen should score higher than Romance Show due to shared Action/Dark Fantasy
        assertTrue(recommendations[0].anime.malId == 2)
        assertTrue(recommendations[0].matchPercentage > recommendations[1].matchPercentage)
        assertNotNull(recommendations[0].explanation)
        assertTrue(recommendations[0].explanation.contains("Attack on Titan") || recommendations[0].explanation.contains("Action"))
    }
}

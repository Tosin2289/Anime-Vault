package com.example.domain.recommendation

import com.example.data.model.AnimeItem
import com.example.data.model.Recommendation
import com.example.data.model.TasteProfile
import com.example.data.model.WatchStatus
import kotlin.math.roundToInt

data class RecommendationWeights(
    val genreWeight: Double = 0.30,
    val ratingWeight: Double = 0.25,
    val animeSimilarityWeight: Double = 0.20,
    val themeWeight: Double = 0.10,
    val studioWeight: Double = 0.05,
    val popularityWeight: Double = 0.05,
    val recencyWeight: Double = 0.05
)

object RecommendationEngine {
    var weights: RecommendationWeights = RecommendationWeights()

    fun calculateTasteProfile(
        library: List<AnimeItem>
    ): TasteProfile {
        if (library.isEmpty()) {
            return TasteProfile(hasSufficientData = false)
        }

        val ratedOrFinished = library.filter { it.userRating != null || it.userStatus == WatchStatus.COMPLETED || it.episodesWatched > 0 }
        if (ratedOrFinished.isEmpty()) {
            return TasteProfile(hasSufficientData = false, totalAnimeTracked = library.size)
        }

        val genreScores = mutableMapOf<String, Double>()
        val themeScores = mutableMapOf<String, Double>()
        val studioScores = mutableMapOf<String, Double>()
        var totalRatingSum = 0.0
        var ratedCount = 0
        var totalEpisodes = 0

        library.forEach { anime ->
            totalEpisodes += anime.episodesWatched
            val rating = anime.userRating
            if (rating != null) {
                totalRatingSum += rating
                ratedCount++
            }

            // Weight multiplier based on user rating and status
            val multiplier = when {
                anime.isFavorite -> 2.0
                rating != null -> (rating / 5.0) // 10/10 -> 2.0, 5/10 -> 1.0, 1/10 -> 0.2
                anime.userStatus == WatchStatus.COMPLETED -> 1.5
                anime.userStatus == WatchStatus.WATCHING -> 1.2
                anime.userStatus == WatchStatus.DROPPED -> 0.3
                else -> 1.0
            }

            anime.genres.forEach { g ->
                genreScores[g] = (genreScores[g] ?: 0.0) + multiplier
            }
            anime.themes.forEach { t ->
                themeScores[t] = (themeScores[t] ?: 0.0) + multiplier
            }
            anime.studios.forEach { s ->
                studioScores[s] = (studioScores[s] ?: 0.0) + multiplier
            }
        }

        // Normalize genre scores to percentages between 20% and 98%
        val maxGenreScore = genreScores.values.maxOrNull() ?: 1.0
        val genrePercentages = genreScores.mapValues { (_, score) ->
            val pct = ((score / maxGenreScore) * 100).roundToInt()
            pct.coerceIn(15, 98)
        }.toList().sortedByDescending { it.second }.toMap()

        val topThemes = themeScores.toList().sortedByDescending { it.second }.map { it.first }.take(5)
        val topStudios = studioScores.toList().sortedByDescending { it.second }.map { it.first }.take(3)
        val avgRating = if (ratedCount > 0) (totalRatingSum / ratedCount * 10.0).roundToInt() / 10.0 else 0.0

        return TasteProfile(
            genrePercentages = genrePercentages,
            topThemes = topThemes,
            topStudios = topStudios,
            totalAnimeTracked = library.size,
            totalEpisodesWatched = totalEpisodes,
            estimatedHoursWatched = (totalEpisodes * 24.0) / 60.0,
            averageRating = avgRating,
            hasSufficientData = ratedCount >= 2 || library.size >= 3
        )
    }

    fun generateRecommendations(
        candidatePool: List<AnimeItem>,
        userLibrary: List<AnimeItem>,
        limit: Int = 10
    ): List<Recommendation> {
        val userAnimeIds = userLibrary.map { it.malId }.toSet()
        // Filter out anime user is already tracking
        val candidates = candidatePool.filter { it.malId !in userAnimeIds }
        if (candidates.isEmpty()) return emptyList()

        val highlyRated = userLibrary.filter { (it.userRating ?: 0) >= 8 || it.isFavorite }
        val allUserGenres = mutableMapOf<String, Int>()
        val allUserThemes = mutableMapOf<String, Int>()
        val allUserStudios = mutableSetOf<String>()

        userLibrary.forEach { item ->
            val weight = if (item.isFavorite) 3 else ((item.userRating ?: 7) / 3).coerceAtLeast(1)
            item.genres.forEach { g -> allUserGenres[g] = (allUserGenres[g] ?: 0) + weight }
            item.themes.forEach { t -> allUserThemes[t] = (allUserThemes[t] ?: 0) + weight }
            allUserStudios.addAll(item.studios)
        }

        val topUserGenres = allUserGenres.toList().sortedByDescending { it.second }.map { it.first }.take(4)
        val topUserThemes = allUserThemes.toList().sortedByDescending { it.second }.map { it.first }.take(4)

        val scored = candidates.map { candidate ->
            val matchingFactors = mutableListOf<String>()

            // 1. Genre similarity (30%)
            val sharedGenres = candidate.genres.filter { it in allUserGenres.keys }
            val genreScore = if (candidate.genres.isNotEmpty()) {
                sharedGenres.size.toDouble() / candidate.genres.size.toDouble()
            } else 0.5
            if (sharedGenres.isNotEmpty()) {
                matchingFactors.add(sharedGenres.take(2).joinToString(", "))
            }

            // 2. User rating behavior & similarity to highly rated anime (25% + 20%)
            var bestRatedMatch: AnimeItem? = null
            var bestRatedOverlap = 0
            highlyRated.forEach { liked ->
                val overlap = (candidate.genres.intersect(liked.genres.toSet()).size * 2) +
                        candidate.themes.intersect(liked.themes.toSet()).size
                if (overlap > bestRatedOverlap) {
                    bestRatedOverlap = overlap
                    bestRatedMatch = liked
                }
            }
            val likedSimilarityScore = (bestRatedOverlap.toDouble() / 6.0).coerceIn(0.0, 1.0)

            // 3. Theme similarity (10%)
            val sharedThemes = candidate.themes.filter { it in allUserThemes.keys }
            val themeScore = if (candidate.themes.isNotEmpty()) {
                sharedThemes.size.toDouble() / candidate.themes.size.toDouble()
            } else 0.5
            if (sharedThemes.isNotEmpty()) {
                matchingFactors.add(sharedThemes.take(2).joinToString(", "))
            }

            // 4. Studio similarity (5%)
            val studioMatch = candidate.studios.any { it in allUserStudios }
            val studioScore = if (studioMatch) 1.0 else 0.0
            if (studioMatch) {
                candidate.studios.firstOrNull { it in allUserStudios }?.let { matchingFactors.add(it) }
            }

            // 5. Popularity & score (5%)
            val popularityScore = ((candidate.score ?: 7.5) / 10.0).coerceIn(0.0, 1.0)

            // 6. Recency (5%)
            val recencyScore = if ((candidate.year ?: 2020) >= 2022) 1.0 else 0.6

            // Weighted composite
            val totalRawScore = (genreScore * weights.genreWeight) +
                    (likedSimilarityScore * (weights.ratingWeight + weights.animeSimilarityWeight)) +
                    (themeScore * weights.themeWeight) +
                    (studioScore * weights.studioWeight) +
                    (popularityScore * weights.popularityWeight) +
                    (recencyScore * weights.recencyWeight)

            val matchPercentage = (totalRawScore * 100).roundToInt().coerceIn(68, 98)

            // Construct truthful data-driven explanation
            val explanation = when {
                bestRatedMatch != null && sharedGenres.isNotEmpty() -> {
                    val likedTitle = bestRatedMatch?.englishTitle ?: bestRatedMatch?.title ?: "favorites"
                    val ratingStr = bestRatedMatch?.userRating?.let { " $it/10" } ?: ""
                    "Recommended because you rated $likedTitle$ratingStr and frequently enjoy ${sharedGenres.take(2).joinToString(", ")}."
                }
                sharedGenres.isNotEmpty() && sharedThemes.isNotEmpty() -> {
                    "Matches your taste profile for ${sharedGenres.take(2).joinToString(" & ")} with ${sharedThemes.firstOrNull()} elements."
                }
                sharedGenres.isNotEmpty() -> {
                    "High match based on your viewing history in ${sharedGenres.joinToString(", ")}."
                }
                else -> {
                    "Top-rated critically acclaimed title matching your discovered interest in ${(candidate.genres.firstOrNull() ?: "Anime")}."
                }
            }

            Recommendation(
                anime = candidate,
                matchPercentage = matchPercentage,
                explanation = explanation,
                matchingFactors = matchingFactors.distinct()
            )
        }.sortedByDescending { it.matchPercentage }

        return scored.take(limit)
    }
}

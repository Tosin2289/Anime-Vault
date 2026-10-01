package com.example.data.model

enum class WatchStatus(val displayName: String) {
    WATCHING("Watching"),
    COMPLETED("Completed"),
    PLAN_TO_WATCH("Plan to Watch"),
    ON_HOLD("On Hold"),
    DROPPED("Dropped");

    companion object {
        fun fromString(value: String): WatchStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: WATCHING
        }
    }
}

data class AnimeRelation(
    val relationType: String,
    val malId: Int,
    val title: String,
    val type: String
)

data class EpisodeItem(
    val episodeNumber: Int,
    val title: String,
    val airedDate: String? = null,
    val isWatched: Boolean = false,
    val isFiller: Boolean = false
)

data class AnimeNewsItem(
    val id: String,
    val title: String,
    val description: String,
    val link: String,
    val imageUrl: String? = null,
    val publishedDate: String,
    val author: String? = null
)

data class AnimeReleaseItem(
    val id: Int,
    val malId: Int,
    val title: String,
    val imageUrl: String,
    val episodeNumber: Int,
    val airingAt: Long,
    val timeUntilAiringSeconds: Long,
    val format: String = "TV",
    val genres: List<String> = emptyList()
)

data class AnimeItem(
    val malId: Int,
    val title: String,
    val englishTitle: String? = null,
    val japaneseTitle: String? = null,
    val imageUrl: String,
    val bannerUrl: String? = null,
    val synopsis: String,
    val episodesTotal: Int? = null,
    val episodesWatched: Int = 0,
    val score: Double? = null,
    val rank: Int? = null,
    val popularity: Int? = null,
    val airingStatus: String = "Finished Airing",
    val isAiring: Boolean = false,
    val airedString: String? = null,
    val broadcastDay: String? = null,
    val broadcastTime: String? = null,
    val broadcastTimezone: String = "Asia/Tokyo",
    val season: String? = null,
    val year: Int? = null,
    val durationMinutes: Int = 24,
    val genres: List<String> = emptyList(),
    val themes: List<String> = emptyList(),
    val studios: List<String> = emptyList(),
    val relations: List<AnimeRelation> = emptyList(),
    val userStatus: WatchStatus? = null,
    val userRating: Int? = null,
    val isFavorite: Boolean = false,
    val userNotes: String? = null,
    val lastWatchedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

data class Recommendation(
    val anime: AnimeItem,
    val matchPercentage: Int,
    val explanation: String,
    val matchingFactors: List<String>
)

data class TasteProfile(
    val genrePercentages: Map<String, Int> = emptyMap(),
    val topThemes: List<String> = emptyList(),
    val topStudios: List<String> = emptyList(),
    val totalAnimeTracked: Int = 0,
    val totalEpisodesWatched: Int = 0,
    val estimatedHoursWatched: Double = 0.0,
    val averageRating: Double = 0.0,
    val hasSufficientData: Boolean = false
)

data class WatchHistoryEntry(
    val id: Long = 0,
    val animeId: Int,
    val animeTitle: String,
    val animeImageUrl: String,
    val episodeNumber: Int,
    val watchedAt: Long = System.currentTimeMillis()
)

data class UserProfile(
    val id: String = "user_default",
    val name: String = "Anime Enthusiast",
    val email: String = "ayoolatosin00@gmail.com",
    val avatarUrl: String? = null,
    val timezone: String = "UTC",
    val joinedDate: String = "September 2026",
    val notifyNewEpisodes: Boolean = true,
    val notifyNewSeasons: Boolean = true,
    val notifyUpcomingReminders: Boolean = true,
    val notifyWeeklySummary: Boolean = true
)

data class SessionInfo(
    val sessionNumber: Int = 1,
    val sessionStartTime: Long = System.currentTimeMillis(),
    val lastActiveTime: Long = System.currentTimeMillis(),
    val episodesWatchedThisSession: Int = 0,
    val lastDestination: String = "HOME",
    val lastDayFilter: String = "Today",
    val lastViewedAnimeId: Int? = null,
    val lastViewedAnimeTitle: String? = null,
    val lastViewedAnimeImage: String? = null,
    val isOnboardingCompleted: Boolean = false
)

data class SessionLogItem(
    val id: Long = 0,
    val sessionNumber: Int,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Long,
    val lastDestination: String,
    val episodesWatchedCount: Int,
    val summary: String
)

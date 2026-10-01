package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AnimeItem
import com.example.data.model.AnimeRelation
import com.example.data.model.SessionInfo
import com.example.data.model.SessionLogItem
import com.example.data.model.UserProfile
import com.example.data.model.WatchStatus

@Entity(tableName = "user_anime")
data class UserAnimeEntity(
    @PrimaryKey val malId: Int,
    val title: String,
    val englishTitle: String?,
    val japaneseTitle: String?,
    val imageUrl: String,
    val bannerUrl: String?,
    val synopsis: String,
    val episodesTotal: Int?,
    val episodesWatched: Int,
    val score: Double?,
    val rank: Int?,
    val popularity: Int?,
    val airingStatus: String,
    val isAiring: Boolean,
    val airedString: String?,
    val broadcastDay: String?,
    val broadcastTime: String?,
    val broadcastTimezone: String,
    val season: String?,
    val year: Int?,
    val durationMinutes: Int,
    val genres: List<String>,
    val themes: List<String>,
    val studios: List<String>,
    val relations: List<AnimeRelation>,
    val userStatus: String, // WATCHING, COMPLETED, etc.
    val userRating: Int?,
    val isFavorite: Boolean,
    val userNotes: String?,
    val lastWatchedAt: Long?,
    val updatedAt: Long
) {
    fun toAnimeItem(): AnimeItem = AnimeItem(
        malId = malId,
        title = title,
        englishTitle = englishTitle,
        japaneseTitle = japaneseTitle,
        imageUrl = imageUrl,
        bannerUrl = bannerUrl,
        synopsis = synopsis,
        episodesTotal = episodesTotal,
        episodesWatched = episodesWatched,
        score = score,
        rank = rank,
        popularity = popularity,
        airingStatus = airingStatus,
        isAiring = isAiring,
        airedString = airedString,
        broadcastDay = broadcastDay,
        broadcastTime = broadcastTime,
        broadcastTimezone = broadcastTimezone,
        season = season,
        year = year,
        durationMinutes = durationMinutes,
        genres = genres,
        themes = themes,
        studios = studios,
        relations = relations,
        userStatus = WatchStatus.fromString(userStatus),
        userRating = userRating,
        isFavorite = isFavorite,
        userNotes = userNotes,
        lastWatchedAt = lastWatchedAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromAnimeItem(item: AnimeItem, status: WatchStatus): UserAnimeEntity = UserAnimeEntity(
            malId = item.malId,
            title = item.title,
            englishTitle = item.englishTitle,
            japaneseTitle = item.japaneseTitle,
            imageUrl = item.imageUrl,
            bannerUrl = item.bannerUrl,
            synopsis = item.synopsis,
            episodesTotal = item.episodesTotal,
            episodesWatched = item.episodesWatched,
            score = item.score,
            rank = item.rank,
            popularity = item.popularity,
            airingStatus = item.airingStatus,
            isAiring = item.isAiring,
            airedString = item.airedString,
            broadcastDay = item.broadcastDay,
            broadcastTime = item.broadcastTime,
            broadcastTimezone = item.broadcastTimezone,
            season = item.season,
            year = item.year,
            durationMinutes = item.durationMinutes,
            genres = item.genres,
            themes = item.themes,
            studios = item.studios,
            relations = item.relations,
            userStatus = status.name,
            userRating = item.userRating,
            isFavorite = item.isFavorite,
            userNotes = item.userNotes,
            lastWatchedAt = item.lastWatchedAt,
            updatedAt = System.currentTimeMillis()
        )
    }
}

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val animeId: Int,
    val animeTitle: String,
    val animeImageUrl: String,
    val episodeNumber: Int,
    val watchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "episode_progress")
data class EpisodeProgressEntity(
    @PrimaryKey val id: String, // "$animeId-$episodeNumber"
    val animeId: Int,
    val episodeNumber: Int,
    val watchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_anime")
data class CachedAnimeEntity(
    @PrimaryKey val malId: Int,
    val category: String, // "TRENDING", "AIRING", "UPCOMING", "CATALOG"
    val title: String,
    val englishTitle: String?,
    val japaneseTitle: String?,
    val imageUrl: String,
    val bannerUrl: String?,
    val synopsis: String,
    val episodesTotal: Int?,
    val score: Double?,
    val rank: Int?,
    val popularity: Int?,
    val airingStatus: String,
    val isAiring: Boolean,
    val airedString: String?,
    val broadcastDay: String?,
    val broadcastTime: String?,
    val broadcastTimezone: String,
    val season: String?,
    val year: Int?,
    val durationMinutes: Int,
    val genres: List<String>,
    val themes: List<String>,
    val studios: List<String>,
    val relations: List<AnimeRelation>,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toAnimeItem(): AnimeItem = AnimeItem(
        malId = malId,
        title = title,
        englishTitle = englishTitle,
        japaneseTitle = japaneseTitle,
        imageUrl = imageUrl,
        bannerUrl = bannerUrl,
        synopsis = synopsis,
        episodesTotal = episodesTotal,
        score = score,
        rank = rank,
        popularity = popularity,
        airingStatus = airingStatus,
        isAiring = isAiring,
        airedString = airedString,
        broadcastDay = broadcastDay,
        broadcastTime = broadcastTime,
        broadcastTimezone = broadcastTimezone,
        season = season,
        year = year,
        durationMinutes = durationMinutes,
        genres = genres,
        themes = themes,
        studios = studios,
        relations = relations
    )
}

@Entity(tableName = "app_session")
data class AppSessionEntity(
    @PrimaryKey val id: String = "active_session",
    val userId: String = "user_default",
    val userName: String = "Anime Enthusiast",
    val userEmail: String = "ayoolatosin00@gmail.com",
    val avatarUrl: String? = null,
    val timezone: String = "UTC",
    val joinedDate: String = "September 2026",
    val notifyNewEpisodes: Boolean = true,
    val notifyNewSeasons: Boolean = true,
    val notifyUpcomingReminders: Boolean = true,
    val notifyWeeklySummary: Boolean = true,
    val isOnboardingCompleted: Boolean = false,
    val lastActiveDestination: String = "HOME",
    val lastActiveDayFilter: String = "Today",
    val lastActiveSearchQuery: String? = null,
    val lastViewedAnimeId: Int? = null,
    val lastViewedAnimeTitle: String? = null,
    val lastViewedAnimeImage: String? = null,
    val sessionStartTime: Long = System.currentTimeMillis(),
    val lastActiveTime: Long = System.currentTimeMillis(),
    val sessionCount: Int = 1,
    val episodesWatchedThisSession: Int = 0
) {
    fun toUserProfile(): UserProfile = UserProfile(
        id = userId,
        name = userName,
        email = userEmail,
        avatarUrl = avatarUrl,
        timezone = timezone,
        joinedDate = joinedDate,
        notifyNewEpisodes = notifyNewEpisodes,
        notifyNewSeasons = notifyNewSeasons,
        notifyUpcomingReminders = notifyUpcomingReminders,
        notifyWeeklySummary = notifyWeeklySummary
    )

    fun toSessionInfo(): SessionInfo = SessionInfo(
        sessionNumber = sessionCount,
        sessionStartTime = sessionStartTime,
        lastActiveTime = lastActiveTime,
        episodesWatchedThisSession = episodesWatchedThisSession,
        lastDestination = lastActiveDestination,
        lastDayFilter = lastActiveDayFilter,
        lastViewedAnimeId = lastViewedAnimeId,
        lastViewedAnimeTitle = lastViewedAnimeTitle,
        lastViewedAnimeImage = lastViewedAnimeImage,
        isOnboardingCompleted = isOnboardingCompleted
    )
}

@Entity(tableName = "session_history")
data class SessionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val sessionId: Long = 0,
    val sessionNumber: Int,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Long,
    val lastDestination: String,
    val episodesWatchedCount: Int,
    val summary: String
) {
    fun toSessionLogItem(): SessionLogItem = SessionLogItem(
        id = sessionId,
        sessionNumber = sessionNumber,
        startTime = startTime,
        endTime = endTime,
        durationMinutes = durationMinutes,
        lastDestination = lastDestination,
        episodesWatchedCount = episodesWatchedCount,
        summary = summary
    )
}

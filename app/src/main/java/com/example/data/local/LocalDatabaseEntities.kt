package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.data.model.AnimeItem
import com.example.data.model.AnimeRelation
import com.example.data.model.WatchHistoryEntry
import com.example.data.model.WatchStatus

/**
 * Room Entity representing Anime information stored offline in the local database.
 */
@Entity(tableName = "AnimeEntity")
data class AnimeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val englishTitle: String? = null,
    val japaneseTitle: String? = null,
    val synopsis: String = "",
    val imageUrl: String = "",
    val bannerUrl: String? = null,
    val score: Double? = null,
    val rank: Int? = null,
    val popularity: Int? = null,
    val episodesTotal: Int? = null,
    val airingStatus: String = "Finished Airing",
    val isAiring: Boolean = false,
    val airedString: String? = null,
    val season: String? = null,
    val year: Int? = null,
    val durationMinutes: Int = 24,
    val broadcastDay: String? = null,
    val broadcastTime: String? = null,
    val broadcastTimezone: String = "Asia/Tokyo",
    val genres: List<String> = emptyList(),
    val themes: List<String> = emptyList(),
    val studios: List<String> = emptyList(),
    val relations: List<AnimeRelation> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toAnimeItem(progress: UserAnimeProgress? = null): AnimeItem {
        val watchStatus = progress?.statusEnum
        return AnimeItem(
            malId = id,
            title = title,
            englishTitle = englishTitle,
            japaneseTitle = japaneseTitle,
            synopsis = synopsis,
            imageUrl = imageUrl,
            bannerUrl = bannerUrl,
            score = score,
            rank = rank,
            popularity = popularity,
            episodesTotal = episodesTotal,
            episodesWatched = progress?.episodesWatched ?: 0,
            airingStatus = airingStatus,
            isAiring = isAiring,
            airedString = airedString,
            season = season,
            year = year,
            durationMinutes = durationMinutes,
            broadcastDay = broadcastDay,
            broadcastTime = broadcastTime,
            broadcastTimezone = broadcastTimezone,
            genres = genres,
            themes = themes,
            studios = studios,
            relations = relations,
            userStatus = watchStatus,
            userRating = progress?.userRating,
            userNotes = progress?.notes,
            isFavorite = progress?.isFavorite ?: false,
            lastWatchedAt = progress?.lastWatchedAt,
            updatedAt = progress?.updatedAt ?: updatedAt
        )
    }

    companion object {
        fun fromAnimeItem(item: AnimeItem): AnimeEntity = AnimeEntity(
            id = item.malId,
            title = item.title,
            englishTitle = item.englishTitle,
            japaneseTitle = item.japaneseTitle,
            synopsis = item.synopsis,
            imageUrl = item.imageUrl,
            bannerUrl = item.bannerUrl,
            score = item.score,
            rank = item.rank,
            popularity = item.popularity,
            episodesTotal = item.episodesTotal,
            airingStatus = item.airingStatus,
            isAiring = item.isAiring,
            airedString = item.airedString,
            season = item.season,
            year = item.year,
            durationMinutes = item.durationMinutes,
            broadcastDay = item.broadcastDay,
            broadcastTime = item.broadcastTime,
            broadcastTimezone = item.broadcastTimezone,
            genres = item.genres,
            themes = item.themes,
            studios = item.studios,
            relations = item.relations,
            updatedAt = item.updatedAt
        )
    }
}

/**
 * Room Entity representing user tracking progress for an anime in the 'My Anime' library.
 */
@Entity(
    tableName = "UserAnimeProgress",
    indices = [Index(value = ["watchStatus"]), Index(value = ["isFavorite"])]
)
data class UserAnimeProgress(
    @PrimaryKey val animeId: Int,
    val watchStatus: String = "WATCHING",
    val episodesWatched: Int = 0,
    val totalEpisodes: Int? = null,
    val userRating: Int? = null,
    val isFavorite: Boolean = false,
    val notes: String? = null,
    val lastWatchedAt: Long = System.currentTimeMillis(),
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val statusEnum: WatchStatus
        get() = try {
            WatchStatus.valueOf(watchStatus)
        } catch (_: Exception) {
            WatchStatus.PLAN_TO_WATCH
        }

    companion object {
        fun fromAnimeItem(item: AnimeItem): UserAnimeProgress = UserAnimeProgress(
            animeId = item.malId,
            watchStatus = (item.userStatus ?: WatchStatus.WATCHING).name,
            episodesWatched = item.episodesWatched,
            totalEpisodes = item.episodesTotal,
            userRating = item.userRating,
            isFavorite = item.isFavorite,
            notes = item.userNotes,
            lastWatchedAt = item.lastWatchedAt ?: System.currentTimeMillis(),
            startedAt = System.currentTimeMillis(),
            completedAt = if (item.userStatus == WatchStatus.COMPLETED) System.currentTimeMillis() else null,
            updatedAt = System.currentTimeMillis()
        )
    }
}

/**
 * Room Entity representing episode watch history logs stored offline.
 */
@Entity(
    tableName = "WatchHistory",
    indices = [Index(value = ["animeId"]), Index(value = ["watchedTimestamp"])]
)
data class WatchHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val animeId: Int,
    val animeTitle: String,
    val animeImageUrl: String,
    val episodeNumber: Int,
    val watchedTimestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    fun toWatchHistoryEntry(): WatchHistoryEntry = WatchHistoryEntry(
        id = id,
        animeId = animeId,
        animeTitle = animeTitle,
        animeImageUrl = animeImageUrl,
        episodeNumber = episodeNumber,
        watchedAt = watchedTimestamp
    )

    companion object {
        fun fromEntry(entry: WatchHistoryEntry): WatchHistory = WatchHistory(
            id = entry.id,
            animeId = entry.animeId,
            animeTitle = entry.animeTitle,
            animeImageUrl = entry.animeImageUrl,
            episodeNumber = entry.episodeNumber,
            watchedTimestamp = entry.watchedAt
        )
    }
}

/**
 * Relational model combining Anime data and user watch progress for 'My Anime' library.
 */
data class AnimeWithProgress(
    @Embedded val anime: AnimeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "animeId"
    )
    val progress: UserAnimeProgress?
) {
    fun toAnimeItem(): AnimeItem = anime.toAnimeItem(progress)
}

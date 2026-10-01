package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAnimeDao {
    @Query("SELECT * FROM user_anime ORDER BY updatedAt DESC")
    fun getAllUserAnime(): Flow<List<UserAnimeEntity>>

    @Query("SELECT * FROM user_anime WHERE userStatus = :status ORDER BY updatedAt DESC")
    fun getUserAnimeByStatus(status: String): Flow<List<UserAnimeEntity>>

    @Query("SELECT * FROM user_anime WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteAnime(): Flow<List<UserAnimeEntity>>

    @Query("SELECT * FROM user_anime WHERE malId = :malId LIMIT 1")
    fun getUserAnime(malId: Int): Flow<UserAnimeEntity?>

    @Query("SELECT * FROM user_anime WHERE malId = :malId LIMIT 1")
    suspend fun getUserAnimeSync(malId: Int): UserAnimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: UserAnimeEntity)

    @Query("UPDATE user_anime SET episodesWatched = :progress, lastWatchedAt = :timestamp, updatedAt = :timestamp WHERE malId = :malId")
    suspend fun updateProgress(malId: Int, progress: Int, timestamp: Long)

    @Query("UPDATE user_anime SET userStatus = :status, updatedAt = :timestamp WHERE malId = :malId")
    suspend fun updateStatus(malId: Int, status: String, timestamp: Long)

    @Query("UPDATE user_anime SET userRating = :rating, updatedAt = :timestamp WHERE malId = :malId")
    suspend fun updateRating(malId: Int, rating: Int?, timestamp: Long)

    @Query("UPDATE user_anime SET isFavorite = :isFav, updatedAt = :timestamp WHERE malId = :malId")
    suspend fun updateFavorite(malId: Int, isFav: Boolean, timestamp: Long)

    @Query("UPDATE user_anime SET userNotes = :notes, updatedAt = :timestamp WHERE malId = :malId")
    suspend fun updateNotes(malId: Int, notes: String?, timestamp: Long)

    @Query("DELETE FROM user_anime WHERE malId = :malId")
    suspend fun delete(malId: Int)

    @Query("SELECT COUNT(*) FROM user_anime")
    suspend fun getCount(): Int
}

@Dao
interface LegacyWatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 100): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE animeId = :animeId")
    suspend fun deleteForAnime(animeId: Int)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()
}

@Dao
interface EpisodeProgressDao {
    @Query("SELECT * FROM episode_progress WHERE animeId = :animeId")
    fun getWatchedEpisodes(animeId: Int): Flow<List<EpisodeProgressEntity>>

    @Query("SELECT * FROM episode_progress WHERE animeId = :animeId")
    suspend fun getWatchedEpisodesSync(animeId: Int): List<EpisodeProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markEpisode(entity: EpisodeProgressEntity)

    @Query("DELETE FROM episode_progress WHERE id = :id")
    suspend fun unmarkEpisode(id: String)
}

@Dao
interface CachedAnimeDao {
    @Query("SELECT * FROM cached_anime WHERE category = :category ORDER BY popularity ASC, score DESC")
    fun getCachedByCategory(category: String): Flow<List<CachedAnimeEntity>>

    @Query("SELECT * FROM cached_anime WHERE category = :category ORDER BY popularity ASC, score DESC")
    suspend fun getCachedByCategorySync(category: String): List<CachedAnimeEntity>

    @Query("SELECT * FROM cached_anime WHERE malId = :malId LIMIT 1")
    suspend fun getCachedAnime(malId: Int): CachedAnimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<CachedAnimeEntity>)

    @Query("SELECT * FROM cached_anime WHERE title LIKE '%' || :query || '%' OR englishTitle LIKE '%' || :query || '%' LIMIT 20")
    suspend fun searchCached(query: String): List<CachedAnimeEntity>
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM app_session WHERE id = 'active_session' LIMIT 1")
    fun getActiveSession(): Flow<AppSessionEntity?>

    @Query("SELECT * FROM app_session WHERE id = 'active_session' LIMIT 1")
    suspend fun getActiveSessionSync(): AppSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSession(session: AppSessionEntity)

    @Query("UPDATE app_session SET lastActiveDestination = :destination, lastActiveDayFilter = :dayFilter, lastViewedAnimeId = :animeId, lastViewedAnimeTitle = :animeTitle, lastViewedAnimeImage = :animeImage, lastActiveTime = :time WHERE id = 'active_session'")
    suspend fun updateNavigation(
        destination: String,
        dayFilter: String,
        animeId: Int?,
        animeTitle: String?,
        animeImage: String?,
        time: Long
    )

    @Query("UPDATE app_session SET userName = :name, userEmail = :email, timezone = :timezone, notifyNewEpisodes = :episodes, notifyNewSeasons = :seasons, notifyUpcomingReminders = :upcoming, notifyWeeklySummary = :summary, lastActiveTime = :time WHERE id = 'active_session'")
    suspend fun updateProfile(
        name: String,
        email: String,
        timezone: String,
        episodes: Boolean,
        seasons: Boolean,
        upcoming: Boolean,
        summary: Boolean,
        time: Long
    )

    @Query("UPDATE app_session SET isOnboardingCompleted = :completed, lastActiveTime = :time WHERE id = 'active_session'")
    suspend fun setOnboardingCompleted(completed: Boolean, time: Long)

    @Query("UPDATE app_session SET episodesWatchedThisSession = episodesWatchedThisSession + 1, lastActiveTime = :time WHERE id = 'active_session'")
    suspend fun incrementSessionEpisodes(time: Long)

    @Query("UPDATE app_session SET sessionCount = sessionCount + 1, sessionStartTime = :startTime, lastActiveTime = :startTime, episodesWatchedThisSession = 0 WHERE id = 'active_session'")
    suspend fun startNewSession(startTime: Long)

    @Query("SELECT * FROM session_history ORDER BY startTime DESC LIMIT 50")
    fun getAllSessionHistory(): Flow<List<SessionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionHistory(history: SessionHistoryEntity)

    @Query("DELETE FROM session_history")
    suspend fun clearSessionHistory()
}

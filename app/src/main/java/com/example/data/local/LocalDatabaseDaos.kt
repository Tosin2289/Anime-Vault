package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Anime metadata offline storage.
 */
@Dao
interface AnimeDao {
    @Query("SELECT * FROM AnimeEntity ORDER BY score DESC")
    fun getAllAnime(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM AnimeEntity WHERE id = :id LIMIT 1")
    fun getAnimeById(id: Int): Flow<AnimeEntity?>

    @Query("SELECT * FROM AnimeEntity WHERE id = :id LIMIT 1")
    suspend fun getAnimeByIdSync(id: Int): AnimeEntity?

    @Query("SELECT * FROM AnimeEntity WHERE title LIKE '%' || :query || '%' OR englishTitle LIKE '%' || :query || '%' ORDER BY score DESC")
    fun searchAnime(query: String): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM AnimeEntity WHERE title LIKE '%' || :query || '%' OR englishTitle LIKE '%' || :query || '%' ORDER BY score DESC LIMIT 20")
    suspend fun searchAnimeSync(query: String): List<AnimeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnime(anime: AnimeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animes: List<AnimeEntity>)

    @Update
    suspend fun updateAnime(anime: AnimeEntity)

    @Delete
    suspend fun deleteAnime(anime: AnimeEntity)

    @Query("DELETE FROM AnimeEntity WHERE id = :id")
    suspend fun deleteAnimeById(id: Int)

    @Query("SELECT COUNT(*) FROM AnimeEntity")
    suspend fun getAnimeCount(): Int
}

/**
 * Data Access Object for persisting user progress in 'My Anime' library.
 */
@Dao
interface UserAnimeProgressDao {
    @Query("SELECT * FROM UserAnimeProgress ORDER BY lastWatchedAt DESC")
    fun getAllProgress(): Flow<List<UserAnimeProgress>>

    @Query("SELECT * FROM UserAnimeProgress WHERE animeId = :animeId LIMIT 1")
    fun getProgressForAnime(animeId: Int): Flow<UserAnimeProgress?>

    @Query("SELECT * FROM UserAnimeProgress WHERE animeId = :animeId LIMIT 1")
    suspend fun getProgressForAnimeSync(animeId: Int): UserAnimeProgress?

    @Query("SELECT * FROM UserAnimeProgress WHERE watchStatus = :status ORDER BY lastWatchedAt DESC")
    fun getProgressByStatus(status: String): Flow<List<UserAnimeProgress>>

    @Query("SELECT * FROM UserAnimeProgress WHERE isFavorite = 1 ORDER BY lastWatchedAt DESC")
    fun getFavorites(): Flow<List<UserAnimeProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserAnimeProgress)

    @Query("UPDATE UserAnimeProgress SET episodesWatched = :count, lastWatchedAt = :timestamp, updatedAt = :timestamp WHERE animeId = :animeId")
    suspend fun updateEpisodesWatched(animeId: Int, count: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE UserAnimeProgress SET watchStatus = :status, lastWatchedAt = :timestamp, updatedAt = :timestamp WHERE animeId = :animeId")
    suspend fun updateWatchStatus(animeId: Int, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE UserAnimeProgress SET userRating = :rating, updatedAt = :timestamp WHERE animeId = :animeId")
    suspend fun updateRating(animeId: Int, rating: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE UserAnimeProgress SET isFavorite = :isFavorite, updatedAt = :timestamp WHERE animeId = :animeId")
    suspend fun setFavorite(animeId: Int, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE UserAnimeProgress SET notes = :notes, updatedAt = :timestamp WHERE animeId = :animeId")
    suspend fun updateNotes(animeId: Int, notes: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM UserAnimeProgress WHERE animeId = :animeId")
    suspend fun deleteProgress(animeId: Int)

    @Query("DELETE FROM UserAnimeProgress")
    suspend fun clearAllProgress()

    @Transaction
    @Query("SELECT * FROM AnimeEntity WHERE id IN (SELECT animeId FROM UserAnimeProgress)")
    fun getLibraryAnimeWithProgress(): Flow<List<AnimeWithProgress>>

    @Transaction
    @Query("SELECT * FROM AnimeEntity WHERE id IN (SELECT animeId FROM UserAnimeProgress WHERE watchStatus = :status)")
    fun getLibraryAnimeWithProgressByStatus(status: String): Flow<List<AnimeWithProgress>>

    @Transaction
    @Query("SELECT * FROM AnimeEntity WHERE id IN (SELECT animeId FROM UserAnimeProgress WHERE isFavorite = 1)")
    fun getLibraryFavoritesWithProgress(): Flow<List<AnimeWithProgress>>

    @Query("SELECT COUNT(*) FROM UserAnimeProgress")
    suspend fun getLibraryCount(): Int
}

/**
 * Data Access Object for episode watch history offline logs.
 */
@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM WatchHistory ORDER BY watchedTimestamp DESC")
    fun getAllHistory(): Flow<List<WatchHistory>>

    @Query("SELECT * FROM WatchHistory ORDER BY watchedTimestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 50): Flow<List<WatchHistory>>

    @Query("SELECT * FROM WatchHistory WHERE animeId = :animeId ORDER BY watchedTimestamp DESC")
    fun getHistoryForAnime(animeId: Int): Flow<List<WatchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WatchHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: WatchHistory)

    @Query("DELETE FROM WatchHistory WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM WatchHistory WHERE animeId = :animeId")
    suspend fun deleteHistoryForAnime(animeId: Int)

    @Query("DELETE FROM WatchHistory")
    suspend fun clearHistory()
}

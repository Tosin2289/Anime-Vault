package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Primary Room Database persisting AnimeEntity, UserAnimeProgress, and WatchHistory offline.
 * Supports the 'My Anime' library feature, caching, episode tracking, and app sessions.
 */
@Database(
    entities = [
        AnimeEntity::class,
        UserAnimeProgress::class,
        WatchHistory::class,
        EpisodeProgressEntity::class,
        CachedAnimeEntity::class,
        AppSessionEntity::class,
        SessionHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
    abstract fun userAnimeProgressDao(): UserAnimeProgressDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun episodeProgressDao(): EpisodeProgressDao
    abstract fun cachedAnimeDao(): CachedAnimeDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: LocalDatabase? = null

        fun getInstance(context: Context): LocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDatabase::class.java,
                    "anime_vault_offline.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

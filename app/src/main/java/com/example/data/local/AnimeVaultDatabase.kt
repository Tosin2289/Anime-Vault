package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserAnimeEntity::class,
        WatchHistoryEntity::class,
        EpisodeProgressEntity::class,
        CachedAnimeEntity::class,
        AppSessionEntity::class,
        SessionHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AnimeVaultDatabase : RoomDatabase() {
    abstract fun userAnimeDao(): UserAnimeDao
    abstract fun legacyWatchHistoryDao(): LegacyWatchHistoryDao
    abstract fun episodeProgressDao(): EpisodeProgressDao
    abstract fun cachedAnimeDao(): CachedAnimeDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AnimeVaultDatabase? = null

        fun getInstance(context: Context): AnimeVaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnimeVaultDatabase::class.java,
                    "anime_vault.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

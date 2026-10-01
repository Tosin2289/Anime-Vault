package com.example

import com.example.data.local.AnimeEntity
import com.example.data.local.AnimeWithProgress
import com.example.data.local.UserAnimeProgress
import com.example.data.local.WatchHistory
import com.example.data.model.AnimeItem
import com.example.data.model.WatchHistoryEntry
import com.example.data.model.WatchStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalDatabaseSchemaTest {

    @Test
    fun testAnimeEntityAndUserAnimeProgressMapping() {
        val anime = AnimeItem(
            malId = 16498,
            title = "Attack on Titan",
            englishTitle = "Attack on Titan",
            japaneseTitle = "Shingeki no Kyojin",
            imageUrl = "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
            bannerUrl = "https://cdn.myanimelist.net/images/anime/10/47347l.jpg",
            synopsis = "Centuries ago, mankind was slaughtered to near extinction...",
            episodesTotal = 25,
            episodesWatched = 12,
            score = 8.54,
            airingStatus = "Finished Airing",
            genres = listOf("Action", "Fantasy"),
            studios = listOf("Wit Studio"),
            userStatus = WatchStatus.WATCHING,
            userRating = 9,
            isFavorite = true,
            userNotes = "Peak fiction"
        )

        // Test conversion to Room Entities
        val animeEntity = AnimeEntity.fromAnimeItem(anime)
        val progressEntity = UserAnimeProgress.fromAnimeItem(anime)

        assertEquals(16498, animeEntity.id)
        assertEquals("Attack on Titan", animeEntity.title)
        assertEquals(25, animeEntity.episodesTotal)
        assertEquals(listOf("Action", "Fantasy"), animeEntity.genres)

        assertEquals(16498, progressEntity.animeId)
        assertEquals("WATCHING", progressEntity.watchStatus)
        assertEquals(12, progressEntity.episodesWatched)
        assertEquals(25, progressEntity.totalEpisodes)
        assertEquals(9, progressEntity.userRating)
        assertTrue(progressEntity.isFavorite)
        assertEquals("Peak fiction", progressEntity.notes)
        assertEquals(WatchStatus.WATCHING, progressEntity.statusEnum)

        // Test AnimeWithProgress relation mapping back to domain AnimeItem
        val relation = AnimeWithProgress(anime = animeEntity, progress = progressEntity)
        val mappedItem = relation.toAnimeItem()

        assertEquals(16498, mappedItem.malId)
        assertEquals("Attack on Titan", mappedItem.title)
        assertEquals(12, mappedItem.episodesWatched)
        assertEquals(WatchStatus.WATCHING, mappedItem.userStatus)
        assertEquals(9, mappedItem.userRating)
        assertTrue(mappedItem.isFavorite)
        assertEquals("Peak fiction", mappedItem.userNotes)
    }

    @Test
    fun testWatchHistoryEntityMapping() {
        val now = System.currentTimeMillis()
        val entry = WatchHistoryEntry(
            id = 42L,
            animeId = 5114,
            animeTitle = "Fullmetal Alchemist: Brotherhood",
            animeImageUrl = "https://cdn.myanimelist.net/images/anime/1223/96541.jpg",
            episodeNumber = 10,
            watchedAt = now
        )

        val entity = WatchHistory.fromEntry(entry)
        assertEquals(42L, entity.id)
        assertEquals(5114, entity.animeId)
        assertEquals("Fullmetal Alchemist: Brotherhood", entity.animeTitle)
        assertEquals(10, entity.episodeNumber)
        assertEquals(now, entity.watchedTimestamp)

        val mappedBack = entity.toWatchHistoryEntry()
        assertEquals(42L, mappedBack.id)
        assertEquals(5114, mappedBack.animeId)
        assertEquals("Fullmetal Alchemist: Brotherhood", mappedBack.animeTitle)
        assertEquals(10, mappedBack.episodeNumber)
        assertEquals(now, mappedBack.watchedAt)
    }

    @Test
    fun testUserAnimeProgressDefaultStatus() {
        val progress = UserAnimeProgress(
            animeId = 1,
            watchStatus = "INVALID_STATUS"
        )
        // Defaults gracefully to PLAN_TO_WATCH
        assertEquals(WatchStatus.PLAN_TO_WATCH, progress.statusEnum)
    }
}

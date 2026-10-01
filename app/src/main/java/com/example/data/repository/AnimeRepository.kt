package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AnimeEntity
import com.example.data.local.AppSessionEntity
import com.example.data.local.CachedAnimeEntity
import com.example.data.local.EpisodeProgressEntity
import com.example.data.local.LocalDatabase
import com.example.data.local.SessionHistoryEntity
import com.example.data.local.UserAnimeProgress
import com.example.data.local.WatchHistory
import com.example.data.model.AnimeItem
import com.example.data.model.AnimeNewsItem
import com.example.data.model.AnimeReleaseItem
import com.example.data.model.EpisodeItem
import com.example.data.model.SessionInfo
import com.example.data.model.SessionLogItem
import com.example.data.model.UserProfile
import com.example.data.model.WatchHistoryEntry
import com.example.data.model.WatchStatus
import com.example.data.remote.AniListApiClient
import com.example.data.remote.AnimeNewsService
import com.example.data.remote.CatalogData
import com.example.data.remote.JikanMapper
import com.example.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AnimeRepository(context: Context) {
    private val localDb = LocalDatabase.getInstance(context)
    private val animeDao = localDb.animeDao()
    private val userProgressDao = localDb.userAnimeProgressDao()
    private val watchHistoryDao = localDb.watchHistoryDao()
    private val progressDao = localDb.episodeProgressDao()
    private val cacheDao = localDb.cachedAnimeDao()
    private val sessionDao = localDb.sessionDao()
    private val api = NetworkClient.jikanApi

    private var cachedUserProfile = UserProfile()

    // 1. User Library - Persisted in LocalDatabase (UserAnimeProgress + AnimeEntity)
    val userLibrary: Flow<List<AnimeItem>> = userProgressDao.getLibraryAnimeWithProgress().map { progressList ->
        progressList.map { it.toAnimeItem() }
    }

    fun getUserAnime(malId: Int): Flow<AnimeItem?> = userProgressDao.getProgressForAnime(malId).map { progress ->
        if (progress != null) {
            val anime = animeDao.getAnimeByIdSync(malId)
            anime?.toAnimeItem(progress)
        } else {
            null
        }
    }

    suspend fun saveToLibrary(
        item: AnimeItem,
        status: WatchStatus,
        watchedEp: Int = 0,
        rating: Int? = null,
        isFav: Boolean = false,
        notes: String? = null
    ) = withContext(Dispatchers.IO) {
        val updatedItem = item.copy(
            userStatus = status,
            episodesWatched = watchedEp,
            userRating = rating,
            isFavorite = isFav,
            userNotes = notes
        )

        // Persist offline in LocalDatabase schema
        val animeEntity = AnimeEntity.fromAnimeItem(updatedItem)
        val progressEntity = UserAnimeProgress(
            animeId = updatedItem.malId,
            watchStatus = status.name,
            episodesWatched = watchedEp,
            totalEpisodes = updatedItem.episodesTotal,
            userRating = rating,
            isFavorite = isFav,
            notes = notes,
            lastWatchedAt = System.currentTimeMillis(),
            startedAt = System.currentTimeMillis(),
            completedAt = if (status == WatchStatus.COMPLETED) System.currentTimeMillis() else null,
            updatedAt = System.currentTimeMillis()
        )
        animeDao.insertAnime(animeEntity)
        userProgressDao.insertOrUpdateProgress(progressEntity)

        // If watched episodes > 0, log latest
        if (watchedEp > 0) {
            logHistory(item.malId, item.title, item.imageUrl, watchedEp)
        }
    }

    suspend fun updateProgress(malId: Int, newProgress: Int) = withContext(Dispatchers.IO) {
        val existingProgress = userProgressDao.getProgressForAnimeSync(malId)
        val total = existingProgress?.totalEpisodes
        val validProgress = if (total != null && total > 0) {
            newProgress.coerceIn(0, total)
        } else {
            newProgress.coerceAtLeast(0)
        }
        val now = System.currentTimeMillis()
        userProgressDao.updateEpisodesWatched(malId, validProgress, now)

        val previousProgress = existingProgress?.episodesWatched ?: 0
        val title = animeDao.getAnimeByIdSync(malId)?.title ?: "Anime"
        val imageUrl = animeDao.getAnimeByIdSync(malId)?.imageUrl ?: ""

        // If watched increased, log to history
        if (validProgress > previousProgress) {
            logHistory(malId, title, imageUrl, validProgress)
        }

        // Check if finished
        if (total != null && validProgress >= total && existingProgress?.watchStatus != WatchStatus.COMPLETED.name) {
            userProgressDao.updateWatchStatus(malId, WatchStatus.COMPLETED.name, now)
        }
    }

    suspend fun updateStatus(malId: Int, status: WatchStatus) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        userProgressDao.updateWatchStatus(malId, status.name, now)
    }

    suspend fun updateRating(malId: Int, rating: Int?) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        userProgressDao.updateRating(malId, rating ?: 0, now)
    }

    suspend fun toggleFavorite(malId: Int, isFav: Boolean) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        userProgressDao.setFavorite(malId, isFav, now)
    }

    suspend fun updateNotes(malId: Int, notes: String?) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        userProgressDao.updateNotes(malId, notes ?: "", now)
    }

    suspend fun deleteFromLibrary(malId: Int) = withContext(Dispatchers.IO) {
        userProgressDao.deleteProgress(malId)
    }

    suspend fun isLibraryEmpty(): Boolean = withContext(Dispatchers.IO) {
        userProgressDao.getLibraryCount() == 0
    }

    // 2. Watch History & Episodes - Persisted in WatchHistory table
    val watchHistory: Flow<List<WatchHistoryEntry>> = watchHistoryDao.getRecentHistory(100).map { list ->
        list.map { it.toWatchHistoryEntry() }
    }

    fun getWatchedEpisodeNumbers(animeId: Int): Flow<Set<Int>> =
        progressDao.getWatchedEpisodes(animeId).map { list ->
            list.map { it.episodeNumber }.toSet()
        }

    suspend fun toggleEpisodeWatched(animeId: Int, epNum: Int, animeTitle: String, imageUrl: String) =
        withContext(Dispatchers.IO) {
            val key = "$animeId-$epNum"
            val existing = progressDao.getWatchedEpisodesSync(animeId)
            val isCurrentlyWatched = existing.any { it.episodeNumber == epNum }
            if (isCurrentlyWatched) {
                progressDao.unmarkEpisode(key)
            } else {
                progressDao.markEpisode(EpisodeProgressEntity(key, animeId, epNum))
                logHistory(animeId, animeTitle, imageUrl, epNum)
                // Sync progress count
                val updatedCount = existing.size + 1
                userProgressDao.updateEpisodesWatched(animeId, updatedCount, System.currentTimeMillis())
                // Increment session episodes counter in SQLite database
                sessionDao.incrementSessionEpisodes(System.currentTimeMillis())
            }
        }

    private suspend fun logHistory(animeId: Int, title: String, imageUrl: String, epNum: Int) {
        val now = System.currentTimeMillis()
        watchHistoryDao.insert(
            WatchHistory(
                animeId = animeId,
                animeTitle = title,
                animeImageUrl = imageUrl,
                episodeNumber = epNum,
                watchedTimestamp = now
            )
        )
    }

    // 3. News & Releases
    suspend fun getNews(): List<AnimeNewsItem> = withContext(Dispatchers.IO) {
        AnimeNewsService.getLatestNews()
    }

    suspend fun getAiringReleases(): List<AnimeReleaseItem> = withContext(Dispatchers.IO) {
        AniListApiClient.getAiringReleases(25)
    }

    // 4. Search and Discovery using Jikan REST API as primary, AniList as live backup, and offline catalog fallback
    suspend fun searchAnime(query: String): List<AnimeItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext CatalogData.curatedCatalog

        // 1. Primary: Jikan API
        try {
            val resp = api.searchAnime(query = query.trim(), limit = 20)
            val items = resp.data?.map { JikanMapper.toDomain(it) } ?: emptyList()
            if (items.isNotEmpty()) {
                Log.d("AnimeRepository", "Jikan search returned ${items.size} results for '$query'")
                return@withContext items
            }
        } catch (e: Exception) {
            Log.w("AnimeRepository", "Jikan search error: ${e.message}")
        }

        // 2. Backup: AniList Live GraphQL
        try {
            val aniListResults = AniListApiClient.searchAnime(searchQuery = query, limit = 20)
            if (aniListResults.isNotEmpty()) {
                Log.d("AnimeRepository", "AniList search fallback returned ${aniListResults.size} results")
                return@withContext aniListResults
            }
        } catch (e: Exception) {
            Log.w("AnimeRepository", "AniList search backup error: ${e.message}")
        }

        // 3. Fallback filter in offline catalog
        CatalogData.curatedCatalog.filter {
            it.title.contains(query, ignoreCase = true) ||
                    (it.englishTitle?.contains(query, ignoreCase = true) == true) ||
                    it.genres.any { g -> g.contains(query, ignoreCase = true) } ||
                    it.studios.any { s -> s.contains(query, ignoreCase = true) }
        }
    }

    suspend fun getTrending(): List<AnimeItem> = withContext(Dispatchers.IO) {
        // 1. Primary: Jikan Top Anime
        try {
            val resp = api.getTopAnime(filter = "bypopularity", limit = 25)
            val items = resp.data?.map { JikanMapper.toDomain(it) } ?: emptyList()
            if (items.isNotEmpty()) {
                Log.d("AnimeRepository", "Jikan getTopAnime returned ${items.size} items")
                return@withContext items
            }
        } catch (e: Exception) {
            Log.w("AnimeRepository", "Jikan top anime network error: ${e.message}")
        }

        // 2. Backup: AniList Live Trending
        try {
            val liveTrending = AniListApiClient.getTrendingAnime(limit = 25)
            if (liveTrending.isNotEmpty()) return@withContext liveTrending
        } catch (e: Exception) {
            Log.w("AnimeRepository", "AniList trending fallback error: ${e.message}")
        }

        CatalogData.curatedCatalog.sortedBy { it.popularity ?: 999 }
    }

    suspend fun getCurrentlyAiring(): List<AnimeItem> = withContext(Dispatchers.IO) {
        // 1. Primary: Jikan Seasons Now
        try {
            val resp = api.getSeasonsNow(limit = 25)
            val items = resp.data?.map { JikanMapper.toDomain(it) } ?: emptyList()
            if (items.isNotEmpty()) {
                Log.d("AnimeRepository", "Jikan getSeasonsNow returned ${items.size} items")
                return@withContext items
            }
        } catch (e: Exception) {
            Log.w("AnimeRepository", "Jikan seasons now network error: ${e.message}")
        }

        // 2. Backup: AniList Airing
        try {
            val liveAiring = AniListApiClient.getCurrentlyAiring(limit = 25)
            if (liveAiring.isNotEmpty()) return@withContext liveAiring
        } catch (e: Exception) {
            Log.w("AnimeRepository", "AniList airing fallback error: ${e.message}")
        }

        CatalogData.curatedCatalog.filter { it.isAiring || it.airingStatus.contains("Airing", ignoreCase = true) }
    }

    suspend fun getUpcoming(): List<AnimeItem> = withContext(Dispatchers.IO) {
        // 1. Primary: Jikan Seasons Upcoming
        try {
            val resp = api.getSeasonsUpcoming(limit = 25)
            val items = resp.data?.map { JikanMapper.toDomain(it) } ?: emptyList()
            if (items.isNotEmpty()) {
                Log.d("AnimeRepository", "Jikan getSeasonsUpcoming returned ${items.size} items")
                return@withContext items
            }
        } catch (e: Exception) {
            Log.w("AnimeRepository", "Jikan upcoming anime network error: ${e.message}")
        }

        // 2. Backup: AniList Upcoming
        try {
            val liveUpcoming = AniListApiClient.getUpcomingReleases(limit = 25)
            if (liveUpcoming.isNotEmpty()) return@withContext liveUpcoming
        } catch (e: Exception) {
            Log.w("AnimeRepository", "AniList upcoming fallback error: ${e.message}")
        }

        // Curated upcoming seasons
        CatalogData.curatedCatalog.flatMap { it.relations }
            .filter { it.relationType.contains("Sequel", ignoreCase = true) }
            .map { rel ->
                AnimeItem(
                    malId = rel.malId,
                    title = rel.title,
                    imageUrl = "https://cdn.myanimelist.net/images/anime/1865/144933.jpg",
                    synopsis = "Upcoming continuation for ${rel.title}.",
                    airingStatus = "Not Yet Aired",
                    isAiring = false,
                    season = "Upcoming",
                    genres = listOf("Action", "Fantasy")
                )
            }.distinctBy { it.malId }
    }

    suspend fun getSchedule(day: String?): List<AnimeItem> = withContext(Dispatchers.IO) {
        // 1. Primary: Jikan Weekly Schedule
        try {
            val queryDay = if (day.isNullOrBlank() || day.equals("Today", ignoreCase = true)) null else day.lowercase()
            val resp = api.getSchedules(day = queryDay, limit = 25)
            val items = resp.data?.map { JikanMapper.toDomain(it) } ?: emptyList()
            if (items.isNotEmpty()) {
                Log.d("AnimeRepository", "Jikan getSchedules returned ${items.size} items for day: $day")
                return@withContext items
            }
        } catch (e: Exception) {
            Log.w("AnimeRepository", "Jikan schedule network error: ${e.message}")
        }

        // 2. Backup: AniList Schedule
        try {
            val liveAiring = AniListApiClient.getCurrentlyAiring(limit = 30)
            val filteredByDay = if (!day.isNullOrBlank() && !day.equals("Today", ignoreCase = true)) {
                liveAiring.filter { it.broadcastDay.equals(day, ignoreCase = true) }
            } else {
                liveAiring
            }
            if (filteredByDay.isNotEmpty()) return@withContext filteredByDay
            if (liveAiring.isNotEmpty()) return@withContext liveAiring
        } catch (e: Exception) {
            Log.w("AnimeRepository", "AniList schedule fallback error: ${e.message}")
        }

        CatalogData.curatedCatalog.filter { it.isAiring }
    }

    suspend fun getAnimeDetails(malId: Int): AnimeItem = withContext(Dispatchers.IO) {
        // First check user library in LocalDatabase
        val animeEntity = animeDao.getAnimeByIdSync(malId)
        if (animeEntity != null) {
            val progress = userProgressDao.getProgressForAnimeSync(malId)
            return@withContext animeEntity.toAnimeItem(progress)
        }

        // 1. Primary: Jikan fast anime details
        try {
            val resp = api.getAnimeDetails(malId)
            if (resp.data != null) {
                Log.d("AnimeRepository", "Jikan getAnimeDetails loaded for ID $malId")
                return@withContext JikanMapper.toDomain(resp.data)
            }
        } catch (e: Exception) {
            Log.w("AnimeRepository", "Jikan anime details error: ${e.message}")
        }

        // 2. Secondary Jikan attempt: full endpoint
        try {
            val respFull = api.getAnimeFull(malId)
            if (respFull.data != null) {
                Log.d("AnimeRepository", "Jikan getAnimeFull loaded for ID $malId")
                return@withContext JikanMapper.toDomain(respFull.data)
            }
        } catch (e: Exception) {
            Log.w("AnimeRepository", "Jikan anime full error: ${e.message}")
        }

        // 3. Backup: AniList Details
        try {
            val aniDetails = AniListApiClient.getAnimeDetails(malId)
            if (aniDetails != null) return@withContext aniDetails
        } catch (e: Exception) {
            Log.w("AnimeRepository", "AniList detail fallback error: ${e.message}")
        }

        CatalogData.curatedCatalog.firstOrNull { it.malId == malId }
            ?: AnimeItem(
                malId = malId,
                title = "Anime #$malId",
                imageUrl = "",
                synopsis = "Details could not be loaded at this time. Please check your network connection."
            )
    }

    suspend fun getAnimeEpisodes(malId: Int, totalEpisodes: Int? = null): List<EpisodeItem> =
        withContext(Dispatchers.IO) {
            // 1. Primary: Jikan Real-time Episode List
            try {
                val resp = api.getAnimeEpisodes(malId)
                val episodes = resp.data?.mapIndexed { index, ep ->
                    EpisodeItem(
                        episodeNumber = ep.malId ?: (index + 1),
                        title = ep.title ?: "Episode ${ep.malId ?: (index + 1)}",
                        airedDate = ep.aired?.take(10),
                        isFiller = ep.filler == true
                    )
                }
                if (!episodes.isNullOrEmpty()) {
                    Log.d("AnimeRepository", "Jikan loaded ${episodes.size} episodes for ID $malId")
                    return@withContext episodes
                }
            } catch (e: Exception) {
                Log.w("AnimeRepository", "Jikan episodes network error: ${e.message}")
            }

            // Fallback generated episodes list
            val count = totalEpisodes ?: 12
            (1..count).map { num ->
                EpisodeItem(
                    episodeNumber = num,
                    title = "Episode $num",
                    airedDate = "Aired",
                    isFiller = false
                )
            }
        }

    suspend fun getRandomAnime(): AnimeItem = withContext(Dispatchers.IO) {
        try {
            val resp = api.getRandomAnime()
            if (resp.data != null) {
                return@withContext JikanMapper.toDomain(resp.data)
            }
        } catch (e: Exception) {
            Log.w("AnimeRepository", "Jikan random anime error: ${e.message}")
        }
        CatalogData.curatedCatalog.random()
    }

    // 4. Seed Onboarding
    suspend fun seedOnboarding(
        selections: List<Pair<AnimeItem, Int?>>,
        extraGenres: List<String>
    ) = withContext(Dispatchers.IO) {
        selections.forEach { (anime, rating) ->
            saveToLibrary(
                item = anime,
                status = if (rating != null && rating >= 8) WatchStatus.COMPLETED else WatchStatus.WATCHING,
                watchedEp = anime.episodesTotal ?: 12,
                rating = rating,
                isFav = (rating ?: 0) >= 9,
                notes = if (rating != null) "Initial rating from onboarding" else null
            )
        }
    }

    // 5. Session Memory & User Profile Persistence in Room Database
    val userProfile: Flow<UserProfile> = sessionDao.getActiveSession().map { session ->
        val profile = session?.toUserProfile() ?: UserProfile()
        cachedUserProfile = profile
        profile
    }

    val sessionInfo: Flow<SessionInfo> = sessionDao.getActiveSession().map { session ->
        session?.toSessionInfo() ?: SessionInfo()
    }

    val sessionLogs: Flow<List<SessionLogItem>> = sessionDao.getAllSessionHistory().map { list ->
        list.map { it.toSessionLogItem() }
    }

    suspend fun initializeSession(): SessionInfo = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val existing = sessionDao.getActiveSessionSync()
        if (existing == null) {
            // First time launch: Initialize session entity
            val firstSession = AppSessionEntity(
                sessionStartTime = now,
                lastActiveTime = now,
                sessionCount = 1
            )
            sessionDao.insertOrUpdateSession(firstSession)
            firstSession.toSessionInfo()
        } else {
            // Check session gap: If inactive for > 15 minutes or cold start, archive previous session
            val timeSinceLastActive = now - existing.lastActiveTime
            if (timeSinceLastActive > 15 * 60 * 1000L && existing.lastActiveTime > existing.sessionStartTime) {
                val durationMinutes = ((existing.lastActiveTime - existing.sessionStartTime) / (60 * 1000L)).coerceAtLeast(1L)
                val sessionLog = SessionHistoryEntity(
                    sessionNumber = existing.sessionCount,
                    startTime = existing.sessionStartTime,
                    endTime = existing.lastActiveTime,
                    durationMinutes = durationMinutes,
                    lastDestination = existing.lastActiveDestination,
                    episodesWatchedCount = existing.episodesWatchedThisSession,
                    summary = "Session #${existing.sessionCount} ended on ${existing.lastActiveDestination}. Tracked ${existing.episodesWatchedThisSession} episodes."
                )
                sessionDao.insertSessionHistory(sessionLog)
                // Start next session in SQLite
                sessionDao.startNewSession(startTime = now)
            } else {
                // Resume active session
                sessionDao.updateNavigation(
                    destination = existing.lastActiveDestination,
                    dayFilter = existing.lastActiveDayFilter,
                    animeId = existing.lastViewedAnimeId,
                    animeTitle = existing.lastViewedAnimeTitle,
                    animeImage = existing.lastViewedAnimeImage,
                    time = now
                )
            }
            sessionDao.getActiveSessionSync()?.toSessionInfo() ?: SessionInfo()
        }
    }

    suspend fun updateUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        cachedUserProfile = profile
        val existing = sessionDao.getActiveSessionSync()
        if (existing != null) {
            sessionDao.updateProfile(
                name = profile.name,
                email = profile.email,
                timezone = profile.timezone,
                episodes = profile.notifyNewEpisodes,
                seasons = profile.notifyNewSeasons,
                upcoming = profile.notifyUpcomingReminders,
                summary = profile.notifyWeeklySummary,
                time = System.currentTimeMillis()
            )
        } else {
            sessionDao.insertOrUpdateSession(
                AppSessionEntity(
                    userId = profile.id,
                    userName = profile.name,
                    userEmail = profile.email,
                    avatarUrl = profile.avatarUrl,
                    timezone = profile.timezone,
                    joinedDate = profile.joinedDate,
                    notifyNewEpisodes = profile.notifyNewEpisodes,
                    notifyNewSeasons = profile.notifyNewSeasons,
                    notifyUpcomingReminders = profile.notifyUpcomingReminders,
                    notifyWeeklySummary = profile.notifyWeeklySummary,
                    lastActiveTime = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun updateNavigationState(
        destination: String,
        dayFilter: String,
        animeId: Int? = null,
        animeTitle: String? = null,
        animeImage: String? = null
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val current = sessionDao.getActiveSessionSync()
        val targetId = animeId ?: current?.lastViewedAnimeId
        val targetTitle = animeTitle ?: current?.lastViewedAnimeTitle
        val targetImage = animeImage ?: current?.lastViewedAnimeImage

        sessionDao.updateNavigation(
            destination = destination,
            dayFilter = dayFilter,
            animeId = targetId,
            animeTitle = targetTitle,
            animeImage = targetImage,
            time = now
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) = withContext(Dispatchers.IO) {
        sessionDao.setOnboardingCompleted(completed, System.currentTimeMillis())
    }

    suspend fun startNewSessionManually(): SessionInfo = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val existing = sessionDao.getActiveSessionSync()
        if (existing != null) {
            val durationMinutes = ((existing.lastActiveTime - existing.sessionStartTime) / (60 * 1000L)).coerceAtLeast(1L)
            val sessionLog = SessionHistoryEntity(
                sessionNumber = existing.sessionCount,
                startTime = existing.sessionStartTime,
                endTime = now,
                durationMinutes = durationMinutes,
                lastDestination = existing.lastActiveDestination,
                episodesWatchedCount = existing.episodesWatchedThisSession,
                summary = "Session #${existing.sessionCount} manual checkpoint: ${existing.episodesWatchedThisSession} episodes tracked."
            )
            sessionDao.insertSessionHistory(sessionLog)
            sessionDao.startNewSession(startTime = now)
        }
        sessionDao.getActiveSessionSync()?.toSessionInfo() ?: SessionInfo()
    }

    suspend fun clearSessionLogs() = withContext(Dispatchers.IO) {
        sessionDao.clearSessionHistory()
    }

    suspend fun resetSession() = withContext(Dispatchers.IO) {
        sessionDao.clearSessionHistory()
        val freshSession = AppSessionEntity(
            sessionStartTime = System.currentTimeMillis(),
            lastActiveTime = System.currentTimeMillis(),
            sessionCount = 1
        )
        sessionDao.insertOrUpdateSession(freshSession)
    }

    fun getUserProfile(): UserProfile = cachedUserProfile
}


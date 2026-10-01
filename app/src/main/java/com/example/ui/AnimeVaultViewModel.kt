package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AnimeItem
import com.example.data.model.AnimeNewsItem
import com.example.data.model.AnimeReleaseItem
import com.example.data.model.EpisodeItem
import com.example.data.model.Recommendation
import com.example.data.model.SessionInfo
import com.example.data.model.SessionLogItem
import com.example.data.model.TasteProfile
import com.example.data.model.UserProfile
import com.example.data.model.WatchHistoryEntry
import com.example.data.model.WatchStatus
import com.example.data.remote.CatalogData
import com.example.data.repository.AnimeRepository
import com.example.domain.recommendation.RecommendationEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class LibrarySort(val displayName: String) {
    RECENTLY_UPDATED("Recently Updated"),
    TITLE("Title"),
    RATING("Rating"),
    PROGRESS("Progress"),
    RELEASE_DATE("Release Date")
}

data class UiState(
    val userLibrary: List<AnimeItem> = emptyList(),
    val filteredLibrary: List<AnimeItem> = emptyList(),
    val selectedStatusTab: String = "ALL", // "ALL", "WATCHING", "COMPLETED", "PLAN_TO_WATCH", "ON_HOLD", "DROPPED", "FAVORITES"
    val selectedSort: LibrarySort = LibrarySort.RECENTLY_UPDATED,
    val selectedGenreFilter: String? = null,
    val continueWatching: List<AnimeItem> = emptyList(),
    val newEpisodesToday: List<AnimeItem> = emptyList(),
    val comingSoon: List<AnimeItem> = emptyList(),
    val comingSoonFilter: String = "Today", // "Today", "Tomorrow", "This Week"
    val recommendations: List<Recommendation> = emptyList(),
    val tasteProfile: TasteProfile = TasteProfile(),
    val trendingAnime: List<AnimeItem> = emptyList(),
    val airingAnime: List<AnimeItem> = emptyList(),
    val upcomingAnime: List<AnimeItem> = emptyList(),
    val calendarSchedule: List<AnimeItem> = emptyList(),
    val selectedCalendarDay: String = "Today",
    val searchQuery: String = "",
    val searchResults: List<AnimeItem> = emptyList(),
    val isSearching: Boolean = false,
    val watchHistory: List<WatchHistoryEntry> = emptyList(),
    val activeDetailsAnime: AnimeItem? = null,
    val activeEpisodes: List<EpisodeItem> = emptyList(),
    val activeWatchedEpisodeNumbers: Set<Int> = emptySet(),
    val showAddDialog: Boolean = false,
    val selectedAnimeForAdd: AnimeItem? = null,
    val showOnboarding: Boolean = false,
    val userProfile: UserProfile = UserProfile(),
    val sessionInfo: SessionInfo = SessionInfo(),
    val sessionLogs: List<SessionLogItem> = emptyList(),
    val sessionDurationFormatted: String = "0m",
    val restoredDestination: String? = null,
    val animeNews: List<AnimeNewsItem> = emptyList(),
    val isNewsLoading: Boolean = false,
    val activeNewsItem: AnimeNewsItem? = null,
    val upcomingReleases: List<AnimeReleaseItem> = emptyList(),
    val jikanStatus: String = "Jikan API Live",
    val toastMessage: String? = null,
    val isLoading: Boolean = false
)

class AnimeVaultViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AnimeRepository(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var allCandidates = CatalogData.curatedCatalog

    init {
        loadInitialData()
        observeLibrary()
        observeWatchHistory()
        observeSession()
        startSessionTicker()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isNewsLoading = true)

            // Initialize or resume database-backed session
            val session = repository.initializeSession()
            val isEmpty = repository.isLibraryEmpty()
            val showOnboarding = !session.isOnboardingCompleted && isEmpty

            _uiState.value = _uiState.value.copy(
                showOnboarding = showOnboarding,
                selectedCalendarDay = session.lastDayFilter.ifEmpty { "Today" },
                restoredDestination = session.lastDestination
            )

            // Launch news fetch concurrently
            launch {
                val news = repository.getNews()
                _uiState.value = _uiState.value.copy(
                    animeNews = news,
                    isNewsLoading = false
                )
            }

            // Launch upcoming episode releases concurrently
            launch {
                val releases = repository.getAiringReleases()
                _uiState.value = _uiState.value.copy(upcomingReleases = releases)
            }

            // Load remote or catalog lists
            val trending = repository.getTrending()
            val airing = repository.getCurrentlyAiring()
            val upcoming = repository.getUpcoming()
            val todayDay = if (session.lastDayFilter.equals("Today", ignoreCase = true)) {
                SimpleDateFormat("EEEE", Locale.ENGLISH).format(Calendar.getInstance().time)
            } else session.lastDayFilter
            val schedule = repository.getSchedule(todayDay)

            allCandidates = (trending + airing + upcoming + CatalogData.curatedCatalog).distinctBy { it.malId }

            _uiState.value = _uiState.value.copy(
                trendingAnime = trending,
                airingAnime = airing,
                upcomingAnime = upcoming,
                calendarSchedule = schedule,
                isLoading = false
            )
            refreshRecommendations()
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                _uiState.value = _uiState.value.copy(userProfile = profile)
            }
        }
        viewModelScope.launch {
            repository.sessionInfo.collect { session ->
                _uiState.value = _uiState.value.copy(sessionInfo = session)
            }
        }
        viewModelScope.launch {
            repository.sessionLogs.collect { logs ->
                _uiState.value = _uiState.value.copy(sessionLogs = logs)
            }
        }
    }

    private fun startSessionTicker() {
        viewModelScope.launch {
            while (true) {
                val startTime = _uiState.value.sessionInfo.sessionStartTime
                val elapsedMinutes = ((System.currentTimeMillis() - startTime) / (60 * 1000L)).coerceAtLeast(0)
                val formatted = if (elapsedMinutes >= 60) {
                    val hours = elapsedMinutes / 60
                    val mins = elapsedMinutes % 60
                    "${hours}h ${mins}m"
                } else {
                    "${elapsedMinutes}m"
                }
                _uiState.value = _uiState.value.copy(sessionDurationFormatted = formatted)
                delay(30_000L)
            }
        }
    }

    private fun observeLibrary() {
        viewModelScope.launch {
            repository.userLibrary.collect { library ->
                val continueWatching = library.filter { it.userStatus == WatchStatus.WATCHING }
                    .sortedByDescending { it.lastWatchedAt ?: it.updatedAt }

                val currentDay = SimpleDateFormat("EEEE", Locale.ENGLISH).format(Calendar.getInstance().time)
                val newToday = library.filter {
                    it.isAiring && (it.broadcastDay.equals(currentDay, ignoreCase = true) || it.broadcastDay == null)
                }

                val coming = library.filter { it.isAiring }

                val taste = RecommendationEngine.calculateTasteProfile(library)

                _uiState.value = _uiState.value.copy(
                    userLibrary = library,
                    continueWatching = continueWatching,
                    newEpisodesToday = newToday,
                    comingSoon = coming,
                    tasteProfile = taste
                )
                applyLibraryFilters()
                refreshRecommendations()
            }
        }
    }

    private fun observeWatchHistory() {
        viewModelScope.launch {
            repository.watchHistory.collect { history ->
                _uiState.value = _uiState.value.copy(watchHistory = history)
            }
        }
    }

    private fun refreshRecommendations() {
        val library = _uiState.value.userLibrary
        val recs = RecommendationEngine.generateRecommendations(
            candidatePool = allCandidates,
            userLibrary = library,
            limit = 10
        )
        _uiState.value = _uiState.value.copy(recommendations = recs)
    }

    fun applyLibraryFilters() {
        val state = _uiState.value
        var list = state.userLibrary

        // Status tab
        list = when (state.selectedStatusTab) {
            "WATCHING" -> list.filter { it.userStatus == WatchStatus.WATCHING }
            "COMPLETED" -> list.filter { it.userStatus == WatchStatus.COMPLETED }
            "PLAN_TO_WATCH" -> list.filter { it.userStatus == WatchStatus.PLAN_TO_WATCH }
            "ON_HOLD" -> list.filter { it.userStatus == WatchStatus.ON_HOLD }
            "DROPPED" -> list.filter { it.userStatus == WatchStatus.DROPPED }
            "FAVORITES" -> list.filter { it.isFavorite }
            else -> list
        }

        // Genre filter
        if (!state.selectedGenreFilter.isNullOrBlank()) {
            list = list.filter { it.genres.any { g -> g.equals(state.selectedGenreFilter, ignoreCase = true) } }
        }

        // Sort
        list = when (state.selectedSort) {
            LibrarySort.RECENTLY_UPDATED -> list.sortedByDescending { it.updatedAt }
            LibrarySort.TITLE -> list.sortedBy { it.title }
            LibrarySort.RATING -> list.sortedByDescending { it.userRating ?: 0 }
            LibrarySort.PROGRESS -> list.sortedByDescending {
                val total = it.episodesTotal ?: 1
                it.episodesWatched.toDouble() / total.toDouble()
            }
            LibrarySort.RELEASE_DATE -> list.sortedByDescending { it.year ?: 0 }
        }

        _uiState.value = _uiState.value.copy(filteredLibrary = list)
    }

    fun selectStatusTab(tab: String) {
        _uiState.value = _uiState.value.copy(selectedStatusTab = tab)
        applyLibraryFilters()
    }

    fun selectSort(sort: LibrarySort) {
        _uiState.value = _uiState.value.copy(selectedSort = sort)
        applyLibraryFilters()
    }

    fun selectGenreFilter(genre: String?) {
        _uiState.value = _uiState.value.copy(selectedGenreFilter = genre)
        applyLibraryFilters()
    }

    fun setComingSoonFilter(filter: String) {
        _uiState.value = _uiState.value.copy(comingSoonFilter = filter)
    }

    fun searchAnime(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }

        searchJob = viewModelScope.launch {
            delay(350) // Debounce
            _uiState.value = _uiState.value.copy(isSearching = true)
            val results = repository.searchAnime(query)
            _uiState.value = _uiState.value.copy(searchResults = results, isSearching = false)
        }
    }

    fun updateProgress(anime: AnimeItem, newProgress: Int) {
        viewModelScope.launch {
            repository.updateProgress(anime.malId, newProgress)
            showToast("Updated ${anime.title} to Episode $newProgress")
        }
    }

    fun incrementProgress(anime: AnimeItem) {
        val current = anime.episodesWatched
        val total = anime.episodesTotal
        if (total != null && current >= total) {
            showToast("All $total episodes already watched!")
            return
        }
        updateProgress(anime, current + 1)
    }

    fun decrementProgress(anime: AnimeItem) {
        val current = anime.episodesWatched
        if (current <= 0) return
        updateProgress(anime, current - 1)
    }

    fun saveToLibrary(
        item: AnimeItem,
        status: WatchStatus,
        watchedEp: Int,
        rating: Int?,
        isFav: Boolean,
        notes: String?
    ) {
        viewModelScope.launch {
            repository.saveToLibrary(
                item = item,
                status = status,
                watchedEp = watchedEp,
                rating = rating,
                isFav = isFav,
                notes = notes
            )
            _uiState.value = _uiState.value.copy(showAddDialog = false, selectedAnimeForAdd = null)
            showToast("Added ${item.title} to ${status.displayName}")
        }
    }

    fun updateAnimeStatus(malId: Int, status: WatchStatus) {
        viewModelScope.launch {
            repository.updateStatus(malId, status)
            showToast("Moved to ${status.displayName}")
        }
    }

    fun updateAnimeRating(malId: Int, rating: Int?) {
        viewModelScope.launch {
            repository.updateRating(malId, rating)
            showToast(if (rating != null) "Rated $rating/10" else "Rating removed")
        }
    }

    fun toggleAnimeFavorite(malId: Int, currentFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(malId, !currentFav)
            showToast(if (!currentFav) "Marked as Favorite" else "Removed from Favorites")
        }
    }

    fun updateAnimeNotes(malId: Int, notes: String?) {
        viewModelScope.launch {
            repository.updateNotes(malId, notes)
            showToast("Saved notes")
        }
    }

    fun deleteFromLibrary(malId: Int) {
        viewModelScope.launch {
            repository.deleteFromLibrary(malId)
            _uiState.value = _uiState.value.copy(activeDetailsAnime = null)
            showToast("Removed from library")
        }
    }

    fun openAnimeDetails(anime: AnimeItem) {
        viewModelScope.launch {
            repository.updateNavigationState(
                destination = _uiState.value.restoredDestination ?: "HOME",
                dayFilter = _uiState.value.selectedCalendarDay,
                animeId = anime.malId,
                animeTitle = anime.title,
                animeImage = anime.imageUrl
            )
            val fullDetails = repository.getAnimeDetails(anime.malId)
            _uiState.value = _uiState.value.copy(activeDetailsAnime = fullDetails)

            // Load episodes
            val episodes = repository.getAnimeEpisodes(anime.malId, fullDetails.episodesTotal)
            _uiState.value = _uiState.value.copy(activeEpisodes = episodes)

            // Observe watched episodes
            repository.getWatchedEpisodeNumbers(anime.malId).collect { watchedSet ->
                _uiState.value = _uiState.value.copy(activeWatchedEpisodeNumbers = watchedSet)
            }
        }
    }

    fun closeAnimeDetails() {
        _uiState.value = _uiState.value.copy(activeDetailsAnime = null, activeEpisodes = emptyList())
    }

    fun toggleEpisodeWatched(anime: AnimeItem, epNum: Int) {
        viewModelScope.launch {
            repository.toggleEpisodeWatched(anime.malId, epNum, anime.title, anime.imageUrl)
        }
    }

    fun openAddDialog(anime: AnimeItem? = null) {
        _uiState.value = _uiState.value.copy(showAddDialog = true, selectedAnimeForAdd = anime)
    }

    fun closeAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, selectedAnimeForAdd = null)
    }

    fun updateSessionDestination(destination: String) {
        _uiState.value = _uiState.value.copy(restoredDestination = destination)
        viewModelScope.launch {
            repository.updateNavigationState(
                destination = destination,
                dayFilter = _uiState.value.selectedCalendarDay
            )
        }
    }

    fun selectCalendarDay(day: String) {
        _uiState.value = _uiState.value.copy(selectedCalendarDay = day)
        viewModelScope.launch {
            repository.updateNavigationState(
                destination = _uiState.value.restoredDestination ?: "CALENDAR",
                dayFilter = day
            )
            val apiDay = if (day.equals("Today", ignoreCase = true)) {
                SimpleDateFormat("EEEE", Locale.ENGLISH).format(Calendar.getInstance().time)
            } else day
            val schedule = repository.getSchedule(apiDay)
            _uiState.value = _uiState.value.copy(calendarSchedule = schedule)
        }
    }

    fun completeOnboarding(
        selections: List<Pair<AnimeItem, Int?>>,
        extraGenres: List<String>
    ) {
        viewModelScope.launch {
            repository.seedOnboarding(selections, extraGenres)
            repository.setOnboardingCompleted(true)
            _uiState.value = _uiState.value.copy(showOnboarding = false)
            showToast("Welcome to AnimeVault! Your taste profile has been saved.")
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
            _uiState.value = _uiState.value.copy(showOnboarding = false)
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateUserProfile(profile)
            _uiState.value = _uiState.value.copy(userProfile = profile)
            showToast("Profile settings saved to database")
        }
    }

    fun startNewSession() {
        viewModelScope.launch {
            val newSession = repository.startNewSessionManually()
            showToast("Started Session #${newSession.sessionNumber}")
        }
    }

    fun clearSessionLogs() {
        viewModelScope.launch {
            repository.clearSessionLogs()
            showToast("Session history cleared from database")
        }
    }

    fun resetSessionData() {
        viewModelScope.launch {
            repository.resetSession()
            showToast("Session memory reset")
        }
    }

    fun resumeLastViewedAnime() {
        val lastId = _uiState.value.sessionInfo.lastViewedAnimeId ?: return
        viewModelScope.launch {
            val anime = repository.getAnimeDetails(lastId)
            openAnimeDetails(anime)
        }
    }


    fun refreshNews() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isNewsLoading = true)
            val news = repository.getNews()
            _uiState.value = _uiState.value.copy(animeNews = news, isNewsLoading = false)
            showToast("Anime news updated")
        }
    }

    fun openNewsItem(newsItem: AnimeNewsItem) {
        _uiState.value = _uiState.value.copy(activeNewsItem = newsItem)
    }

    fun closeNewsItem() {
        _uiState.value = _uiState.value.copy(activeNewsItem = null)
    }

    fun refreshAllData() {
        refreshFromJikan()
    }

    fun refreshFromJikan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, toastMessage = "Connecting to Jikan API...")
            val trending = repository.getTrending()
            val airing = repository.getCurrentlyAiring()
            val upcoming = repository.getUpcoming()
            val todayDay = SimpleDateFormat("EEEE", Locale.ENGLISH).format(Calendar.getInstance().time)
            val schedule = repository.getSchedule(todayDay)

            allCandidates = (trending + airing + upcoming + CatalogData.curatedCatalog).distinctBy { it.malId }

            _uiState.value = _uiState.value.copy(
                trendingAnime = trending,
                airingAnime = airing,
                upcomingAnime = upcoming,
                calendarSchedule = schedule,
                isLoading = false,
                toastMessage = "Jikan real-time sync complete"
            )
            refreshRecommendations()
        }
    }

    fun fetchRandomAnime(onFound: (AnimeItem) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, toastMessage = "Fetching surprise anime from Jikan...")
            val anime = repository.getRandomAnime()
            _uiState.value = _uiState.value.copy(isLoading = false, toastMessage = null)
            onFound(anime)
        }
    }

    fun showToast(message: String) {
        _uiState.value = _uiState.value.copy(toastMessage = message)
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}

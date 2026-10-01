package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.dialogs.AddAnimeDialog
import com.example.ui.dialogs.AnimeDetailsDialog
import com.example.ui.dialogs.AnimeNewsDialog
import com.example.ui.dialogs.OnboardingDialog
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VaultBlack
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultCardElevated
import com.example.ui.theme.VaultDarkSurface

enum class AppDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    MY_ANIME("My Anime", Icons.Default.VideoLibrary),
    CALENDAR("Calendar", Icons.Default.CalendarMonth),
    DISCOVER("Discover", Icons.Default.Explore),
    PROFILE("Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AnimeVaultViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var hasRestoredNav by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.restoredDestination) {
        val dest = uiState.restoredDestination
        if (!hasRestoredNav && !dest.isNullOrBlank()) {
            try {
                currentDestination = AppDestination.valueOf(dest)
                hasRestoredNav = true
            } catch (_: Exception) {}
        }
    }

    val navigateToDestination = { dest: AppDestination ->
        currentDestination = dest
        viewModel.updateSessionDestination(dest.name)
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        containerColor = VaultBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CrimsonAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "AV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AnimeVault",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshFromJikan() },
                        modifier = Modifier.testTag("topbar_refresh_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sync Jikan API", tint = CyanAccent)
                    }
                    IconButton(
                        onClick = { navigateToDestination(AppDestination.DISCOVER) },
                        modifier = Modifier.testTag("topbar_search_button")
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                    }
                    IconButton(
                        onClick = { viewModel.openAddDialog(null) },
                        modifier = Modifier.testTag("topbar_add_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Anime", tint = CrimsonAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VaultDarkSurface.copy(alpha = 0.95f),
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = VaultDarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                AppDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { navigateToDestination(destination) },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = CrimsonAccent,
                            indicatorColor = CrimsonAccent,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary
                        ),
                        modifier = Modifier.testTag("nav_${destination.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentDestination != AppDestination.PROFILE) {
                FloatingActionButton(
                    onClick = { viewModel.openAddDialog(null) },
                    containerColor = CrimsonAccent,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_add_anime")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add anime to vault")
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = VaultCardElevated,
                    contentColor = TextPrimary,
                    actionColor = CrimsonAccent,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(VaultBlack)
        ) {
            when (currentDestination) {
                AppDestination.HOME -> HomeScreen(
                    uiState = uiState,
                    onAnimeClick = { viewModel.openAnimeDetails(it) },
                    onIncrementProgress = { viewModel.incrementProgress(it) },
                    onDecrementProgress = { viewModel.decrementProgress(it) },
                    onMarkWatched = { viewModel.incrementProgress(it) },
                    onAddToLibrary = { viewModel.openAddDialog(it) },
                    onComingSoonFilterChange = { viewModel.setComingSoonFilter(it) },
                    onNavigateToDiscover = { navigateToDestination(AppDestination.DISCOVER) },
                    onNewsClick = { viewModel.openNewsItem(it) },
                    onSurpriseClick = {
                        viewModel.fetchRandomAnime { anime ->
                            viewModel.openAnimeDetails(anime)
                        }
                    }
                )

                AppDestination.MY_ANIME -> LibraryScreen(
                    uiState = uiState,
                    onAnimeClick = { viewModel.openAnimeDetails(it) },
                    onIncrementProgress = { viewModel.incrementProgress(it) },
                    onDecrementProgress = { viewModel.decrementProgress(it) },
                    onStatusTabSelected = { viewModel.selectStatusTab(it) },
                    onSortSelected = { viewModel.selectSort(it) },
                    onGenreFilterSelected = { viewModel.selectGenreFilter(it) },
                    onOpenAddAnime = { viewModel.openAddDialog(null) }
                )

                AppDestination.CALENDAR -> CalendarScreen(
                    uiState = uiState,
                    onAnimeClick = { viewModel.openAnimeDetails(it) },
                    onDaySelected = { viewModel.selectCalendarDay(it) }
                )

                AppDestination.DISCOVER -> DiscoverScreen(
                    uiState = uiState,
                    onAnimeClick = { viewModel.openAnimeDetails(it) },
                    onSearchChange = { viewModel.searchAnime(it) },
                    onAddToLibrary = { viewModel.openAddDialog(it) },
                    onNewsClick = { viewModel.openNewsItem(it) },
                    onSurpriseClick = {
                        viewModel.fetchRandomAnime { anime ->
                            viewModel.openAnimeDetails(anime)
                        }
                    }
                )

                AppDestination.PROFILE -> ProfileScreen(
                    uiState = uiState,
                    onUpdateProfile = { viewModel.updateProfile(it) },
                    onStartNewSession = { viewModel.startNewSession() },
                    onClearSessionLogs = { viewModel.clearSessionLogs() },
                    onResumeLastAnime = { viewModel.resumeLastViewedAnime() }
                )
            }

            // Anime Details Dialog Modal
            if (uiState.activeDetailsAnime != null) {
                AnimeDetailsDialog(
                    anime = uiState.activeDetailsAnime!!,
                    episodes = uiState.activeEpisodes,
                    watchedEpisodeNumbers = uiState.activeWatchedEpisodeNumbers,
                    onDismiss = { viewModel.closeAnimeDetails() },
                    onStatusChange = { viewModel.updateAnimeStatus(uiState.activeDetailsAnime!!.malId, it) },
                    onProgressChange = { viewModel.updateProgress(uiState.activeDetailsAnime!!, it) },
                    onRatingChange = { viewModel.updateAnimeRating(uiState.activeDetailsAnime!!.malId, it) },
                    onFavoriteToggle = { viewModel.toggleAnimeFavorite(uiState.activeDetailsAnime!!.malId, it) },
                    onNotesChange = { viewModel.updateAnimeNotes(uiState.activeDetailsAnime!!.malId, it) },
                    onDeleteFromLibrary = { viewModel.deleteFromLibrary(uiState.activeDetailsAnime!!.malId) },
                    onToggleEpisode = { viewModel.toggleEpisodeWatched(uiState.activeDetailsAnime!!, it.episodeNumber) },
                    onRelatedAnimeClick = { malId, _ ->
                        viewModel.openAnimeDetails(
                            com.example.data.model.AnimeItem(
                                malId = malId,
                                title = "Loading...",
                                imageUrl = "",
                                synopsis = "Loading anime details..."
                            )
                        )
                    }
                )
            }

            // Add Anime Dialog Modal
            if (uiState.showAddDialog) {
                AddAnimeDialog(
                    initialAnime = uiState.selectedAnimeForAdd,
                    onDismiss = { viewModel.closeAddDialog() },
                    onSave = { anime, status, progress, rating, isFav, notes ->
                        viewModel.saveToLibrary(anime, status, progress, rating, isFav, notes)
                    }
                )
            }

            // Onboarding Dialog Modal
            if (uiState.showOnboarding) {
                OnboardingDialog(
                    onComplete = { selections, extraGenres ->
                        viewModel.completeOnboarding(selections, extraGenres)
                    },
                    onSkip = { viewModel.skipOnboarding() }
                )
            }

            // Anime News Article Modal
            if (uiState.activeNewsItem != null) {
                AnimeNewsDialog(
                    newsItem = uiState.activeNewsItem!!,
                    onDismiss = { viewModel.closeNewsItem() }
                )
            }
        }
    }
}
